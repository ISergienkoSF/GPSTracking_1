package com.viol4tsf.gpstracking

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

//класс для сообщения, что будут внедрены зависимости с помощью Dagger hilt
@HiltAndroidApp
class BaseApp: Application() {

    override fun onCreate() {
        super.onCreate()
        //ведение журнала логов
        Timber.plant(Timber.DebugTree())
    }
}