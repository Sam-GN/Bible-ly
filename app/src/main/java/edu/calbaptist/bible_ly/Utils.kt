package edu.calbaptist.bible_ly

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.content_class_single.*
import java.util.*


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