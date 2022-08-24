package com.viol4tsf.gpstracking.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import com.viol4tsf.gpstracking.R
import com.viol4tsf.gpstracking.other.Constants.ACTION_PAUSE_SERVICE
import com.viol4tsf.gpstracking.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.viol4tsf.gpstracking.other.Constants.MAP_ZOOM
import com.viol4tsf.gpstracking.other.Constants.POLYLINE_COLOR
import com.viol4tsf.gpstracking.other.Constants.POLYLINE_WIDTH
import com.viol4tsf.gpstracking.other.TrackingUtility
import com.viol4tsf.gpstracking.services.Polyline
import com.viol4tsf.gpstracking.services.TrackingService
import com.viol4tsf.gpstracking.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*

@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking){

    private val viewModel: MainViewModel by viewModels()
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var map: GoogleMap? = null
    private var currentTimeInMillis = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //контроль ЖЦ представления карты
        mapView.onCreate(savedInstanceState)

        toggleRunButton.setOnClickListener{
            toggleRun()
        }

        //загрузка карты в представление
        mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        subscribeToObservers()
    }

    //подключение к наблюдателям
    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis, true)
            timerTextView.text = formattedTime
        })
    }

    //переключатель запуска
    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    //отслеживание изменений
    private fun updateTracking(isTracking: Boolean){
        this.isTracking = isTracking
        if (!isTracking){
            toggleRunButton.text = "Старт"
            finishRunButton.visibility = View.VISIBLE
        } else {
            toggleRunButton.text = "Стоп"
            finishRunButton.visibility = View.GONE
        }
    }

    //движение камеры за пользователем
    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    //соединение всех полилиний
    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    //соединение последних координат
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    //установка действия службы для передачи
    private fun sendCommandToService(action: String)
        = Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}