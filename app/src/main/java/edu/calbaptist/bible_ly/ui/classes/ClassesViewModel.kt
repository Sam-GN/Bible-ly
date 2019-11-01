package edu.calbaptist.bible_ly.ui.classes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.calbaptist.bible_ly.BoardRecycleViewItem
import edu.calbaptist.bible_ly.Class
import edu.calbaptist.bible_ly.Event
import edu.calbaptist.bible_ly.Notification
import edu.calbaptist.bible_ly.ui.board.getDaysAgo
import java.util.*

class ClassesViewModel : ViewModel() {

/*

    val classes = mutableListOf<Class>()
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
        for (i in 0 until 5) {
            val event = Event()
            event.eventID = i
            event.endDate = Date()
            event.startDate = getDaysAgo(i)
            event.repeat = 0

            event.name = "Event #$i"
            event.description ="this is fun #$i"
            //  saveEvent(event )
            val temp = BoardRecycleViewItem(event.eventID, event.name,"${sdf.format(event.startDate)} - ${sdf.format(event.endDate)}",
                event.className,0)

            boardRecycleViewItem+=(temp)
        }




    }
*/



}