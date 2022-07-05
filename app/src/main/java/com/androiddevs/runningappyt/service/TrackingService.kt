package com.androiddevs.runningappyt.service

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
import com.androiddevs.runningappyt.other.Constants.ACTION_PAUSE_SERVICE
import com.androiddevs.runningappyt.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.androiddevs.runningappyt.other.Constants.ACTION_STOP_SERVICE
import com.androiddevs.runningappyt.other.Constants.LOCATION_FASTEST_INTERVAL
import com.androiddevs.runningappyt.other.Constants.LOCATION_UPDATE_INTERVAL
import com.androiddevs.runningappyt.other.Constants.NOTIFICATION_CHANNEL_ID
import com.androiddevs.runningappyt.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.androiddevs.runningappyt.other.Constants.NOTIFICATION_ID
import com.androiddevs.runningappyt.other.Constants.TIMER_UPDATE_INTERVAl
import com.androiddevs.runningappyt.other.TimeFormatterUtility
import com.androiddevs.runningappyt.other.TrackingPermissionUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias  Polyline = MutableList<LatLng>
typealias  PolyLineList = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var currentNotificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    private var isFirstRun = true
    private var isServiceKilled = false

    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<PolyLineList>()
        val timeRunInMillis = MutableLiveData<Long>()
    }

    private fun postInitialValues() {
//        initially is tracking false
        isTracking.postValue(false)
//        initially there is no tracking data
        pathPoints.postValue(mutableListOf(mutableListOf()))

        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this){
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
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
                       startTimer()
                    }


                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.e("service  paused")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.e("service stopped")
                    killService()
                }
            }

        }
        return super.onStartCommand(intent, flags, startId)
    }
    private var isTimerEnable = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnable = true
        CoroutineScope(Dispatchers.Main).launch{
            while(isTracking.value == true){
//              time difference between the each start time and system tiem
                lapTime = System.currentTimeMillis() - timeStarted

//                 post the new lapTime
//                total run is
                timeRunInMillis.postValue(timeRun + lapTime)

                if(timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L){

                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)

                    lastSecondTimeStamp += 1000L
                }

            delay(TIMER_UPDATE_INTERVAl)
            }
//             updating total time only when
            timeRun += lapTime
        }
    }
    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnable = false
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
        startTimer()
        isTracking.postValue(true)
        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(
            NOTIFICATION_ID,
            baseNotificationBuilder.build()
        )
//        handle notification carefully
        timeRunInSeconds.observe(this) {
            if (!isServiceKilled) {
                val notification = currentNotificationBuilder
                    .setContentText(
                        TimeFormatterUtility.getFormattedStopWatchTime(it * 1000L)
                    )
                notificationManager.notify(
                    NOTIFICATION_ID,
                    notification.build()
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"

        val pendingIntent: PendingIntent = if (isTracking) {
            val pauseIntent = Intent(
                this,
                TrackingService::class.java
            ).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(
                this,
                TrackingService::class.java
            ).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

//         remove previous action buttons
        currentNotificationBuilder.javaClass.getField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if (!isServiceKilled) {

            currentNotificationBuilder = currentNotificationBuilder
                .addAction(
                    R.drawable.ic_pause_black_24dp,
                    notificationActionText,
                    pendingIntent
                )
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    private fun killService() {
        isServiceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }
}