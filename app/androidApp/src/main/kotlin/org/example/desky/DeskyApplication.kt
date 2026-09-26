package org.example.desky

import android.app.Application
import org.example.desky.core.di.coreModule
import org.example.desky.devices.di.devicesModule
import org.example.desky.home.di.homeModule
import org.example.desky.laptop.di.laptopModule
import org.example.desky.led_strip.di.ledModule
import org.example.desky.settings.di.settingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class DeskyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@DeskyApplication)
            modules(
                coreModule,
                homeModule,
                devicesModule,
                ledModule,
                settingsModule,
                laptopModule
            )
        }
    }
}
