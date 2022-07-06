package com.androiddevs.runningappyt.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.db.Run
import com.androiddevs.runningappyt.other.Constants.ACTION_PAUSE_SERVICE
import com.androiddevs.runningappyt.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.androiddevs.runningappyt.other.Constants.ACTION_STOP_SERVICE
import com.androiddevs.runningappyt.other.Constants.KEY_WEIGHT
import com.androiddevs.runningappyt.other.Constants.MAP_ZOOM
import com.androiddevs.runningappyt.other.Constants.POLYLINE_COLOR
import com.androiddevs.runningappyt.other.Constants.POLYLINE_WIDTH
import com.androiddevs.runningappyt.other.TimeFormatterUtility
import com.androiddevs.runningappyt.service.Polyline
import com.androiddevs.runningappyt.service.TrackingService
import com.androiddevs.runningappyt.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.round
import kotlin.properties.Delegates

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var weight by Delegates.notNull<Float>()

    private val viewModel: MainViewModel by viewModels()

    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var menu: Menu? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        weight = sharedPreferences.getFloat(KEY_WEIGHT, 80f)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private var currentTimeMillis = 0L
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView?.onCreate(savedInstanceState)

        btnToggleRun.setOnClickListener {
            toggleRun()
        }
        btnFinishRun.setOnClickListener {
            try {
                zoomToSeeWholeTrack()
                endRunAndSaveToDb()
            } catch (
                e: IllegalStateException
            ) {
                e.printStackTrace()
                stopRun()
            }

        }
        mapView.getMapAsync {
            map = it
            addAllPolyLines()
            moveCameraToUserLocation()
        }
        subscribeToObservers()

    }

    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner){
            updateTracking(it)
        }
        TrackingService.pathPoints.observe(viewLifecycleOwner){
            pathPoints = it
            addLatestPolyline()
            moveCameraToUserLocation()
        }
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner){
            currentTimeMillis = it
            val formattedTime = TimeFormatterUtility.getFormattedStopWatchTime(ms = currentTimeMillis, isMillisIncluded = true)
            tvTimer.text = formattedTime
        }
    }
    private fun moveCameraToUserLocation(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun updateTracking(isTracking : Boolean){
        this.isTracking = isTracking
        if(!isTracking){
            btnToggleRun.text = "START"
            btnFinishRun.visibility = View.VISIBLE
        }
        else
        {
            btnToggleRun.text = "STOP"
            btnFinishRun.visibility = View.GONE
            menu?.getItem(0)?.isVisible = true
        }
    }

    private fun toggleRun(){
        if(isTracking){
            sendCommandToTrackingService(ACTION_PAUSE_SERVICE)
            menu?.getItem(0)?.isVisible = true
        }
        else{
            sendCommandToTrackingService(ACTION_START_OR_RESUME_SERVICE)
        }
    }
    private fun addAllPolyLines(){
        for(polyline in pathPoints){
            val polyLineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polyLineOptions)
        }
    }
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

    private fun sendCommandToTrackingService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
//            sending intent to the service
            requireContext().startService(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeMillis > 0L) {
            menu.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()

            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the current run and delete all its data")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton(
                "Yes"
            ) { dialog: DialogInterface, which: Int ->

                stopRun()

            }.setNegativeButton(
                "No"
            ) { dialog, which ->
                dialog.cancel()
            }.create()
        dialog.show()
    }

    private fun stopRun() {
        sendCommandToTrackingService(ACTION_STOP_SERVICE)
        val action = TrackingFragmentDirections.actionTrackingFragmentToRunFragment()
        findNavController().navigate(action)
    }
    private fun  zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()

        for (polyline in pathPoints){
            for(pos in polyline){
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(2000)
            map?.snapshot {
                var distanceInMeter = 0
                for (polyline in pathPoints) {
                    distanceInMeter += calculatePolyline(polyline).toInt()
                }
                val avgSpeed =
                    round((distanceInMeter / 1000f) / (currentTimeMillis / 1000f / 60f / 60f) * 10) / 10f

                val dateTimeStamp = Calendar.getInstance().timeInMillis
                val caloriesBurned = ((distanceInMeter / 1000f) * weight).toInt()
                val run = Run(
                    img = it,
                    timestamp = dateTimeStamp,
                    avgSpeedInKM = avgSpeed,
                    timeInMillis = currentTimeMillis,
                    caloriesBurned = caloriesBurned
                )
                viewModel.insertRun(run)

                Snackbar.make(
                    requireActivity().findViewById(R.id.rootView),
                    "Run saved successfully",
                    Snackbar.LENGTH_LONG
                ).show()
            }
            stopRun()
        }
    }

    private fun calculatePolyline(polyline: Polyline):Float{
        var distance = 0f
        for(i in 0..polyline.size - 2){
            val pos1 = polyline[i]
            val pos2 = polyline[i+1]

            val result = FloatArray(1)

            Location.distanceBetween(
                pos1.latitude,
                pos2.longitude,
                pos2.latitude,
                pos2.longitude,
                result
            )
            distance += result[0]

        }
        return distance
    }
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        addAllPolyLines()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()

    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        addAllPolyLines()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        mapView?.onDestroy()
        super.onDestroy()

    }
}