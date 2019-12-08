package edu.calbaptist.bible_ly

import android.app.*
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import edu.calbaptist.bible_ly.activity.MainActivity
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat


fun getScaledBitmap (path: String, activity: Activity): Bitmap {
    val size = Point()
    activity.windowManager.defaultDisplay.getSize(size)
    return getScaledBitmap(path, size.x, size.y)
}

fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    var inSampleSize = 1
    if(srcHeight > destHeight || srcWidth > destWidth ){
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth
        val sampleScale = if(heightScale>widthScale) {
            heightScale
        } else {
            widthScale
        }
        inSampleSize = Math.round(sampleScale)
    }
    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize

    return BitmapFactory.decodeFile(path, options)
}


fun ImageButton.setGlide(imagePath: String){
    if(imagePath!= "") {
        try {
            Glide.with(this.context)

                .applyDefaultRequestOptions( RequestOptions()
                    .placeholder(R.mipmap.ic_launcher2_round)
                    .error(R.mipmap.ic_launcher2_round)
                )
                //.applyDefaultRequestOptions( RequestOptions())
                //.setDefaultRequestOptions(RequestOptions())
                .load(imagePath)
                // .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(30)))
                //.apply(RequestOptions.centerCropTransform())
                .apply(RequestOptions.circleCropTransform())

                .into(this)

        }catch (e: IOException){
            this.setImageResource(R.mipmap.ic_launcher2_round)
        }

    } else {
        this.setImageResource(R.mipmap.ic_launcher2_round)
    }
}
fun ImageView.setGlide(imagePath: String,isRound:Boolean){
    if(imagePath!= "") {
        try {
            Glide.with(this.context)

                .applyDefaultRequestOptions( RequestOptions()
                    .placeholder(R.mipmap.ic_launcher2_round)
                    .error(R.mipmap.ic_launcher2_round)
                )
                //.applyDefaultRequestOptions( RequestOptions())
                //.setDefaultRequestOptions(RequestOptions())
                .load(imagePath)
                // .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(30)))
                //.apply(RequestOptions.centerCropTransform())
                .apply(if(isRound)RequestOptions.circleCropTransform()else RequestOptions.centerCropTransform())

                .into(this)


        }catch (e: IOException){
            this.setImageResource(R.mipmap.ic_launcher2_round)
        }

    } else {
        this.setImageResource(R.mipmap.ic_launcher2_round)
    }
}
fun getServerTimeStamp(callback: (Timestamp) -> Unit){


    var ref = FirebaseFirestore.getInstance().collection("timestamp").document("timestamp")
    ref.update("time" , FieldValue.serverTimestamp()).addOnCompleteListener {
        var ref2 = FirebaseFirestore.getInstance().collection("timestamp").document("timestamp")
        ref2.get()
            .addOnSuccessListener { document ->
                if (document != null) {

                    callback(document.get("time") as Timestamp)
                } else {
                    // Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                //  Log.d(TAG, "get failed with ", exception)
            }
    }


}
fun addDay(t:Timestamp,dayNum:Int): Date{
    var c = Calendar.getInstance()
    c.time = t.toDate()
    c.add(Calendar.DAY_OF_YEAR,dayNum)
    return c.time
}

fun extendToken(tokenTime:Timestamp, currentTime: Timestamp): Date{
    var ctoken = Calendar.getInstance()
    ctoken.time = tokenTime.toDate()

    var calCurrentTime = Calendar.getInstance()
    calCurrentTime.time = currentTime.toDate()

    var temporaryCalendar = ctoken

    if( calCurrentTime.before(temporaryCalendar.add(Calendar.HOUR,12))) {
        ctoken.add(Calendar.HOUR, 12)
    }

        return ctoken.time

}
/*fun getCalendarFromDatePicker(datePicker: DatePicker): Date {
    val day = datePicker.dayOfMonth
    val month = datePicker.month
    val year = datePicker.year

    val calendar = Calendar.getInstance()
    calendar.set(year, month, day)

    return calendar.time
}*/
fun DatePicker.getCalendarFromDatePicker(): Calendar {
    val day = this.dayOfMonth
    val month = this.month
    val year = this.year

    val calendar = Calendar.getInstance()
    calendar.set(year, month, day)

    return calendar
}
fun Date.toLocalDateString(hasTime:Boolean):String{
    return if(hasTime)
        java.text.DateFormat.getDateInstance().format(this)+SimpleDateFormat(" HH:mm").format(this)
    else
        java.text.DateFormat.getDateInstance().format(this)
}

fun getTime(btn: Button, context: Context,cal: Calendar){

    //val cal = btn.tag as Calendar



    val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)

        btn.tag = cal
        btn.text = cal.time.toLocalDateString(true)
    }
    TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()


}
fun shareIntent(context: Context,text:String,subject:String,title: String) {
    Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }.also { intent ->
        val chooserIntent = Intent.createChooser(intent, title)
        context.startActivity(chooserIntent)
    }
}
fun showNotification(context:Context, title: String, message: String, newComment:Boolean, param1:String, param2:String){
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val NOTIFICATION_CHANNEL_ID = "Nilesh_channel"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Your Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationChannel.description = "Description"
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
        notificationChannel.enableVibration(true)
        notificationManager.createNotificationChannel(notificationChannel)
    }


    // to diaplay notification in DND Mode
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel =
            notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
        channel.canBypassDnd()
    }


    val notificationBuilder =
        NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)

    notificationBuilder.setAutoCancel(true)
        .setColor(ContextCompat.getColor(context, R.color.colorAccent))
        .setContentTitle(title)
        //.setContentText(remoteMessage!!.getNotification()!!.getBody())
        .setContentText(message)
        .setDefaults(android.app.Notification.DEFAULT_ALL)
        .setWhen(System.currentTimeMillis())
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setAutoCancel(true)

    if(newComment) {
        // Create an Intent for the activity you want to start
        val resultIntent = Intent(context, MainActivity::class.java)
        resultIntent.putExtra("currentDestination", R.id.nav_bible)
        resultIntent.putExtra("noteID", param1)
        // Create the TaskStackBuilder
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        notificationBuilder.setContentIntent(resultPendingIntent)


    }
    else{
        val resultIntent = Intent(context, MainActivity::class.java)
        resultIntent.putExtra("currentDestination", R.id.nav_board)
        resultIntent.putExtra("eventID", param1)
        resultIntent.putExtra("classID", param2)
        // Create the TaskStackBuilder
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        notificationBuilder.setContentIntent(resultPendingIntent)
    }

    notificationManager.notify(1000, notificationBuilder.build())



}
fun sendNotification(topic:String,title:String,message:String,context: Context,param1:String ) {
    val TAG = "sendNotification"
    val FCM_API = "https://fcm.googleapis.com/fcm/send"
    val serverKey = "key=" + "AAAA_Z8c2FM:APA91bFTvaDRR7T0VmD2NVKvmkUfWF5yU3ZFDsXsVUZnYD7wvHSk1rV3iU82kDd625Q5PKZDgYCWXpdsLN0tRkZePw00iu7ToIpD2Ixh5xYS6ku4uWSVqBhQ4H-lNURQZ-xSB9mz5vnK"
    val contentType = "application/json"
    var TOPIC = "/topics/$topic" //topic must match with what the receiver subscribed to
//        var NOTIFICATION_TITLE = "test from client"
//        var NOTIFICATION_MESSAGE = "test from client mmm"

    var notification =  JSONObject()
    var notifcationBody =  JSONObject()
    try {
        notifcationBody.put("title", title)
        notifcationBody.put("message", message)
        notifcationBody.put("topic", topic)
        notifcationBody.put("param1", param1)

        notification.put("to", TOPIC)
        notification.put("data", notifcationBody)
    } catch (e: JSONException) {
       // Log.e(TAG, "onCreate: " + e.message )
    }
    var jsonObjectRequest  = object: JsonObjectRequest(
        Method.POST, FCM_API, notification,
        Response.Listener<JSONObject> { response ->
           // Log.i(TAG, "onResponse: $response")
        },
        Response.ErrorListener {
          //  Toast.makeText(context, "That didn't work!", Toast.LENGTH_SHORT).show()
        })
    {
        override fun getHeaders(): MutableMap<String, String> {

            val headers = HashMap<String, String>()
            headers["Authorization"] = serverKey
            headers["Content-Type"] = contentType
            return headers
        }
    }

    VolleySingleton.requestQueque.add(jsonObjectRequest)
}
fun getCurrentActivity(context:Context) :String{
    var am =  context.getSystemService(ACTIVITY_SERVICE) as (ActivityManager)
    var  taskInfo = am.getRunningTasks(1);
  //  Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.className)
    var componentInfo = taskInfo[0].topActivity
    return  componentInfo.className
}
fun Int.toDp(displayMetrics: DisplayMetrics) = toFloat().toDp(displayMetrics).toInt()
fun Float.toDp(displayMetrics: DisplayMetrics) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, displayMetrics)