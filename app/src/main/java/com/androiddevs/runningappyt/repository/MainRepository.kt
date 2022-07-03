package com.androiddevs.runningappyt.repository

import com.androiddevs.runningappyt.db.Run
import com.androiddevs.runningappyt.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val runDAO: RunDAO
) {

    suspend fun insertRun(run: Run) = runDAO.insertRun(run)

    suspend fun deleteRun(run: Run) = runDAO.deleteRun(run)

    fun getAllRunsSortedByDate() = runDAO.getAllRunsSortedByDate()

    fun getAllRunsSortedByTimeMillis() = runDAO.getAllRunsSortedByTimeMillis()

    fun getAllRunsSortedByTimeAvgSpeedInKM() = runDAO.getAllRunsSortedByTimeAvgSpeedInKM()

    fun getAllRunsSortedByDistanceInMeter() = runDAO.getAllRunsSortedByDistanceInMeter()

    fun getAllRunsSortedByCaloriesBurned() = runDAO.getAllRunsSortedByCaloriesBurned()

    fun getTotalCaloriesBurned() = runDAO.getTotalCaloriesBurned()

    fun getTotalAvgSpeed() = runDAO.getTotalAvgSpeed()

    fun getTotalTimeInMillis() = runDAO.getTotalTimeInMillis()

    fun getTotalDistanceInMeters() = runDAO.getTotalDistanceInMeters()

}