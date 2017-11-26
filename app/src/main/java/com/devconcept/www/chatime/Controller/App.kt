package com.devconcept.www.chatime.Controller

import android.app.Application
import com.devconcept.www.chatime.Utilities.SharedPrefs

/**
 * Created by harry on 26/11/2017.
 */
class App: Application() {

    companion object {
        lateinit var prefs: SharedPrefs
    }

    override fun onCreate() {
        prefs = SharedPrefs(applicationContext)
        super.onCreate()
    }
}