package com.viol4tsf.gpstracking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.viol4tsf.gpstracking.db.RunDao
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}