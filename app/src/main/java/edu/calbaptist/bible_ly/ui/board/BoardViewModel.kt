package edu.calbaptist.bible_ly.ui.board

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import edu.calbaptist.bible_ly.BoardRecycleViewItem
import edu.calbaptist.bible_ly.Event
import edu.calbaptist.bible_ly.Notification
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel : ViewModel() {


    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val sdf = SimpleDateFormat("MMM,dd HH:mm")

    // val events = MutableLiveData<MutableList<Event>>()
    val boardRecycleViewItem = mutableListOf<BoardRecycleViewItem>()
    init {

        for (i in 0 until 5) {
            val event = Notification()
            event.id = i
            event.createdDate = getDaysAgo(i)
            event.title = "Noti #$i"
            event.description ="this is fun #$i"


            val temp = BoardRecycleViewItem(event.id, event.title,sdf.format(event.createdDate),
                event.description,1)

            boardRecycleViewItem+=(temp)
        }
        boardRecycleViewItem.sortBy { a->a.date }
        /*for (i in 0 until 5) {
            val event = Event()
            event.eventID = i
            event.endDate = Date()
            event.startDate = getDaysAgo(i)
            event.repeat = 0

            event.name = "Event #$i"
            event.description ="this is fun #$i"
          //  saveEvent(event )
         *//*   val temp = BoardRecycleViewItem(event.eventID, event.name,"${sdf.format(event.startDate)} - ${sdf.format(event.endDate)}",
                event.className,0)*//*

          //  boardRecycleViewItem+=(temp)
        }*/




    }
    val text: LiveData<String> = _text
}

fun getDaysAgo(daysAgo: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)

    return calendar.time
}

fun saveEvent (event:Event){
   val fireStore = FirebaseFirestore.getInstance()
    val batch = fireStore.batch()

        val eventRef = fireStore.collection("Event").document()


        // Add restaurant
        batch.set(eventRef, event)




    batch.commit().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("BoardViewModel", "Write batch succeeded.")
        } else {
            Log.w("BoardViewModel", "write batch failed.", task.exception)
        }
    }
}
