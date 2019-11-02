package edu.calbaptist.bible_ly

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.widget.Button
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.content_class_single.*
import java.util.*
import android.widget.DatePicker
import android.widget.TextView
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
