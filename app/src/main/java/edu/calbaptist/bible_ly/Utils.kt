package edu.calbaptist.bible_ly

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.util.Log
import android.widget.*
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.content_class_single.*
import java.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
fun getUser(path:String,callback: (User) -> Unit){
    FirebaseFirestore.getInstance().document(path!!).get().addOnSuccessListener {
        if (it != null) {
            callback( it.toObject(User::class.java) as User)
        }
    }
}
fun getClass(path:String,callback: (Class) -> Unit){
    FirebaseFirestore.getInstance().document(path!!).get().addOnSuccessListener {
        if (it != null) {
            callback( it.toObject(Class::class.java) as Class)
        }
    }
}
fun getEvent(path:String,callback: (Event) -> Unit){
    FirebaseFirestore.getInstance().document(path!!).get().addOnSuccessListener {
        if (it != null) {
            callback( it.toObject(Event::class.java) as Event)
        }
    }
}
fun getVerse(path:String,callback: (Verse) -> Unit){
    FirebaseFirestore.getInstance().document(path!!).get().addOnSuccessListener {
        if (it != null) {
            callback( it.toObject(Verse::class.java) as Verse)
        }
    }
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
            Log.e("BoardAdapter",e.message)
            this.setImageResource(R.mipmap.ic_launcher2_round)
        }

    } else {
        this.setImageResource(R.mipmap.ic_launcher2_round)
    }
}
fun ImageView.setGlide(imagePath: String){
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
            Log.e("BoardAdapter",e.message)
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
fun sendNotification(topic:String,title:String,message:String,context: Context ) {
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

        notification.put("to", TOPIC)
        notification.put("data", notifcationBody)
    } catch (e: JSONException) {
        Log.e(TAG, "onCreate: " + e.message )
    }
    var jsonObjectRequest  = object: JsonObjectRequest(
        Method.POST, FCM_API, notification,
        Response.Listener<JSONObject> { response ->
            Log.i(TAG, "onResponse: $response")
        },
        Response.ErrorListener {
            Toast.makeText(context, "That didn't work!", Toast.LENGTH_SHORT).show()
        })
    {
        override fun getHeaders(): MutableMap<String, String> {

            val headers = HashMap<String, String>()
            headers["Authorization"] = serverKey
            headers["Content-Type"] = contentType
            return headers
        }
    }
    // var jsonObjectRequest =  JsonObjectRequest("",notification,null,null)
    /* var jsonObjectRequest =  JsonObjectRequest("FCM_API", notification,
          Response.Listener<JSONObject>() {
             @Override
             void onResponse(JSONObject response) {
                 Log.i(TAG, "onResponse: " + response.toString());
                 edtTitle.setText("");
                 edtMessage.setText("");
             }
         },
         Response.ErrorListener() {
             @Override
             void onErrorResponse(VolleyError error) {
                 Toast.makeText(MainActivity.this, "Request error", Toast.LENGTH_LONG).show();
                 Log.i(TAG, "onErrorResponse: Didn't work");
             }
         }){
         @Override
         public Map<String, String> getHeaders() throws AuthFailureError {
         Map<String, String> params = new HashMap<>();
         params.put("Authorization", serverKey);
         params.put("Content-Type", contentType);
         return params;
     }
     };*/
    VolleySingleton.requestQueque.add(jsonObjectRequest)
}
