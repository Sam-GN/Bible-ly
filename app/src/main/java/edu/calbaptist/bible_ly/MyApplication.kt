package edu.calbaptist.bible_ly

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

/**
 * Created by Manu on 10/30/2017.
 */
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        VolleySingleton.initConfi(this)




    }


}