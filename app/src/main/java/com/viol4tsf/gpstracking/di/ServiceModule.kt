package com.viol4tsf.gpstracking.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.viol4tsf.gpstracking.R
import com.viol4tsf.gpstracking.other.Constants
import com.viol4tsf.gpstracking.ui.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

//внедрение зависимостей для сервиса
@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    //единственный экземпляр клиента во время использования сервиса
    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(@ApplicationContext app: Context) =
        FusedLocationProviderClient(app)

    //интент для ожидания нажатия на уведомление
    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(@ApplicationContext app: Context) =
        PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    //построение простого уведомления
    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        //отмена скрытия уведомления при нажатии на него
        .setAutoCancel(false)
        //уведомление нельзя смахнуть
        .setOngoing(true)
        //установка маленького значка
        .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
        .setContentTitle("GPS Tracking")
        .setContentText("00:00:00")
        //подключение ожидающего интента
        .setContentIntent(pendingIntent)
}