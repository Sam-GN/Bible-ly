package edu.calbaptist.bible_ly


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String?) {
        var d = p0
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        var classID = "Class/"+remoteMessage!!.data["topic"]
        FirebaseFirestore.getInstance()
            .collection("User").document(MainActivity.user.email).collection("classes")
            .whereEqualTo("classID",classID).get().addOnSuccessListener {document ->
                if(document.isEmpty){
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(remoteMessage!!.data["topic"])
                } else {
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val NOTIFICATION_CHANNEL_ID = "Nilesh_channel"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Your Notifications", NotificationManager.IMPORTANCE_HIGH)

                        notificationChannel.description = "Description"
                        notificationChannel.enableLights(true)
                        notificationChannel.lightColor = Color.RED
                        notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                        notificationChannel.enableVibration(true)
                        notificationManager.createNotificationChannel(notificationChannel)
                    }

                    // to diaplay notification in DND Mode
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
                        channel.canBypassDnd()
                    }

                    val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)

                    notificationBuilder.setAutoCancel(true)
                        .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .setContentTitle(remoteMessage!!.data["title"].toString())
                        //.setContentText(remoteMessage!!.getNotification()!!.getBody())
                        .setContentText(remoteMessage!!.data["message"].toString())
                        .setDefaults(android.app.Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setAutoCancel(true)


                    notificationManager.notify(1000, notificationBuilder.build())

                }

            }

    }
}