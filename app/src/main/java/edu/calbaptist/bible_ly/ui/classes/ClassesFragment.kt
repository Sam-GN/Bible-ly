package edu.calbaptist.bible_ly.ui.classes

import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import edu.calbaptist.bible_ly.*
import edu.calbaptist.bible_ly.adapter.ClassAdapter
import kotlinx.android.synthetic.main.dialogue_join_class.view.*
import kotlinx.android.synthetic.main.dialogue_new_class.*
import kotlinx.android.synthetic.main.dialogue_new_class.view.*
import kotlinx.android.synthetic.main.fragment_classes.*
import java.util.*
import kotlin.collections.ArrayList


private const val DIALOG_CLASS = "DialogDate"
private const val FINAL_CHOOSE_PHOTO = 122
class ClassesFragment : Fragment(), ClassAdapter.OnClassItemSelectedListener {

    private lateinit var classesViewModel: ClassesViewModel
    private lateinit var fabCreate: FloatingActionButton
    private lateinit var fabJoin: FloatingActionButton
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
    private  var uri: Uri? =null

    private lateinit var dialogView:View

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
        val intent = Intent(requireContext(), ClassSingleActivity::class.java)
// To pass any data to next activity
        intent.putExtra("path", classItem.reference.path)
        intent.putExtra("title", classItem.get("name").toString())
// start your next activity
        startActivity(intent)
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

            val view = layoutInflater.inflate(R.layout.dialogue_new_class, null)
            dialogView=view
            view.et_class_new_teacher.let {
                it.setText(MainActivity.user.userName)
                it.isEnabled = false
            }
            view.ib_class_new_classLogo.apply {

                setOnClickListener {

                    val checkSelfPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (checkSelfPermission != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                    }
                    else{
                        openAlbum()
                    }
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
                    saveClass()
                    fab.collapse()
                }
                ).setView(view).create().show()
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
                dialoge.dismiss()
                fab.collapse()
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
    private fun openAlbum(){
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        startActivityForResult(intent, FINAL_CHOOSE_PHOTO)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1 ->
                if (grantResults.isNotEmpty() && grantResults.get(0) ==PackageManager.PERMISSION_GRANTED){
                    openAlbum()
                }
                else {
                    Toast.makeText(requireContext(), "You denied the permission", Toast.LENGTH_SHORT).show()
                }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            /*FINAL_TAKE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    val bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri))
                    picture!!.setImageBitmap(bitmap)
                }*/
            FINAL_CHOOSE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
//                        4.4以上
                        handleImageOnKitkat(data)
                    }
                    else{
//                        4.4以下
                        handleImageBeforeKitkat(data)
                    }
                }
        }
    }
    @TargetApi(19)
    private fun handleImageOnKitkat(data: Intent?) {
        var imagePath: String? = null
        val uri = data!!.data
        if (DocumentsContract.isDocumentUri(requireContext(), uri)){
//            document类型的Uri，用document id处理
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri.authority){
                val id = docId.split(":")[1]
                val selsetion = MediaStore.Images.Media._ID + "=" + id
                imagePath = imagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selsetion)
            }
            else if ("com.android.providers.downloads.documents" == uri.authority){
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(docId))
                imagePath = imagePath(contentUri, null)
            }
        }
        else if ("content".equals(uri.scheme, ignoreCase = true)){
//            content类型Uri 普通方式处理
            imagePath = imagePath(uri, null)
        }
        else if ("file".equals(uri.scheme, ignoreCase = true)){
            imagePath = uri.path
        }
        displayImage(imagePath,uri)
    }

    //    没4.4的设备，略过
    private fun handleImageBeforeKitkat(data: Intent?) {}

    private fun imagePath(uri: Uri?, selection: String?): String {
        var path: String? = null
//        通过Uri和selection获取路径
        val cursor = requireActivity().contentResolver.query(uri, null, selection, null, null )
        if (cursor != null){
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }

    private fun displayImage(imagePath: String?,uri: Uri ){
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            dialogView.ib_class_new_classLogo?.setImageBitmap(bitmap)
            this.uri = uri

        }
        else {
            Toast.makeText(requireContext(), "Failed to get image", Toast.LENGTH_SHORT).show()
        }
    }
    private fun saveClass(){
        var storageReference = FirebaseStorage.getInstance().reference
        if(this.uri != null){
            val ref = storageReference?.child("ClassLogos/" + UUID.randomUUID().toString())
            val uploadTask = ref?.putFile(this.uri!!)

            val urlTask = uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            })?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    Toast.makeText(requireContext(), downloadUri.toString(), Toast.LENGTH_SHORT).show()

                    var studentList = ArrayList<User>()
                    studentList.add(MainActivity.user)
                    studentList.add(MainActivity.user)
                    var biblelyClass = Class(
                        "",
                        MainActivity.user,
                        dialogView.sw_class_new_autoJoin.isChecked,
                        dialogView.et_class_new_title.text.toString(),
                        dialogView.et_class_new_description.text.toString(),
                        studentList,
                        downloadUri.toString(),
                        mapOf()
                    )
                    val fireStore = FirebaseFirestore.getInstance()
                    val eventRef = fireStore.collection("Class").document()
                    eventRef.set(biblelyClass)

                    for(a in studentList){
                        val ref2 = fireStore.document(eventRef.path).collection("students").document()
                        ref2.set(a)
                    }

                    //addUploadRecordToDb(downloadUri.toString())
                } else {
                    // Handle failures
                }
            }?.addOnFailureListener{

            }
        }else{
            var studentList = ArrayList<User>()
            // studentList.add(MainActivity.user)
            var biblelyClass = Class(
                "",
                MainActivity.user,
                dialogView.sw_class_new_autoJoin.isChecked,
                dialogView.et_class_new_title.text.toString(),
                dialogView.et_class_new_description.text.toString(),
                studentList,
                "",
               mapOf()
            )
            val fireStore = FirebaseFirestore.getInstance()
            val eventRef = fireStore.collection("Class").document()
            eventRef.set(biblelyClass)
            for(a in studentList){
                val ref2 = fireStore.document(eventRef.path).collection("students").document()
                ref2.set(a)
            }
        }


    }
}
