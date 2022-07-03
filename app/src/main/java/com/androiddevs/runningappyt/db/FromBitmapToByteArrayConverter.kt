package com.androiddevs.runningappyt.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter

class FromBitmapToByteArrayConverter {

    @TypeConverter
    fun fromByteArrayToBitmap(bytes : ByteArray) : Bitmap {
        return BitmapFactory.decodeByteArray(bytes,0, bytes.size)
    }
}