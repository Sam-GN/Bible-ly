package edu.calbaptist.bible_ly


import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import edu.calbaptist.bible_ly.activity.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String?) {
        var d = p0
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        if(remoteMessage!!.data["topic"]!!.startsWith("SendToUser_")){
            if(remoteMessage!!.data["param1"] != MainActivity.currentNoteID)
                showNotification(this,remoteMessage!!.data["title"].toString(),remoteMessage!!.data["message"].toString(),true,remoteMessage!!.data["param1"]!!,"")
        }
        else {
            var classID = "Class/" + remoteMessage!!.data["topic"]
            FirebaseFirestore.getInstance()
                .collection("User").document(MainActivity.user.email).collection("classes")
                .whereEqualTo("classID", classID).get().addOnSuccessListener { document ->
                    if (document.isEmpty) {
                        FirebaseMessaging.getInstance()
                            .unsubscribeFromTopic(remoteMessage!!.data["topic"])
                    } else {
                        showNotification(this,remoteMessage!!.data["title"].toString(),remoteMessage!!.data["message"].toString(),false,remoteMessage!!.data["param1"]!!,classID)
                    }
                }
        }

    }
}