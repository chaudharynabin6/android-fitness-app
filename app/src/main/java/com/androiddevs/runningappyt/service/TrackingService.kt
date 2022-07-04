package com.androiddevs.runningappyt.service

import android.content.Intent
import androidx.lifecycle.LifecycleService
import com.androiddevs.runningappyt.other.Constants.ACTION_PAUSE_SERVICE
import com.androiddevs.runningappyt.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.androiddevs.runningappyt.other.Constants.ACTION_STOP_SERVICE
import timber.log.Timber

class TrackingService : LifecycleService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.e("service started or resumed")

                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.e("service  paused")

                }
                ACTION_STOP_SERVICE -> {
                    Timber.e("service stopped")

                }
            }

        }
        return super.onStartCommand(intent, flags, startId)
    }
}