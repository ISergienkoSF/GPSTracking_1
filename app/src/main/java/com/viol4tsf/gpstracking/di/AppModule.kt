package com.viol4tsf.gpstracking.di

import android.content.Context
import androidx.room.Room
import com.viol4tsf.gpstracking.const.Constants.RUNNING_DB_NAME
import com.viol4tsf.gpstracking.db.RunningDatabase
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
}