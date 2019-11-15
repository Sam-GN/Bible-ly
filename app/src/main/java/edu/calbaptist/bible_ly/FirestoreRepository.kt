package edu.calbaptist.bible_ly

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.event_detailed_fragment.view.*
import kotlinx.android.synthetic.main.note_detailed_fragment.view.*
import java.util.*
import kotlin.collections.ArrayList

class FirestoreRepository {

    val TAG = "FIREBASE_REPOSITORY"
    var firestoreDB = FirebaseFirestore.getInstance()
    var user = FirebaseAuth.getInstance().currentUser

    // get saved addresses from firebase
    fun getVersesQuery(book:String,chapter:String): Query {
        //.limit(10)
        return firestoreDB.collection("KingJamesVersion")
            .whereEqualTo("book",book).whereEqualTo("chapter",chapter)
            .orderBy("verse")
    }
    fun saveNote(isNew:Boolean,book: String,verseNum: String,verseChapter: String,verseText: String,noteText: String,noteTitle: String,notePath:String,type:String,clssPath:String){
        if(isNew) {
            var ref = firestoreDB.collection("Note").document()
            var note = Note(ref.path,MainActivity.user,book, verseNum, verseChapter, verseText, noteText,noteTitle,
                ArrayList<Response>(),type,clssPath
            )

            ref.set(note)

        }else{
//            var ref =firestoreDB.document(notePath)
//            var note = Note(notePath,MainActivity.user,book, verseNum, verseChapter, verseText, noteText,noteTitle,)
//            ref.set(note)
        }
    }
    fun getClassesList(callback: (Map<String, String>) -> Unit){
        var list = mutableMapOf<String,String>()
        FirebaseFirestore.getInstance().document("User/"+MainActivity.user.email).collection("classes")
            .get().addOnSuccessListener {
            if (it != null) {
                it.forEach { clss -> list.put(clss["classID"].toString(),clss["name"].toString()) }
                callback( list)
            }
        }
    }
}