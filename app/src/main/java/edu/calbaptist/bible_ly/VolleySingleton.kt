package edu.calbaptist.bible_ly

import android.app.Application
import android.content.Context
import android.text.TextUtils
import com.android.volley.Request
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

/*class VolleySingleton private constructor(context: Context) {
        companion object {
            @Volatile
            private var INSTANCE: VolleySingleton? = null
            fun getInstance(context: Context) =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: VolleySingleton(context).also {
                        INSTANCE = it
                    }
                }
        }

        val requestQueue: RequestQueue by lazy {
            // applicationContext is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            Volley.newRequestQueue(context.applicationContext)
        }
        fun <T> addToRequestQueue(req: Request<T>) {
            requestQueue.add(req)
        }
    }*/
/*
class UsersDatabase : RoomDatabase() {

    companion object {

        @Volatile private var INSTANCE: UsersDatabase? = null

        fun getInstance(context: Context): UsersDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                UsersDatabase::class.java, "Sample.db")
                .build()
    }
}*/
