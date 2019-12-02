package edu.calbaptist.bible_ly.ui.classes

import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getbase.floatingactionbutton.FloatingActionButton
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.getbase.floatingactionbutton.FloatingActionsMenu.OnFloatingActionsMenuUpdateListener
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import edu.calbaptist.bible_ly.*
import edu.calbaptist.bible_ly.R
import edu.calbaptist.bible_ly.adapter.ClassAdapter
import edu.calbaptist.bible_ly.adapter.ClassMutableListAdapter
import kotlinx.android.synthetic.main.dialogue_join_class.view.*
import kotlinx.android.synthetic.main.dialogue_new_class.view.*
import kotlinx.android.synthetic.main.fragment_classes.*
import kotlinx.android.synthetic.main.fragment_classes.view.*
import java.util.*
import kotlin.collections.ArrayList
import androidx.lifecycle.Observer


class ClassesFragment : Fragment(), ClassMutableListAdapter.OnBiblelyClassItemSelectedListener ,ClassDialog.Callback{
    override fun onClassCreated() {
        fab.collapse()
    }


    override fun onBiblelyClassItemSelected(classItem: BiblelyClass) {
        val intent = Intent(requireContext(), ClassSingleActivity::class.java)
// To pass any data to next activity
        intent.putExtra("classID", classItem.classID)

// start your next activity
        startActivity(intent)
        activity!!.finish()
    }

    private lateinit var classesViewModel: ClassesViewModel
    private lateinit var fabCreate: FloatingActionButton
    private lateinit var fabJoin: FloatingActionButton
    private lateinit var fab: FloatingActionsMenu
    private lateinit var  classesAsStudentRecycleView: RecyclerView
    lateinit var firestore: FirebaseFirestore
    lateinit var adapterAsStudent: ClassMutableListAdapter
    lateinit var adapterAsTeacher: ClassMutableListAdapter
    private lateinit var studentLinearLayoutManager: LinearLayoutManager
    private lateinit var teacherLinearLayoutManager: LinearLayoutManager
    private lateinit var classesAsTeacherRecyclerView: RecyclerView

    var listAsStudents: List<BiblelyClass> = listOf()
    var listAsTeacher: List<BiblelyClass> = listOf()

    //private lateinit var dialogView:View

    override fun onCreateView(
        inflater: LayoutInflater,

        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        classesViewModel =
            ViewModelProviders.of(this).get(ClassesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_classes, container, false)


        fabJoin = root.findViewById(R.id.fab_class_join)
        fabCreate = root.findViewById(R.id.fab_class_create)

        fab = root.findViewById(R.id.fab_class)
        classesAsStudentRecycleView = root.findViewById(R.id.class_frag_student_recycleView) as RecyclerView
        classesAsTeacherRecyclerView = root.findViewById(R.id.class_frag_teacher_recycleView) as RecyclerView


        studentLinearLayoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL ,false)
        teacherLinearLayoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL ,false)
        classesAsStudentRecycleView.layoutManager = studentLinearLayoutManager
        classesAsTeacherRecyclerView.layoutManager = teacherLinearLayoutManager
        firestore = FirebaseFirestore.getInstance()
        adapterAsStudent= ClassMutableListAdapter( this)
        classesAsStudentRecycleView.adapter = adapterAsStudent
        adapterAsTeacher= ClassMutableListAdapter( this)
        classesAsTeacherRecyclerView.adapter = adapterAsTeacher

        classesViewModel.getClassesAsStudent().observe(this, Observer {list ->

            if(list!=null)
            {
                listAsStudents = list
                adapterAsStudent.submitList(list)
                updateUi()
            }

        })
        classesViewModel.getClassesAsTeacher().observe(this, Observer {list ->

            if(list!=null)
            {
                listAsTeacher=list
                adapterAsTeacher.submitList(list)
                updateUi()
            }
        })

        root.tv_class_teacherRV_label.visibility = View.GONE
        root.tv_class_studentRV_label.visibility = View.GONE


        return root
    }


    private fun updateUi(){

        if(listAsTeacher.isEmpty()&&listAsStudents.isEmpty()){
            tv_class_no_items.visibility = View.VISIBLE
            classesAsTeacherRecyclerView.visibility = View.GONE
            classesAsStudentRecycleView.visibility = View.GONE
            tv_class_studentRV_label.visibility = View.GONE
            tv_class_teacherRV_label.visibility = View.GONE
        } else {
            tv_class_no_items.visibility = View.GONE
            classesAsTeacherRecyclerView.visibility = View.VISIBLE
            classesAsStudentRecycleView.visibility = View.VISIBLE
            tv_class_studentRV_label.visibility = View.VISIBLE
            tv_class_teacherRV_label.visibility = View.VISIBLE
        }
        if(listAsStudents.isEmpty()){
            tv_class_studentRV_label.visibility = View.GONE
            classesAsStudentRecycleView.visibility = View.GONE
        } else {
            tv_class_studentRV_label.visibility = View.VISIBLE
            classesAsStudentRecycleView.visibility = View.VISIBLE
        }
        if(listAsTeacher.isEmpty()){
            tv_class_teacherRV_label.visibility = View.GONE
            classesAsTeacherRecyclerView.visibility = View.GONE
        } else {
            tv_class_teacherRV_label.visibility = View.VISIBLE
            classesAsTeacherRecyclerView.visibility = View.VISIBLE
        }
    }






    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fabCreate.setOnClickListener {

            ClassDialog.newInstance(true,"").apply {
                setTargetFragment(this@ClassesFragment, 0)
                show(this@ClassesFragment.requireFragmentManager(), "ClassDialog")
            }

//                var d = ClassDialog.newInstance(
//                    true,
//                    ""
//                )
//                val fm = requireActivity().supportFragmentManager
//                d.show(fm, "ClassDialog")

        }

        fabJoin.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.dialogue_join_class, null)

          var dialoge = AlertDialog.Builder(requireActivity())
                .setCancelable(false)
                .setNegativeButton("Close", DialogInterface.OnClickListener { dialog, which ->
                    //Action goes here
                })
                .setView(view).create()

            dialoge.show()

            view.btn_class_join.setOnClickListener {
                var id = view.tf_class_join_token.editText!!.text.toString()
                FirestoreRepository().joinClass(id,requireContext(),dialoge ,fab)


            }
        }

        fab.setOnFloatingActionsMenuUpdateListener(object :
            OnFloatingActionsMenuUpdateListener {
            override fun onMenuCollapsed() {
                classesAsStudentRecycleView.alpha = 1f
                classesAsTeacherRecyclerView.alpha = 1f
                tv_class_teacherRV_label.alpha = 1f
                tv_class_studentRV_label.alpha = 1f
            }

            override fun onMenuExpanded() {
                classesAsStudentRecycleView.alpha = 0.1f
                classesAsTeacherRecyclerView.alpha = 0.1f
                tv_class_teacherRV_label.alpha = 0.1f
                tv_class_studentRV_label.alpha = 0.1f
            }
        })


        }

}
