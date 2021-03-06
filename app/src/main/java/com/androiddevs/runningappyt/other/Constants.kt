package com.androiddevs.runningappyt.other

import android.graphics.Color

object Constants {
    //    database
    const val RUNNING_DATABASE_NAME = "running.db"

    //    permission
    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    //    services
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

//    notification
    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "tracking"
    const val NOTIFICATION_ID  = 1
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

//    locations
    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val LOCATION_FASTEST_INTERVAL = 2000L

//    map
    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 15f

//    timer
    const val TIMER_UPDATE_INTERVAl = 50L

//    shared preference
    const val SHARED_PREFERENCE_NAME = "SHARED_PREFERENCE_NAME"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME  = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"
}