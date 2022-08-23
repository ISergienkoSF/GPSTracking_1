package com.viol4tsf.gpstracking.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.viol4tsf.gpstracking.R
import com.viol4tsf.gpstracking.other.Constants.ACTION_PAUSE_SERVICE
import com.viol4tsf.gpstracking.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.viol4tsf.gpstracking.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.viol4tsf.gpstracking.other.Constants.ACTION_STOP_SERVICE
import com.viol4tsf.gpstracking.other.Constants.NOTIFICATION_CHANNEL_ID
import com.viol4tsf.gpstracking.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.viol4tsf.gpstracking.other.Constants.NOTIFICATION_ID
import com.viol4tsf.gpstracking.ui.MainActivity
import timber.log.Timber

//фоновая служба
class TrackingService : LifecycleService(){

    var isFirstRun = true

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
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    //функция для фактического запуска службы
    private fun startForegroundService() {
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