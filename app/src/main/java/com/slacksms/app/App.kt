package com.slacksms.app

import android.app.Application
import android.content.Context


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        application = this
    }

    companion object {
        private lateinit var application: App
        const val defaultSmsAppKey = "defaultSmsApp"

        fun getContext(): Context {
            return application
        }
    }
}
