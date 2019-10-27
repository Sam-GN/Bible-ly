package edu.calbaptist.bible_ly.ui.classes

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getbase.floatingactionbutton.FloatingActionButton
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.getbase.floatingactionbutton.FloatingActionsMenu.OnFloatingActionsMenuUpdateListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import edu.calbaptist.bible_ly.*
import edu.calbaptist.bible_ly.adapter.ClassAdapter
import kotlinx.android.synthetic.main.dialogue_new_class.view.*
import kotlinx.android.synthetic.main.fragment_classes.*


private const val DIALOG_CLASS = "DialogDate"
private const val REQUEST_CLASS = 0
class ClassesFragment : Fragment(), ClassAdapter.OnClassItemSelectedListener {

    private lateinit var classesViewModel: ClassesViewModel
    private lateinit var fabCreate: FloatingActionButton
    private lateinit var fab: FloatingActionsMenu
    private lateinit var  classesAsStudentRecycleView: RecyclerView
    private lateinit var  noItemTV: TextView
    lateinit var firestore: FirebaseFirestore
    lateinit var studentAdapter: ClassAdapter
    lateinit var teacherAdapter: ClassAdapter
    lateinit var query: Query
    private lateinit var studentLinearLayoutManager: LinearLayoutManager
    private lateinit var teacherLinearLayoutManager: LinearLayoutManager
    private lateinit var classesAsTeacherRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,

        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        classesViewModel =
            ViewModelProviders.of(this).get(ClassesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_classes, container, false)

        fabCreate = root.findViewById(R.id.fab_class_create)
        fab = root.findViewById(R.id.fab_class)
        noItemTV = root. findViewById(R.id.tv_class_no_items) as TextView
        classesAsStudentRecycleView = root.findViewById(R.id.class_frag_student_recycleView) as RecyclerView
        classesAsTeacherRecyclerView = root.findViewById(R.id.class_frag_teacher_recycleView) as RecyclerView


        studentLinearLayoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL ,false)
        teacherLinearLayoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL ,false)
        classesAsStudentRecycleView.layoutManager = studentLinearLayoutManager
        classesAsTeacherRecyclerView.layoutManager = teacherLinearLayoutManager
        firestore = FirebaseFirestore.getInstance()

        // Get ${LIMIT} restaurants
        query = firestore.collection("Class")
//            .orderBy("startDate", Query.Direction.DESCENDING)
            //.limit()
            .whereArrayContains("students",MainActivity.user)

        var query2 = firestore.collection("Class")
//            .orderBy("startDate", Query.Direction.DESCENDING)
            //.limit()
            .whereEqualTo("teacher",MainActivity.user)
        // RecyclerView
        studentAdapter = object : ClassAdapter(query, this@ClassesFragment) {
            override fun onDataChanged() {
                // Show/hide content if the query returns empty.
                Log.i("ClassFragment student",itemCount.toString())
                updateUi()
            }

            override fun onError(e: FirebaseFirestoreException) {
                // Show a snackbar on errors
                Log.e("ClassFragment",e.message)
            }
        }

        teacherAdapter = object : ClassAdapter(query2, this@ClassesFragment) {
            override fun onDataChanged() {
                // Show/hide content if the query returns empty.
                Log.i("ClassFragment teacher",itemCount.toString())
                updateUi()
            }

            override fun onError(e: FirebaseFirestoreException) {
                // Show a snackbar on errors
                Log.e("ClassFragment",e.message)
            }
        }


        classesAsTeacherRecyclerView.adapter = teacherAdapter
        classesAsStudentRecycleView.adapter = studentAdapter

        //classesAsStudentRecycleView.alpha = 0.3f


        return root
    }

    fun updateUi(){

        if(teacherAdapter.itemCount == 0&&studentAdapter.itemCount==0){
            noItemTV.visibility = View.VISIBLE
            classesAsTeacherRecyclerView.visibility = View.GONE
            classesAsStudentRecycleView.visibility = View.GONE
            tv_class_studentRV_label.visibility = View.GONE
            tv_class_teacherRV_label.visibility = View.GONE
        } else {
            noItemTV.visibility = View.GONE
            classesAsTeacherRecyclerView.visibility = View.VISIBLE
            classesAsStudentRecycleView.visibility = View.VISIBLE
            tv_class_studentRV_label.visibility = View.VISIBLE
            tv_class_teacherRV_label.visibility = View.VISIBLE
        }
        if(studentAdapter.itemCount == 0){
            tv_class_studentRV_label.visibility = View.GONE
            classesAsStudentRecycleView.visibility = View.GONE
        } else {
            tv_class_studentRV_label.visibility = View.VISIBLE
            classesAsStudentRecycleView.visibility = View.VISIBLE
        }
        if(teacherAdapter.itemCount == 0){
            tv_class_teacherRV_label.visibility = View.GONE
            classesAsTeacherRecyclerView.visibility = View.GONE
        } else {
            tv_class_teacherRV_label.visibility = View.VISIBLE
            classesAsTeacherRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onClassItemSelected(classItem: DocumentSnapshot) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStart() {
        super.onStart()

        // Start sign in if necessary
        /* if (shouldStartSignIn()) {
             startSignIn()
             return
         }*/

        // Apply filters
        //  onFilter(viewModel.filters)

        // Start listening for Firestore updates
        studentAdapter.startListening()
        teacherAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        studentAdapter.stopListening()
        teacherAdapter.stopListening()
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fabCreate.setOnClickListener {
            /*  var myDialog= CreateClassDialogFragment()
           // val fragmentTransaction = fragmentManager!!.beginTransaction()
            //myDialog.show(fragmentTransaction,"")

            val ft = requireActivity().getSupportFragmentManager().beginTransaction()
           // val newFragment = CreateClassDialogFragment.newInstance("pass content here")
          //  myDialog.show(this@ClassesFragment.requireFragmentManager(), "1001")
            myDialog.apply {
                setTargetFragment(this@ClassesFragment, REQUEST_CLASS)
                show(this@ClassesFragment.requireFragmentManager(), DIALOG_CLASS)
            }*/

            val view = layoutInflater.inflate(R.layout.dialogue_new_class, null)
            view.et_class_new_teacher.let {
                it.setText(MainActivity.user.userName)
                it.isEnabled = false
            }
            view.ib_class_new_classLogo.apply {
                val packageManager: PackageManager = requireActivity().packageManager

                val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val resolvedActivity: ResolveInfo? =
                    packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
                if (resolvedActivity == null) {
                    isEnabled = false
                }
                setOnClickListener {
                    /*  captureImage.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
                    val cameraActivites: List<ResolveInfo> =
                        packageManager.queryIntentActivities(captureImage,PackageManager.MATCH_DEFAULT_ONLY)

                    for (cameraActivity in cameraActivites) {
                        requireActivity().grantUriPermission(
                            cameraActivity.activityInfo.packageName,photoUri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }
                    startActivityForResult(captureImage, REQUEST_PHOTO)*/
                }
            }
            AlertDialog.Builder(requireActivity())
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton("Close", DialogInterface.OnClickListener { dialog, which ->
                    //Action goes here
                })
                .setPositiveButton("Create", DialogInterface.OnClickListener { dialog, which ->
                    //
                   var studentList = ArrayList<User>()
                    studentList.add(MainActivity.user)
                    var biblelyClass = Class(
                        "",
                        MainActivity.user,
                        view.sw_class_new_autoJoin.isChecked,
                        view.et_class_new_title.text.toString(),
                        view.et_class_new_description.text.toString(),
                        studentList
                    )
                    val fireStore = FirebaseFirestore.getInstance()
                    val eventRef = fireStore.collection("Class").document()
                    eventRef.set(biblelyClass)
                    fab_class.collapse()
                }
                ).setView(view).create().show()
        }
        fab.setOnClickListener {
            if (!fab.isExpanded) {
                classesAsStudentRecycleView.alpha = 0.1f
                classesAsTeacherRecyclerView.alpha = 0.1f
            } else {
                classesAsStudentRecycleView.alpha = 1f
                classesAsTeacherRecyclerView.alpha = 1f
            }

        }
    }
}