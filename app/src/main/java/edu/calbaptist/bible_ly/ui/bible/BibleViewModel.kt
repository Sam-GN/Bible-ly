package edu.calbaptist.bible_ly.ui.bible

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import edu.calbaptist.bible_ly.*

class BibleViewModel : ViewModel() {

//    private val _text = MutableLiveData<String>().apply {
//        value = "This is bible Fragment"
//    }
//    val text: LiveData<String> = _text
    val TAG = "BibleViewModel"
    var firebaseRepository = FirestoreRepository()
    var books : MutableLiveData<List<BibleKey>> = MutableLiveData()
    var numOfchapters : MutableLiveData<Int> = MutableLiveData()
    var verses : MutableLiveData<List<Verse>> = MutableLiveData()
    var notes : MutableLiveData<List<NoteCardViewItem>> = MutableLiveData()
    var bible : MutableLiveData<Bible> = MutableLiveData()
    // get realtime updates from firebase regarding saved addresses


    fun getBibleKeys(): LiveData<List<BibleKey>>{
        firebaseRepository.getBibleKeysQuery().addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                books.value = null
                return@EventListener
            }

            var list : MutableList<BibleKey> = mutableListOf()
            for (doc in value!!) {
                var item = doc.toObject(BibleKey::class.java)
                list.add(item)
            }

            books.value = list
        })

        return books
    }

    fun getBibleBook(book:String): LiveData<Bible> {
        firebaseRepository.getBookQuery(book).addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                bible.value = null
                verses.value = null
                return@EventListener
            }

            var list : MutableList<Verse> = mutableListOf()
            var numOfChapters : Int = 0

            for (doc in value!!) {
                var item = doc.toObject(Verse::class.java)

                if (!list.any { x -> x.chapter == item.chapter }) {
                    numOfChapters++
                }

                list.add(item)
            }

            Log.d(TAG, "chapter size: " + numOfChapters.toString())
            bible.value = Bible(numOfChapters, list)
            verses.value = list
        })

        return bible
    }

    fun getVerses(book:String, chapter:String): LiveData<List<Verse>>{
        firebaseRepository.getVersesQuery(book,chapter).addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
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
            Log.d(TAG, "verses size: " + list.size.toString())
            verses.value = list.sortedBy { a->a.getVerseAsInt() }
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