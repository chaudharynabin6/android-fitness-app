package com.androiddevs.runningappyt.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.androiddevs.runningappyt.db.RunDAO
import com.androiddevs.runningappyt.db.RunningDatabase
import com.androiddevs.runningappyt.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.androiddevs.runningappyt.other.Constants.KEY_NAME
import com.androiddevs.runningappyt.other.Constants.KEY_WEIGHT
import com.androiddevs.runningappyt.other.Constants.RUNNING_DATABASE_NAME
import com.androiddevs.runningappyt.other.Constants.SHARED_PREFERENCE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
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

    @Singleton
    @Provides
    fun providesSharedPreference(
        @ApplicationContext context : Context
    ): SharedPreferences {
        return context.getSharedPreferences(SHARED_PREFERENCE_NAME,Context.MODE_PRIVATE)

    }


    @Singleton
    @Provides
//    @Named(KEY_NAME)
    fun providesName(
        sharedPreferences: SharedPreferences
    ): String {
        return sharedPreferences.getString(KEY_NAME,"") ?: ""
    }


    @Singleton
    @Provides
//    @Named(KEY_WEIGHT)
    fun providesWeight(
        sharedPreferences: SharedPreferences
    ): Float {
        return sharedPreferences.getFloat(KEY_WEIGHT,80F)
    }


    @Singleton
    @Provides
//    @Named(KEY_FIRST_TIME_TOGGLE)
    fun providesIsFirstTime(
       @ApplicationContext context: Context
    ): Boolean {
        return context.getSharedPreferences(SHARED_PREFERENCE_NAME,Context.MODE_PRIVATE).getBoolean(KEY_FIRST_TIME_TOGGLE,false)
    }


}