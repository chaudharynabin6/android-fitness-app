package com.androiddevs.runningappyt.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Run::class],
    version = 1
)

@TypeConverters(
    FromByteArrayToBitmapConverter::class,
    FromBitmapToByteArrayConverter::class
)
abstract class RunningDatabase : RoomDatabase() {
    abstract val dao : RunDAO
}