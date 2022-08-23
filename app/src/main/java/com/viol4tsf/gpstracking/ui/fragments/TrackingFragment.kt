package com.viol4tsf.gpstracking.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.GoogleMap
import com.viol4tsf.gpstracking.R
import com.viol4tsf.gpstracking.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.viol4tsf.gpstracking.services.TrackingService
import com.viol4tsf.gpstracking.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*

@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking){

    private val viewModel: MainViewModel by viewModels()
    private var map: GoogleMap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //контроль ЖЦ представления карты
        mapView.onCreate(savedInstanceState)

        btnToggleRun.setOnClickListener{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }

        //загрузка карты в представление
        mapView.getMapAsync {
            map = it
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