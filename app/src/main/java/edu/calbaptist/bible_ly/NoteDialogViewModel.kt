package edu.calbaptist.bible_ly

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

class NoteDialogViewModel : ViewModel() {

//    private val _text = MutableLiveData<String>().apply {
//        value = "This is bible Fragment"
//    }
//    val text: LiveData<String> = _text
val TAG = "FIRESTORE_VIEW_MODEL"
    var firebaseRepository = FirestoreRepository()
    var comments : MutableLiveData<List<Comment>> = MutableLiveData()


    fun getComments(notePath:String): LiveData<List<Comment>>{
        firebaseRepository.getCommentsQuery(notePath).addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                comments.value = null
                return@EventListener
            }

            var list : MutableList<Comment> = mutableListOf()
            for (doc in value!!) {
                var item = doc.toObject(Comment::class.java)
                list.add(item)
            }
            comments.value = list
        })

        return comments
    }


}