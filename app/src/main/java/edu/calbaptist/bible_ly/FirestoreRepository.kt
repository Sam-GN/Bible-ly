package edu.calbaptist.bible_ly

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.dialogue_join_class.view.*
import kotlinx.android.synthetic.main.dialogue_new_class.view.*
import kotlinx.android.synthetic.main.event_detailed_fragment.view.*
import kotlinx.android.synthetic.main.note_detailed_fragment.view.*
import java.util.*
import kotlin.collections.ArrayList

class FirestoreRepository {

    val TAG = "FIREBASE_REPOSITORY"
    var firestoreDB = FirebaseFirestore.getInstance()
    var user = FirebaseAuth.getInstance().currentUser

    // get saved addresses from firebase
    fun getClassesAsStudentQuery(): Query {
        return firestoreDB.collection("User").document(MainActivity.user.email).collection("classes")
    }
    fun getClassesAsTeacherQuery(): Query {
        return firestoreDB.collection("Class")
            .whereEqualTo("teacher.email",MainActivity.user.email)
    }
    fun getVersesQuery(book: String, chapter: String): Query {

        return firestoreDB.collection("KingJamesVersion")
            .whereEqualTo("book", book).whereEqualTo("chapter", chapter)
            .orderBy("verse")
    }

    fun getCommentsQuery(notePath: String): Query {

        return firestoreDB.document(notePath).collection("comments")
            .orderBy("date")
    }
    fun getEventsQuery(): Query {

        return firestoreDB.collectionGroup("events")
            .orderBy("createdDate", Query.Direction.DESCENDING)
    }
    fun getEventsInClassQuery(classID:String): Query {

        return  firestoreDB.document(classID).collection("events")
            .orderBy("createdDate", Query.Direction.DESCENDING)
    }
    fun getStudentsInClassQuery(classID:String): Query {

        return  firestoreDB.document(classID).collection("students")
            .orderBy("lastName")
            .orderBy("firstName")
        //.limit()
        //.where("students",MainActivity.user)
    }

    fun getNotesQuery(): Query {

        return firestoreDB.collection("Note").whereEqualTo("user", MainActivity.user)
            .orderBy("verseChapter").orderBy("verseNum")
    }


    fun getNotesShareQuery(classID: String): Query {

        return firestoreDB.collection("Note").whereEqualTo("clss", classID)
            .orderBy("verseChapter").orderBy("verseNum")
    }
    fun getUser(path:String,callback: (User) -> Unit){
        firestoreDB.document(path!!).get().addOnSuccessListener {
            if (it != null) {
                callback( it.toObject(User::class.java) as User)
            }
        }
    }

    fun getEvent(path:String,callback: (Event) -> Unit){
        firestoreDB.document(path!!).get().addOnSuccessListener {
            if (it != null) {
                callback( it.toObject(Event::class.java) as Event)
            }
        }
    }
    fun joinClass(id:String,context: Context,dialoge:androidx.appcompat.app.AlertDialog,fab: FloatingActionsMenu){

        firestoreDB.collection("Class").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    getServerTimeStamp { current ->
                        var classes = document.documents
                        for (clss in classes) {
                            var c = clss.toObject(BiblelyClass::class.java)

//                                    c!!.classID = clss.reference.path
                            for (t in c!!.tokens) {
                                if (t.id == id) {

                                    if(c!!.teacher!!.email == MainActivity.user.email){
                                        Toast.makeText(context,"You can't join a class you teach",Toast.LENGTH_LONG).show()
                                        return@getServerTimeStamp
                                    }

                                    if (t.expireDate!!.after(current.toDate())) {

                                        firestoreDB
                                            .document(clss.reference.path)
                                            .collection("students")
                                            .whereEqualTo("email", MainActivity.user.email).get()
                                            .addOnSuccessListener { documents ->
                                                if(documents.isEmpty){
                                                    firestoreDB
                                                        .document(clss.reference.path)
                                                        .collection("students").document().set(MainActivity.user)
                                                    firestoreDB.collection("User")
                                                        .document(MainActivity.user.email)
                                                        .collection("classes").document().set(c)
                                                    FirebaseMessaging.getInstance().subscribeToTopic(c.classID.split("/")[1]
                                                    )
                                                }
                                                else{
                                                    Toast.makeText(context,"You are already in this class",Toast.LENGTH_LONG).show()
//                                                            firestoreDB
//                                                                .document(clss.reference.path)
//                                                                .collection("students").document().set(MainActivity.user)
                                                }

                                            }



                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Token Expired.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    dialoge.dismiss()
                                    fab.collapse()
                                    return@getServerTimeStamp
                                }

                            }
                        }
                        Toast.makeText(
                            context,
                            "Invalid Token.",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                } else {
                    // Log.d(TAG, "No such document")
                }

            }
            .addOnFailureListener { exception ->
                //  Log.d(TAG, "get failed with ", exception)
            }
    }

    fun saveClass(uri:Uri?,title:String,description:String){

            uploadImage(uri){ downloadLink ->
                val fireStore = firestoreDB
                val ref = fireStore.collection("Class").document()

                var biblelyClass = BiblelyClass(
                    ref.path,
                    MainActivity.user,
                    false,
                    title,
                    description,
                    //studentList,
                    downloadLink,
                    java.util.ArrayList<Token>()
                )

                ref.set(biblelyClass)

            }

    }

    fun saveNote(
        isNew: Boolean,
        book: String,
        verseNum: String,
        verseChapter: String,
        verseText: String,
        noteText: String,
        noteTitle: String,
        notePath: String,
        shared: Boolean,
        clssPath: String
    ): Boolean {
        if (isNew) {
            var ref = firestoreDB.collection("Note").document()
            var note = Note(
                ref.path,
                Date(),
                MainActivity.user,
                book,
                verseNum,
                verseChapter,
                verseText,
                noteText,
                noteTitle,
                false,
                shared,
                clssPath
            )

            ref.set(note)

        } else {
            var ref = firestoreDB.document(notePath).get().addOnSuccessListener {
                var note = it.toObject(Note::class.java)
                note!!.noteTitle = noteTitle
                note!!.noteText = noteText
                note!!.shared = shared
                note!!.clss = clssPath

//                var note = Note(notePath,MainActivity.user,book, verseNum, verseChapter, verseText, noteText,noteTitle,)
                firestoreDB.document(notePath).set(note!!)
            }

        }
        return true
    }

    fun uploadImage(uri:Uri?, downloadLinkCallback: (String) -> Unit){

        if(uri != null){
            var storageReference = FirebaseStorage.getInstance().reference
            val ref = storageReference?.child("ClassLogos/" + UUID.randomUUID().toString())
            val uploadTask = ref?.putFile(uri!!)

            uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                    downloadLinkCallback("")
                }
                return@Continuation ref.downloadUrl
            })?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    downloadLinkCallback(downloadUri.toString())
                }
            }?.addOnFailureListener{
                downloadLinkCallback("")
            }
        }
        else{
            downloadLinkCallback("")
        }
    }

    fun editClass(path:String,classLogoPath:String,uri:Uri?,name:String,description:String,callback: () -> Unit){

        if(uri!=null){
            uploadImage(uri){downloadLink ->
                var biblelyClassRef = firestoreDB.document(path).get().addOnSuccessListener { documentSnapshot ->

                    var biblelyClass = documentSnapshot.toObject(BiblelyClass::class.java)
                    biblelyClass!!.name = name
                    biblelyClass.description = description
                    biblelyClass.classLogo = downloadLink

                    firestoreDB.document(path).set(biblelyClass)
                    callback()
                }
            }
        } else {
            var biblelyClassRef = firestoreDB.document(path).get().addOnSuccessListener { documentSnapshot ->

                var biblelyClass = documentSnapshot.toObject(BiblelyClass::class.java)
                biblelyClass!!.name = name
                biblelyClass.description = description
                biblelyClass.classLogo = classLogoPath

                firestoreDB.document(path).set(biblelyClass)
                callback()
            }
        }



    }
    fun promoteStudentAsTeacher(classID: String,biblelyStudent:User){
        FirebaseFirestore.getInstance().document(classID)
            .update("teacher", biblelyStudent)
        FirebaseFirestore.getInstance().document(classID).collection("events")
            .get().addOnSuccessListener { documents ->
                documents.forEach { d ->
                    FirebaseFirestore.getInstance().document(d.reference.path)
                        .update("clss.teacher", biblelyStudent)
                }
            }
        FirebaseFirestore.getInstance().document(classID).collection("students")
            .whereEqualTo("email", biblelyStudent!!.email)
            .get().addOnSuccessListener { documents ->
                documents.forEach { d ->
                    FirebaseFirestore.getInstance().document(d.reference.path)
                        .delete()
                }
            }
        FirebaseFirestore.getInstance().collection("User")
            .document(biblelyStudent.email).collection("classes")
            .whereEqualTo("classID", classID)
            .get().addOnSuccessListener { documents ->
                documents.forEach { d ->
                    FirebaseFirestore.getInstance().document(d.reference.path)
                        .delete()
                }
            }
    }
    fun removeStudentFromClass(classID:String,email:String){
        FirebaseFirestore.getInstance()
            .collection("User").document(email)
            .collection("classes")
            .whereEqualTo("classID", classID).get()
            .addOnSuccessListener { document ->
                document?.forEach { d -> d.reference.delete() }

            }
        /*.document(classID).update(
                "students",
                FieldValue.arrayRemove(biblelyStudent)
            )*/
        FirebaseFirestore.getInstance().document(classID).collection("students")
            .whereEqualTo("email", email).get()
            .addOnSuccessListener { document ->
                document?.forEach { d -> d.reference.delete() }

            }

    }
    fun leaveClass(classID:String){
        firestoreDB
            .collection("User").document(MainActivity.user.email).collection("classes")
            .whereEqualTo("classID",classID).get().addOnSuccessListener { document ->
                document?.forEach { d->d.reference.delete() }

            }
        firestoreDB.document(classID).collection("students")
            .whereEqualTo("email", MainActivity.user.email).get()
            .addOnSuccessListener { document ->
                document?.forEach { d -> d.reference.delete() }

            }
    }
    fun updateClassToken(classID:String,t:Token){
        firestoreDB.document(classID)
            .update("tokens", FieldValue.arrayUnion(t))
    }
    fun getClass(path:String,callback: (BiblelyClass) -> Unit){
        firestoreDB.document(path!!).get().addOnSuccessListener {
            if (it != null) {
                callback( it.toObject(BiblelyClass::class.java) as BiblelyClass)
            }
        }
    }

    fun deleteClass(classPath: String,callback: (Boolean) -> Unit) {

        var ref = firestoreDB.document(classPath)
        ref.collection("students").get().addOnSuccessListener {students ->
            if(students.isEmpty){
                ref.delete()
                callback(true)
            }
            else{
                callback(false)
            }

        }
        ref.delete()
    }

    fun deleteNote(notePath: String) {
        var ref = firestoreDB.document(notePath)
        ref.delete()
    }

    fun editComment(commentPath: String, text: String) {
        var ref = firestoreDB.document(commentPath).get().addOnSuccessListener {
            var comment = it.toObject(Comment::class.java)
            comment!!.text = text

//                var note = Note(notePath,MainActivity.user,book, verseNum, verseChapter, verseText, noteText,noteTitle,)
            firestoreDB.document(commentPath).set(comment)
        }
    }

    fun deleteComment(commentPath: String) {
        var ref = firestoreDB.document(commentPath)
        ref.delete()
        updateHasComment(commentPath.substring(0,commentPath.indexOf("/comment")))
    }

    fun getClassesList(callback: (Map<String, String>) -> Unit) {
        var list = mutableMapOf<String, String>()
        firestoreDB.document("User/" + MainActivity.user.email).collection("classes")
            .get().addOnSuccessListener {
                if (it != null) {
                    it.forEach { clss ->
                        list.put(
                            clss["classID"].toString(),
                            clss["name"].toString()
                        )
                    }
                    firestoreDB.collection("Class")
                        .whereEqualTo("teacher.email", MainActivity.user.email)
                        .get().addOnSuccessListener { it2 ->
                            it2.forEach { clss ->
                                list.put(
                                    clss["classID"].toString(),
                                    clss["name"].toString()
                                )
                            }
                            callback(list)
                        }

                }
            }
    }

    fun getNote(notePath: String, callback: (Note) -> Unit) {
        firestoreDB.document(notePath)
            .get().addOnSuccessListener {
                if (it != null) {

                    var note = it.toObject(Note::class.java)
                    callback(note!!)
                }
            }
    }

    private fun updateHasComment(notePath: String){
        firestoreDB.document(notePath).collection("comments").get().addOnSuccessListener { comments ->
            firestoreDB.document(notePath).get().addOnSuccessListener {
                if (it != null) {

                    var note = it.toObject(Note::class.java)
                    note!!.hasComment = !comments.isEmpty
                    firestoreDB.document(notePath).set(note)
                }
            }
        }

    }
    fun addComment(notePath: String, text: String) {
        var ref = firestoreDB.document(notePath).collection("comments").document()
        var comment = Comment(ref.path, MainActivity.user, text, Date())
        ref.set(comment)
        updateHasComment(notePath)

    }
    fun getCommentInvolvedUsers(notePath: String,callback: (MutableList<String>) -> Unit){
        var involvedUserEmailList = mutableListOf<String>()
        var ref = firestoreDB.document(notePath).collection("comments").get().addOnSuccessListener {documents ->
            documents.forEach { doc->
             var comment =  doc.toObject(Comment::class.java)
                if(!involvedUserEmailList.contains(comment.user!!.email))
                        involvedUserEmailList.add(comment.user!!.email)
            }
            callback(involvedUserEmailList)
        }

    }
}