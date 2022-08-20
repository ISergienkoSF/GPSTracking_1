package com.viol4tsf.gpstracking

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

//класс для сообщения, что будут внедрены зависимости с помощью Dagger hilt
@HiltAndroidApp
class BaseApp: Application() {

}