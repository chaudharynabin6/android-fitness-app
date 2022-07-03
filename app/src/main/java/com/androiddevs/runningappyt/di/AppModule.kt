package com.androiddevs.runningappyt.di

import android.content.Context
import androidx.room.Room
import com.androiddevs.runningappyt.db.RunDAO
import com.androiddevs.runningappyt.db.RunningDatabase
import com.androiddevs.runningappyt.other.Constants.RUNNING_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRunningDatabase(
        @ApplicationContext context: Context
    ) : RunningDatabase = Room.databaseBuilder(
        context,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun providesRunDao(
        db : RunningDatabase
    ) : RunDAO = db.dao


}