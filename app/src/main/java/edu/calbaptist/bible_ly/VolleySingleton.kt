package edu.calbaptist.bible_ly


import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley


object VolleySingleton{
    private lateinit var context: Context
    val requestQueque : RequestQueue by lazy {
        Volley.newRequestQueue(context)
    }

    fun initConfi(context:Context){
        this.context =context.applicationContext
    }
}

