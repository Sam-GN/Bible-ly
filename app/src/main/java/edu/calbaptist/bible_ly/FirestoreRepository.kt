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


    fun getNotesQuery(): Query {
        //.limit(10)
        return firestoreDB.collection("Note").whereEqualTo("user",MainActivity.user)
            .orderBy("verseChapter").orderBy("verseNum")
    }
    fun getNotesShareQuery(classID:String): Query {
        //.limit(10)
        return firestoreDB.collection("Note").whereEqualTo("clss",classID)
            .orderBy("verseChapter").orderBy("verseNum")
    }
    fun saveNote(isNew:Boolean,book: String,verseNum: String,verseChapter: String,verseText: String,noteText: String,noteTitle: String,notePath:String,type:String,clssPath:String):Boolean{
        if(isNew) {
            var ref = firestoreDB.collection("Note").document()
            var note = Note(ref.path,Date(),MainActivity.user,book, verseNum, verseChapter, verseText, noteText,noteTitle,
                ArrayList<Response>(),type,clssPath
            )

            ref.set(note)

        }else{
            var ref =firestoreDB.document(notePath).get().addOnSuccessListener {
                var note = it.toObject(Note::class.java)
                note!!.noteTitle= noteTitle
                note!!.noteText= noteText
                note!!.type= type
                note!!.clss= clssPath

//                var note = Note(notePath,MainActivity.user,book, verseNum, verseChapter, verseText, noteText,noteTitle,)
                firestoreDB.document(notePath).set(note!!)
            }

        }
        return true
    }
    fun deleteNote(notePath:String){
        var ref = firestoreDB.document(notePath)
        ref.delete()
    }
    fun getClassesList(callback: (Map<String, String>) -> Unit){
        var list = mutableMapOf<String,String>()
        firestoreDB.document("User/"+MainActivity.user.email).collection("classes")
            .get().addOnSuccessListener {
            if (it != null) {
                it.forEach { clss -> list.put(clss["classID"].toString(),clss["name"].toString()) }
                firestoreDB.collection("Class").whereEqualTo("teacher.email",MainActivity.user.email)
                    .get().addOnSuccessListener { it2 ->
                        it2.forEach { clss -> list.put(clss["classID"].toString(),clss["name"].toString()) }
                        callback( list)
                    }

            }
        }
    }
    fun getNote(notePath:String,callback: (Note) -> Unit){
        FirebaseFirestore.getInstance().document(notePath)
            .get().addOnSuccessListener {
                if (it != null) {

                   var note = it.toObject(Note::class.java)
                    callback( note!!)
                }
            }
    }
}