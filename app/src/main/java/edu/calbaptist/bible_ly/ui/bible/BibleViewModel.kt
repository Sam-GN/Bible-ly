package edu.calbaptist.bible_ly.ui.bible

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import edu.calbaptist.bible_ly.*
import edu.calbaptist.bible_ly.activity.MainActivity

class BibleViewModel : ViewModel() {
    private var firebaseRepository = FirestoreRepository()
    private var books : MutableLiveData<List<BibleKey>> = MutableLiveData()
    private var verses : MutableLiveData<List<Verse>> = MutableLiveData()
    private var notes : MutableLiveData<List<NoteCardViewItem>> = MutableLiveData()


    fun getBibleKeys(): LiveData<List<BibleKey>>{
        firebaseRepository.getBibleKeysQuery().addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                books.value = null
                return@EventListener
            }
            val list : MutableList<BibleKey> = mutableListOf()
            for (doc in value!!) {
                val item = doc.toObject(BibleKey::class.java)
                list.add(item)
            }
            books.value = list
        })
        return books
    }
    fun getVerses(book:String, chapter:String): LiveData<List<Verse>>{
        firebaseRepository.getVersesQuery(book,chapter).addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                verses.value = null
                return@EventListener
            }
            val list : MutableList<Verse> = mutableListOf()
            for (doc in value!!) {
                val item = doc.toObject(Verse::class.java)
                list.add(item)
            }
            verses.value = list.sortedBy { a->a.getVerseAsInt() }
        })
        return verses
    }
    fun getNotes(): LiveData<List<NoteCardViewItem>> {
        firebaseRepository.getNotesQuery()
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    notes.value = null
                    return@EventListener
                }
                updateNoteLiveData(value)
            })
        return notes
    }
    private fun updateNoteLiveData(value:QuerySnapshot?) {
        val list: MutableList<NoteCardViewItem> = mutableListOf()
        val sharedMap: MutableMap<String, MutableList<NoteCardViewItem>> = mutableMapOf()
        var finalList: MutableList<NoteCardViewItem>
        for (doc in value!!) {
            val item = doc.toObject(NoteCardViewItem::class.java)
            list.add(item)
        }
        firebaseRepository.getClassesList {

            it.forEach { aa ->
                firebaseRepository.getNotesShareQuery(aa.key)
                    .addSnapshotListener(EventListener<QuerySnapshot> { value2, e ->
                        if (e != null) {
                            notes.value = null
                            return@EventListener
                        }
                        sharedMap[aa.key] = mutableListOf()
                        for (doc in value2!!) {
                            val item = doc.toObject(NoteCardViewItem::class.java)
                            if (item.user!!.email != (MainActivity.user.email)) {
                                sharedMap[aa.key]!!.add(item)
                            }
                        }
                        finalList = mutableListOf()
                        list.forEach { a -> finalList.add(a) }
                        for (a in sharedMap.keys) {
                            for (b in sharedMap[a]!!) {
                                finalList.add(b)
                            }
                        }
                        notes.value = finalList
                    })
            }
        }
        notes.value = list
    }
}