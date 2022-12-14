package com.viol4tsf.gpstracking.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.viol4tsf.gpstracking.R
import com.viol4tsf.gpstracking.db.Run
import com.viol4tsf.gpstracking.other.Constants.ACTION_PAUSE_SERVICE
import com.viol4tsf.gpstracking.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.viol4tsf.gpstracking.other.Constants.ACTION_STOP_SERVICE
import com.viol4tsf.gpstracking.other.Constants.MAP_ZOOM
import com.viol4tsf.gpstracking.other.Constants.POLYLINE_COLOR
import com.viol4tsf.gpstracking.other.Constants.POLYLINE_WIDTH
import com.viol4tsf.gpstracking.other.TrackingUtility
import com.viol4tsf.gpstracking.services.Polyline
import com.viol4tsf.gpstracking.services.TrackingService
import com.viol4tsf.gpstracking.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_TRACKING_DIALOG_TAG = "CancelDialog"

@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking){

    private val viewModel: MainViewModel by viewModels()
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var map: GoogleMap? = null
    private var currentTimeInMillis = 0L
    private var totalDistance = 0f

    private var menu: Menu? = null

    @set:Inject
    var weight = 65f

    @set:Inject
    var distance = 2000L

    @set:Inject
    var calories = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeInMillis > 0L){
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.cancelTrackingMenuItem -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog(){
        CancelTrackingDialog().apply {
            setYListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //???????????????? ???? ?????????????????????????? ??????????
        mapView.onCreate(savedInstanceState)

        maxDistanceTextView.text = "/${distance}"
        maxCaloriesTextView.text = "/${calories}"
        distanceCircularProgressBar.progressMax = distance.toFloat()
        caloriesCircularProgressBar.progressMax = calories.toFloat()

        toggleRunButton.setOnClickListener{
            toggleRun()
        }

        finishRunButton.setOnClickListener {
            zoomToSeeAllTrack()
            endRunAndSaveToDB()
        }

        if (savedInstanceState != null){
            val cancelTrackingDialog = parentFragmentManager
                .findFragmentByTag(CANCEL_TRACKING_DIALOG_TAG) as CancelTrackingDialog?
            cancelTrackingDialog?.setYListener {
                stopRun()
            }
        }

        //???????????????? ?????????? ?? ??????????????????????????
        mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        subscribeToObservers()
    }

    //?????????????????????? ?? ????????????????????????
    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
            var distanceInMeters = 0
            for (polyline in pathPoints){
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            distanceProgressTextView.text = distanceInMeters.toString()
            caloriesProgressTextView.text = caloriesBurned.toString()
            distanceCircularProgressBar.apply {
                setProgressWithAnimation(distanceInMeters.toFloat())
            }
            caloriesCircularProgressBar.apply {
                setProgressWithAnimation(caloriesBurned.toFloat())
            }
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis, true)
            timerTextView.text = formattedTime
        })
    }

    //?????????????????????????? ??????????????
    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun stopRun(){
        timerTextView.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    //???????????????????????? ??????????????????
    private fun updateTracking(isTracking: Boolean){
        this.isTracking = isTracking
        if (!isTracking && currentTimeInMillis > 0L){
            toggleRunButton.text = "??????????"
            finishRunButton.visibility = View.VISIBLE
        } else if (isTracking){
            toggleRunButton.text = "????????"
            menu?.getItem(0)?.isVisible = true
            finishRunButton.visibility = View.GONE
        }
    }

    //???????????????? ???????????? ???? ??????????????????????????
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

    //???????????????????? ???????? ??????????????????
    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    //???????????????????? ?????????????????? ??????????????????
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

    //?????????????????? ???????????? ?????? ???????????? ?????????? ????????????????
    private fun zoomToSeeAllTrack(){
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (position in polyline) {
                bounds.include(position)
            }
        }
        //?????????????? ?????????????????????? ???????????? ?????????? ?????? ????????????
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                //???????????????????????????? ??????????
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDB(){
        //??????????
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for (polyline in pathPoints){
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed = round((distanceInMeters / 1000f) / (currentTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(bmp, dateTimestamp, avgSpeed, distanceInMeters, currentTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "???????????????? ?????????????? ??????????????????!",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    //?????????????????? ???????????????? ???????????? ?????? ????????????????
    private fun sendCommandToService(action: String)
        = Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        addAllPolylines()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        addAllPolylines()
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