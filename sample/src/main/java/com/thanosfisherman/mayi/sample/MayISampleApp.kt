package com.thanosfisherman.mayi.sample

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

class MayISampleApp : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}