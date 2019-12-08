package edu.calbaptist.bible_ly.ui.board

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import edu.calbaptist.bible_ly.*


class BorardViewModel : ViewModel() {

    val TAG = "BorardViewModel"
    var firebaseRepository = FirestoreRepository()
    var events : MutableLiveData<List<Event>> = MutableLiveData()


    fun getEvents(): LiveData<List<Event>>{
        firebaseRepository.getEventsQuery().addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                events.value = null
                return@EventListener
            }

            var list : MutableList<Event> = mutableListOf()
            firebaseRepository.getClassesList {classes ->


                for (doc in value!!) {
                    var event = doc.toObject(Event::class.java)
                    classes.forEach{ clss ->
                        if(clss.key == event.clss!!.classID) {
                            list.add(event)

                        }
                    }

                }
                events.value = list

            }

        })

        return events
    }

}

