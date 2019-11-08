package edu.calbaptist.bible_ly

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.firestore.*
import edu.calbaptist.bible_ly.adapter.StudentAdapter
import kotlinx.android.synthetic.main.activity_class_single.*
import kotlinx.android.synthetic.main.content_class_single.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import edu.calbaptist.bible_ly.adapter.ClassSingleEventAdapter
import edu.calbaptist.bible_ly.ui.EventFragment
import kotlinx.android.synthetic.main.event_detailed_fragment.view.*
import java.util.*
import kotlin.collections.ArrayList


private lateinit var title: String
private lateinit var  classesAsStudentRecycleView: RecyclerView
private lateinit var  eventRecycleView: RecyclerView
lateinit var firestore: FirebaseFirestore
lateinit var studentAdapter: StudentAdapter
var eventAdapter: ClassSingleEventAdapter?=null
lateinit var query: Query
private lateinit var studentLinearLayoutManager: LinearLayoutManager
private lateinit var eventLinearLayoutManager: LinearLayoutManager
private lateinit var classTokens: ArrayList<Token>

private lateinit var path: String
private var iAmTeacher: Boolean = false
private lateinit var mmenu: Menu



class ClassSingleActivity : AppCompatActivity()
    , StudentAdapter.OnStudentItemSelectedListener
    , StudentAdapter.OnMoreItemSelectedListener
    , ClassSingleEventAdapter.OnClassSingleEventItemSelectedListener {
    override fun onMoreItemSelected(view: View, StudentItem: DocumentSnapshot) {
        if(iAmTeacher){
            showPopupStudent(view,StudentItem)
        }
    }
    private fun showPopupStudent(view: View,snapshot: DocumentSnapshot) {
        var popup: PopupMenu? = null;
        popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_students_item_more)


        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.students_item_action_delete-> {

                    var dialoge = AlertDialog.Builder(view.context)
                        .setCancelable(false)
                        .setTitle("Are you sure you want to remove the student?")
                        .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                            //Action goes here
                        })
                        .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                            //snapshot.reference.delete()
                            //FirebaseFirestore.getInstance().document(path).
                            val biblelyStudent = snapshot.toObject(User::class.java)

                            FirebaseFirestore.getInstance()
                                .collection("User").document(biblelyStudent!!.email).collection("classes")
                                .whereEqualTo("classID",path).get().addOnSuccessListener {document ->
                                    document?.forEach { d->d.reference.delete() }

                                }
                                /*.document(path).update(
                                    "students",
                                    FieldValue.arrayRemove(biblelyStudent)
                                )*/
                            FirebaseFirestore.getInstance().document(path).collection("students")
                                .whereEqualTo("email", biblelyStudent?.email).get()
                                .addOnSuccessListener { document ->
                                    document?.forEach { d -> d.reference.delete() }

                                }
                            Toast.makeText(view.context,  "Student Removed", Toast.LENGTH_SHORT).show()
                            // this.finish()
                        })
                        .create()

                    dialoge.show()

                }
                R.id.students_item_action_promote->{
                    var dialoge = AlertDialog.Builder(view.context)
                        .setCancelable(false)
                        .setTitle("Are you sure you want to promote this student to teacher?\nYou will be removed from the class")
                        .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                            //Action goes here
                        })
                        .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                            //snapshot.reference.delete()
                            //FirebaseFirestore.getInstance().document(path).
                            val biblelyStudent = snapshot.toObject(User::class.java)



                            FirebaseFirestore.getInstance().document(path).update("teacher",biblelyStudent)
                            FirebaseFirestore.getInstance().document(path).collection("events")
                                .get().addOnSuccessListener {documents ->
                                documents.forEach { d-> FirebaseFirestore.getInstance().document(d.reference.path)
                                    .update("clss.teacher",biblelyStudent) }
                            }
                            FirebaseFirestore.getInstance().document(path).collection("students").whereEqualTo("email",biblelyStudent!!.email)
                                .get().addOnSuccessListener {documents ->
                                    documents.forEach { d->
                                        FirebaseFirestore.getInstance().document(d.reference.path)
                                        .delete()}
                                }
                            FirebaseFirestore.getInstance().collection("User").document(biblelyStudent.email).collection("classes").whereEqualTo("classID",path)
                                .get().addOnSuccessListener {documents ->
                                    documents.forEach { d->
                                        FirebaseFirestore.getInstance().document(d.reference.path)
                                            .delete()}
                                }

                            Toast.makeText(view.context,  "Student Promoted", Toast.LENGTH_SHORT).show()
                             this.finish()
                        })
                        .create()

                    dialoge.show()
                }
                /* R.id.header2 -> {
                     Toast.makeText(this@MainActivity, item.title, Toast.LENGTH_SHORT).show();
                 }
                 R.id.header3 -> {
                     Toast.makeText(this@MainActivity, item.title, Toast.LENGTH_SHORT).show();
                 }*/
            }

            true
        })

        popup.show()
    }
    override fun onClassSingleEventItemSelected(ClassSingleEventItem: DocumentSnapshot) {
         var d = EventDialog.newInstance(false, iAmTeacher , path,ClassSingleEventItem.reference.path)
//        d.show(requireFragmentManager(),"")

        val fm = supportFragmentManager
        d.show(fm,"EventDialog")
    }

    override fun onStudentItemSelected(StudentItem: DocumentSnapshot) {
        var d = StudentDialog.newInstance(StudentItem.reference.path)
//        d.show(requireFragmentManager(),"")

        val fm = supportFragmentManager
        d.show(fm,"StudentDialog")
    }

    //lateinit var mySupportActionBar: ActionBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_single)
        setSupportActionBar(toolbar)
        var bundle: Bundle? = intent.extras


        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        path = bundle!!.getString("path")
        var title = bundle.getString("title")
        FirebaseFirestore.getInstance().document(path!!).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                  classs  = document.toObject(Class::class.java)
                    // mySupportActionBar.title = classs!!.name
                    tv_class_single_teacher.text = classs!!.teacher!!.userName
                    tv_class_single_description.text = classs!!.description
                    iv_class_single_teacher.setGlide(classs!!.teacher!!.photoID)
                    //sw_class_single_auto_join.isChecked = classs!!.isPublic
                    if(classs!!.teacher!!.email==MainActivity.user.email){
                        iAmTeacher = true
                       if(mmenu!=null)
                           mmenu.removeItem(R.id.action_leave)
                    } else {
                        btn_class_single_new_event.visibility = View.GONE
                        iAmTeacher = false
                    }
                    classTokens = classs!!.tokens


                    var q=FirebaseFirestore.getInstance().document(path).collection("events")
                        .orderBy("createdDate", Query.Direction.DESCENDING)
                    eventAdapter = object : ClassSingleEventAdapter(q, this@ClassSingleActivity) {
                        override fun onDataChanged() {
                            // Show/hide content if the query returns empty.
                            Log.i("ClassFragment student", itemCount.toString())

                            if(itemCount !=0)
                                tv_class_single_no_events.visibility = View.INVISIBLE
                            else
                                tv_class_single_no_events.visibility = View.VISIBLE
                        }

                        override fun onError(e: FirebaseFirestoreException) {
                            // Show a snackbar on errors
                            Log.e("ClassFragment", e.message)
                        }
                    }

                    eventRecycleView.adapter = eventAdapter
                    eventAdapter!!.startListening()


                } else {
                    // Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                //  Log.d(TAG, "get failed with ", exception)
            }

        iv_class_single_teacher.setOnClickListener {

            var d = StudentDialog.newInstance("User/"+ classs!!.teacher!!.email)
//        d.show(requireFragmentManager(),"")

            val fm = supportFragmentManager
            d.show(fm,"StudentDialog")
        }

        fab.setOnClickListener { view ->


            getServerTimeStamp { a ->

                for (t in classTokens) {
                    if (t.user!!.email == MainActivity.user.email && t.expireDate!!.after(a.toDate())) {
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "" + t.id)
                            putExtra(Intent.EXTRA_SUBJECT, "Join Bible-ly Class")
                        }.also { intent ->
                            val chooserIntent = Intent.createChooser(intent, "" + t.id)
                            startActivity(chooserIntent)
                        }
                        return@getServerTimeStamp
                    }
                }

                val t = Token(
                    addDay(a, 2),
                    UUID.randomUUID().toString().substring(0, 8),
                    MainActivity.user
                )
                FirebaseFirestore.getInstance().document(path)
                    .update("tokens", FieldValue.arrayUnion(t))
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "" + t.id)
                    putExtra(Intent.EXTRA_SUBJECT, "Join Bible-ly Class")
                }.also { intent ->
                    val chooserIntent = Intent.createChooser(intent, "" + t.id)
                    startActivity(chooserIntent)
                }
            }


        }
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        app_bar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {

                val toolBarHeight = toolbar.measuredHeight
                val appBarHeight = appBarLayout.getMeasuredHeight()
                var f: Float =
                    (appBarHeight.toFloat() - toolBarHeight + verticalOffset) / (appBarHeight.toFloat() - toolBarHeight) * 255
                if (f >= 254)
                    return
                if (f < 40)
                    f = 0.0f

                iv_class_single_logo.alpha = (f / 255)
//                    Log.v("asghar",""+(f))
//                    Log.v("asghar2",""+verticalOffset)
//                    Log.v("asghar3",""+(Math.round(f) -255 ))

            }
        })

        classesAsStudentRecycleView = findViewById(R.id.rv_class_single_students)
        studentLinearLayoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL, false
        )
        classesAsStudentRecycleView.layoutManager = studentLinearLayoutManager
        firestore = FirebaseFirestore.getInstance()

        // Get ${LIMIT} restaurants
        query = firestore.document(path).collection("students")
//            .orderBy("startDate", Query.Direction.DESCENDING)
        //.limit()
        //.where("students",MainActivity.user)

        // RecyclerView
        studentAdapter = object : StudentAdapter(query, this@ClassSingleActivity,this@ClassSingleActivity) {
            override fun onDataChanged() {
                // Show/hide content if the query returns empty.
                Log.i("ClassFragment student", itemCount.toString())
                //updateUi()
            }

            override fun onError(e: FirebaseFirestoreException) {
                // Show a snackbar on errors
                Log.e("ClassFragment", e.message)
            }
        }

        classesAsStudentRecycleView.adapter = studentAdapter


        eventRecycleView = findViewById(R.id.rv_class_single_events)
        eventLinearLayoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        eventRecycleView.layoutManager = eventLinearLayoutManager





        btn_class_single_new_event.setOnClickListener {

            var d = EventDialog.newInstance(true,iAmTeacher, path,"")
//        d.show(requireFragmentManager(),"")

            val fm = supportFragmentManager
            d.show(fm,"EventDialog")

        }

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
        if(eventAdapter!=null)
            eventAdapter!!.startListening()

    }

    override fun onStop() {
        super.onStop()
        studentAdapter.stopListening()
        eventAdapter!!.stopListening()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_class_single, menu)
        mmenu=menu

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {


            android.R.id.home ->{
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("currentDestination", R.id.classes)
                startActivity(intent)
                this.finish()
            }
            R.id.action_leave -> {

                var dialoge = AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Are you sure you want to leave?")
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                        //Action goes here
                    })
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
//                        FirebaseFirestore.getInstance()
//                            .document(path).update(
//                                "students",
//                                FieldValue.arrayRemove(MainActivity.user)
//                            )
                        FirebaseFirestore.getInstance()
                            .collection("User").document(MainActivity.user.email).collection("classes")
                            .whereEqualTo("classID",path).get().addOnSuccessListener {document ->
                                document?.forEach { d->d.reference.delete() }

                            }
                        FirebaseFirestore.getInstance().document(path).collection("students")
                            .whereEqualTo("email", MainActivity.user.email).get()
                            .addOnSuccessListener { document ->
                                document?.forEach { d -> d.reference.delete() }

                            }
                        this.finish()
                    })
                    .create()

                dialoge.show()
            }

        }
        return super.onOptionsItemSelected(item)
    }



     /*override fun onBackPressed() {

         //this.finish()
            super.onBackPressed()
        Toast.makeText(this,"haha",Toast.LENGTH_LONG).show()

     }*/

    companion object{
        var classs: Class? = null
    }
}