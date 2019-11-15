package edu.calbaptist.bible_ly.ui.bible

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import edu.calbaptist.bible_ly.FirestoreRepository
import edu.calbaptist.bible_ly.Verse

class BibleViewModel : ViewModel() {

//    private val _text = MutableLiveData<String>().apply {
//        value = "This is bible Fragment"
//    }
//    val text: LiveData<String> = _text
val TAG = "FIRESTORE_VIEW_MODEL"
    var firebaseRepository = FirestoreRepository()
    var verses : MutableLiveData<List<Verse>> = MutableLiveData()
    // get realtime updates from firebase regarding saved addresses


    fun getVerses(chapter:String): LiveData<List<Verse>>{
        firebaseRepository.getVersesQuery("1",chapter).addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                verses.value = null
                return@EventListener
            }

            var savedAddressList : MutableList<Verse> = mutableListOf()
            for (doc in value!!) {
                var addressItem = doc.toObject(Verse::class.java)
                savedAddressList.add(addressItem)
            }
            verses.value = savedAddressList
        })

        return verses
    }
}