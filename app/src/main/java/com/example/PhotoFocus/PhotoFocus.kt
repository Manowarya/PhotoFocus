package com.example.PhotoFocus

import android.app.Application
import com.yandex.metrica.YandexMetrica

import com.yandex.metrica.YandexMetricaConfig


class PhotoFocus : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = YandexMetricaConfig.newConfigBuilder("26f9c852-f158-4358-abe8-54688ef9718c").build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(this)
    }
}