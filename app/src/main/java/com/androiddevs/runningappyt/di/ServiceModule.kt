package com.androiddevs.runningappyt.di

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.other.Constants
import com.androiddevs.runningappyt.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @Provides
    @ServiceScoped
    fun providesFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return FusedLocationProviderClient(context)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    @Provides
    @ServiceScoped
    fun providesMainActivityPendingIntent(
        @ApplicationContext context: Context
    ): PendingIntent? {
        return PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).also {
                it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
            },
            PendingIntent.FLAG_IMMUTABLE  or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @Provides
    @ServiceScoped
    fun provideBaseNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent?
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(
            context,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(pendingIntent)
    }
}