package com.viol4tsf.gpstracking.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.viol4tsf.gpstracking.R
import com.viol4tsf.gpstracking.other.Constants.ACTION_PAUSE_SERVICE
import com.viol4tsf.gpstracking.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.viol4tsf.gpstracking.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.viol4tsf.gpstracking.other.Constants.ACTION_STOP_SERVICE
import com.viol4tsf.gpstracking.other.Constants.FASTEST_LOCATION_INTERVAL
import com.viol4tsf.gpstracking.other.Constants.LOCATION_UPDATE_INTERVAL
import com.viol4tsf.gpstracking.other.Constants.NOTIFICATION_CHANNEL_ID
import com.viol4tsf.gpstracking.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.viol4tsf.gpstracking.other.Constants.NOTIFICATION_ID
import com.viol4tsf.gpstracking.other.Constants.TIMER_UPDATE_INTERVAL
import com.viol4tsf.gpstracking.other.TrackingUtility
import com.viol4tsf.gpstracking.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

typealias Polyline = MutableList<LatLng> //список координат
typealias Polylines = MutableList<Polyline> //список списков координат

//фоновая служба
class TrackingService : LifecycleService(){

    var isFirstRun = true

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>() //координаты
    }

    //отправка начальных значений
    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            //просмотр ЖЦ карты
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L //начальное время запуска для определения промежутков
    private var timeRun = 0L //общее время прогулки
    private var timeStarted = 0L //время запуска прогулки
    private var lastSecondTimestamp = 0L //последнее значение целой секунды

    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        //создание потока с помощью корутин
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                //разница между текущим временем и временем старта
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    //активация отслеживания местоположения
    private fun updateLocationTracking(isTracking: Boolean) {
        if(isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)){
                val request = LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    //коллбэк для обновления локации
    val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if(isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    //добавление точки координат в конец списка
    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                //добавление координаты на последнее место
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    //добавление пустой полилинии
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf())) //добавление новой пустой полилинии если значение 0(список пуст)

    //функция для фактического запуска службы
    private fun startForegroundService() {

        startTimer()
        isTracking.postValue(true)

        //служба уведомлений
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //создание канала уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        //создание фактического уведомления
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            //отмена скрытия уведомления при нажатии на него
            .setAutoCancel(false)
            //уведомление нельзя смахнуть
            .setOngoing(true)
            //установка маленького значка
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentTitle("GPS Tracking")
            .setContentText("00:00:00")
            //подключение ожидающего интента
            .setContentIntent(getMainActivityPendingIntent())

        //запуск уведомления(передний сервис)
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    //ожидающий Intent
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )

    //создание канала уведомлений
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        //создание канала уведомлений с низкой важностью, потому что будут частые обновления и не было звуковых оповещений
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }
}