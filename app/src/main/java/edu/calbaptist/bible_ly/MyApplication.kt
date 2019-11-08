package edu.calbaptist.bible_ly

import android.app.Application
/**
 * Created by Manu on 10/30/2017.
 */
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        VolleySingleton.initConfi(this)
    }
}