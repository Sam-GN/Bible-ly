package edu.calbaptist.bible_ly.ui.classes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import edu.calbaptist.bible_ly.BiblelyClass
import edu.calbaptist.bible_ly.Event
import edu.calbaptist.bible_ly.FirestoreRepository


class ClassesViewModel : ViewModel() {
    val TAG = "ClassesViewModel"
    var firebaseRepository = FirestoreRepository()
    private var classesAsStudent : MutableLiveData<List<BiblelyClass>> = MutableLiveData()
    var classesAsTeacher : MutableLiveData<List<BiblelyClass>> = MutableLiveData()


    fun getClassesAsStudent(): LiveData<List<BiblelyClass>> {
        firebaseRepository.getClassesAsStudentQuery()
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    classesAsStudent.value = null
                    return@EventListener
                }

                var list: MutableList<BiblelyClass> = mutableListOf()


                for (doc in value!!) {
                    list.add(doc.toObject(BiblelyClass::class.java))
                }
                classesAsStudent.value = list

            })

        return classesAsStudent
    }

    fun getClassesAsTeacher(): LiveData<List<BiblelyClass>> {
        firebaseRepository.getClassesAsTeacherQuery()
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    classesAsTeacher.value = null
                    return@EventListener
                }

                var list: MutableList<BiblelyClass> = mutableListOf()


                for (doc in value!!) {
                    list.add(doc.toObject(BiblelyClass::class.java))
                }
                classesAsTeacher.value = list

            })

        return classesAsTeacher
    }


}