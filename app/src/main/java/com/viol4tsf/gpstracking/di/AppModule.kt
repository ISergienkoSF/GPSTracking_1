package com.viol4tsf.gpstracking.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.viol4tsf.gpstracking.other.Constants.RUNNING_DB_NAME
import com.viol4tsf.gpstracking.db.RunningDatabase
import com.viol4tsf.gpstracking.other.Constants.KEY_CALORIES
import com.viol4tsf.gpstracking.other.Constants.KEY_DISTANCE
import com.viol4tsf.gpstracking.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.viol4tsf.gpstracking.other.Constants.KEY_NAME
import com.viol4tsf.gpstracking.other.Constants.KEY_WEIGHT
import com.viol4tsf.gpstracking.other.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

//СИНГЛТОН для объявления зависимостей БД, все это существует, пока запущено приложение, затем уничтожается
@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    //построениие БД с созданием единственного экземпляра
    @Singleton
    @Provides
    fun provideRunningDatabase(
        //создание контекста для создания БД, ведь по умолчанию он недоступн в модуле
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(app, RunningDatabase::class.java, RUNNING_DB_NAME).build()

    //предоставление объекта Dao
    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()

    //предоставление настроек
    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    //предоставление имени. ?: НУЖНО ВСЕГДА ПИСАТЬ СО СТРОКАМИ
    @Singleton
    @Provides
    fun provideName(sharedPreferences: SharedPreferences) = sharedPreferences.getString(KEY_NAME, "") ?:""

    //предоставление веса
    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences) = sharedPreferences.getFloat(KEY_WEIGHT, 65f)

    @Singleton
    @Provides
    fun provideDistance(sharedPreferences: SharedPreferences) = sharedPreferences.getLong(
        KEY_DISTANCE, 2000L)

    @Singleton
    @Provides
    fun provideCalories(sharedPreferences: SharedPreferences) = sharedPreferences.getInt(
        KEY_CALORIES, 100)

    //предоставление первого запуска
    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPreferences: SharedPreferences) =
        sharedPreferences.getBoolean(KEY_FIRST_TIME_TOGGLE, true)
}