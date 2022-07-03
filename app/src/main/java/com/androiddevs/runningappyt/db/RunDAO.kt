package com.androiddevs.runningappyt.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query(
        """
            select * from running_table order by timestamp desc
        """
    )
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query(
        """
            select * from running_table order by timeInMillis desc
        """
    )
    fun getAllRunsSortedByTimeMillis(): LiveData<List<Run>>

    @Query(
        """
            select * from running_table order by avgSpeedInKM desc
        """
    )
    fun getAllRunsSortedByTimeAvgSpeedInKM(): LiveData<List<Run>>

    @Query(
        """
            select * from running_table order by distanceInMeters desc
        """
    )
    fun getAllRunsSortedByDistanceInMeter(): LiveData<List<Run>>

    @Query(
        """
            select * from running_table order by caloriesBurned desc
        """
    )
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    @Query(
        """
            select sum(timeInMillis) from running_table
        """
    )
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query(
        """
            select sum(caloriesBurned) from running_table
        """
    )
    fun getTotalCaloriesBurned(): LiveData<Int>

    @Query(
        """
            select sum(distanceInMeters) from running_table
        """
    )
    fun getTotalDistanceInMeters(): LiveData<Int>


    @Query(
        """
            select avg(avgSpeedInKM) from running_table
        """
    )
    fun getTotalAvgSpeed(): LiveData<Float>
}

