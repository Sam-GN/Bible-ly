package edu.calbaptist.bible_ly.ui.notes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import edu.calbaptist.bible_ly.*

class NoteDialogViewModel : ViewModel() {


    var firebaseRepository = FirestoreRepository()
    var comments : MutableLiveData<List<Comment>> = MutableLiveData()


    fun getComments(notePath:String): LiveData<List<Comment>>{
        firebaseRepository.getCommentsQuery(notePath).addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
            if (e != null) {
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