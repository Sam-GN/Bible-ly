package edu.calbaptist.bible_ly.ui.bible

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import edu.calbaptist.bible_ly.FirestoreRepository
import edu.calbaptist.bible_ly.MainActivity
import edu.calbaptist.bible_ly.NoteCardViewItem
import edu.calbaptist.bible_ly.Verse

class BibleViewModel : ViewModel() {

//    private val _text = MutableLiveData<String>().apply {
//        value = "This is bible Fragment"
//    }
//    val text: LiveData<String> = _text
    val TAG = "BibleViewModel"
    var firebaseRepository = FirestoreRepository()
    var verses : MutableLiveData<List<Verse>> = MutableLiveData()
    var notes : MutableLiveData<List<NoteCardViewItem>> = MutableLiveData()
    // get realtime updates from firebase regarding saved addresses


    fun getVerses(chapter:String): LiveData<List<Verse>>{
        firebaseRepository.getVersesQuery("1",chapter).addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                verses.value = null
                return@EventListener
            }

            var list : MutableList<Verse> = mutableListOf()
            for (doc in value!!) {
                var item = doc.toObject(Verse::class.java)
                list.add(item)
            }
            verses.value = list
        })

        return verses
    }
    fun getNotes(): LiveData<List<NoteCardViewItem>> {
        firebaseRepository.getNotesQuery()
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    notes.value = null
                    return@EventListener
                }

                updateNoteLiveData(value)

            })

        return notes
    }
    fun updateNoteLiveData(value:QuerySnapshot?){
        var list: MutableList<NoteCardViewItem> = mutableListOf()
        var sharedMap: MutableMap<String, MutableList<NoteCardViewItem> > = mutableMapOf()
        var finalList: MutableList<NoteCardViewItem> = mutableListOf()
        for (doc in value!!) {
            var item = doc.toObject(NoteCardViewItem::class.java)
            list.add(item)
        }
        firebaseRepository.getClassesList {

            it.forEach { aa ->
                firebaseRepository.getNotesShareQuery(aa.key)
                    .addSnapshotListener(EventListener<QuerySnapshot> { value2, e ->
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e)
                            notes.value = null
                            return@EventListener
                        }
                        //var list2: MutableList<NoteCardViewItem> = mutableListOf()
                        sharedMap[aa.key] = mutableListOf()
                        for (doc in value2!!) {
                            var item = doc.toObject(NoteCardViewItem::class.java)
                            if(item.user!!.email != (MainActivity.user.email)){
                                sharedMap[aa.key]!!.add(item)
                                   // list2.add(item)
                            }

                        }

                        finalList= mutableListOf()
                        list.forEach{a->finalList.add(a)}
                        for( a in sharedMap.keys){
                            for(b in sharedMap[a]!!){
                                finalList.add(b)
                            }
                        }
                        notes.value = finalList
                       // updateNoteLiveData(value)
                        //getNotes()
                    })
            }
        }


        notes.value = list
    }

}