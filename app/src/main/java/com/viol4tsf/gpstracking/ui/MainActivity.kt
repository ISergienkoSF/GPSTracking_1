package com.viol4tsf.gpstracking.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.viol4tsf.gpstracking.R

//@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    //@Inject
    //lateinit var runDao: RunDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Log.d("runDao", "RUNDAO: ${runDao.hashCode()}")
    }
}