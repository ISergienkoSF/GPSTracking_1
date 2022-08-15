package com.viol4tsf.gpstracking.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class TypeConverters {

    @TypeConverter
   fun toBitmap(bytes: ByteArray): Bitmap {
       return BitmapFactory.decodeByteArray(bytes, 0, bytes.size) //вернуть массив байтов растрового декодирования
   }

    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray{
        val outputStream = ByteArrayOutputStream() // массив байтов для выходного потока
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)// сжатие растровых изображений и помещение в поток вывода
        return outputStream.toByteArray() //преобразование потока в массив байтов
    }
}