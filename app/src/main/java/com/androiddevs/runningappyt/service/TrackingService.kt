package com.androiddevs.runningappyt.service

import android.annotation.SuppressLint
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
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.other.Constants
import com.androiddevs.runningappyt.other.Constants.ACTION_PAUSE_SERVICE
import com.androiddevs.runningappyt.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.androiddevs.runningappyt.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.androiddevs.runningappyt.other.Constants.ACTION_STOP_SERVICE
import com.androiddevs.runningappyt.other.Constants.LOCATION_FASTEST_INTERVAL
import com.androiddevs.runningappyt.other.Constants.LOCATION_UPDATE_INTERVAL
import com.androiddevs.runningappyt.other.Constants.NOTIFICATION_CHANNEL_ID
import com.androiddevs.runningappyt.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.androiddevs.runningappyt.other.Constants.NOTIFICATION_ID
import com.androiddevs.runningappyt.other.TrackingPermissionUtility
import com.androiddevs.runningappyt.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

typealias  Polyline = MutableList<LatLng>
typealias  PolyLineList = MutableList<Polyline>

class TrackingService : LifecycleService() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var isFirstRun = true

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<PolyLineList>()
    }

    private fun postInitialValues() {
//        initially is tracking false
        isTracking.postValue(false)
//        initially there is no tracking data
        pathPoints.postValue(mutableListOf(mutableListOf()))
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this){
            updateLocationTracking(it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {

                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    }
                    else{
                        Timber.e("service resumed")
                        startForegroundService()
                    }


                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.e("service  paused")
                    pauseTracking()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.e("service stopped")

                }
            }

        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseTracking() {
        isTracking.postValue(false)
    }

    // adding polyline point as the last of the pathPoints
    fun addPathPoint(location: Location?) {
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            pathPoints.value?.let { polyLineList ->
                try {
                    val polyline: Polyline = polyLineList.last()
                    polyline.add(position)

                    pathPoints.postValue(polyLineList)
                } catch (
                    e: NoSuchElementException
                ) {
                    addEmptyPolyline()
                    val polyline: Polyline = polyLineList.last()

                    polyline.add(position)

                    pathPoints.postValue(polyLineList)
                }


            }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            if (isTracking.value == true) {
                p0?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.e("NEW LOCATION ${location.toString()} ")
                    }
                }
            }
        }
    }

    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingPermissionUtility.hasLocationPermission(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = LOCATION_FASTEST_INTERVAL
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
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

    private fun addEmptyPolyline() = pathPoints.value?.apply {
//        inside polylineList adding another list of polyline co-ordinates
        val polyLineList: PolyLineList = this

        val polyline = mutableListOf<LatLng>()

        polyLineList.add(polyline)

        pathPoints.postValue(this)
    } ?:
//    if the polyline list is null then initializing list of list
    pathPoints.postValue(mutableListOf<Polyline>())

    private fun startForegroundService() {
        addEmptyPolyline()
        isTracking.postValue(true)
        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(
            NOTIFICATION_ID,
            notificationBuilder.build()
        )
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getMainActivityPendingIntent(): PendingIntent? {
        return PendingIntent.getActivity(
            this,
            0,
            Intent(this,MainActivity::class.java).also {
                it.action = ACTION_SHOW_TRACKING_FRAGMENT
            },
            FLAG_UPDATE_CURRENT
        )
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager : NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}