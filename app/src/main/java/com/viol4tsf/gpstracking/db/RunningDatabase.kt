package com.viol4tsf.gpstracking.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Run::class], version = 1)
@TypeConverters(com.viol4tsf.gpstracking.db.TypeConverters::class)
abstract class RunningDatabase : RoomDatabase(){
    abstract fun getRunDao(): RunDao
}