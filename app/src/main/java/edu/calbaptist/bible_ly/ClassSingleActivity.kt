package edu.calbaptist.bible_ly

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBar
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.common.math.Quantiles
import com.google.firebase.firestore.*
import edu.calbaptist.bible_ly.adapter.ClassAdapter
import edu.calbaptist.bible_ly.adapter.StudentAdapter
import kotlinx.android.synthetic.main.activity_class_single.*
import kotlinx.android.synthetic.main.content_class_single.*
import kotlinx.android.synthetic.main.list_class_item_class.*
import com.google.firebase.firestore.FieldValue.serverTimestamp
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import edu.calbaptist.bible_ly.adapter.ClassSingleEventAdapter
import kotlinx.android.synthetic.main.event_detailed_fragment.*
import kotlinx.android.synthetic.main.event_detailed_fragment.view.*
import java.util.*
import kotlin.collections.ArrayList


private lateinit var title: String
private lateinit var  classesAsStudentRecycleView: RecyclerView
private lateinit var  eventRecycleView: RecyclerView
lateinit var firestore: FirebaseFirestore
lateinit var studentAdapter: StudentAdapter
lateinit var eventAdapter: ClassSingleEventAdapter
lateinit var query: Query
private lateinit var studentLinearLayoutManager: LinearLayoutManager
private lateinit var eventLinearLayoutManager: LinearLayoutManager
private lateinit var classTokens: ArrayList<Token>

private lateinit var path: String
private var iAmTeacher: Boolean = false
private lateinit var mmenu: Menu
private var classs: Class? = null

class ClassSingleActivity : AppCompatActivity(), StudentAdapter.OnStudentItemSelectedListener, ClassSingleEventAdapter.OnClassSingleEventItemSelectedListener {
    override fun onClassSingleEventItemSelected(ClassSingleEventItem: DocumentSnapshot) {

    }

    override fun onStudentItemSelected(StudentItem: DocumentSnapshot) {

    }

    //lateinit var mySupportActionBar: ActionBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_single)
        setSupportActionBar(toolbar)
        var bundle: Bundle? = intent.extras

        path = bundle!!.getString("path")
        var title = bundle.getString("title")
        FirebaseFirestore.getInstance().document(path!!).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                  classs  = document.toObject(Class::class.java)
                    // mySupportActionBar.title = classs!!.name
                    tv_class_single_teacher.text = classs!!.teacher!!.userName
                    tv_class_single_description.text = classs!!.description
                    sw_class_single_auto_join.isChecked = classs!!.isPublic
                    if(classs!!.teacher!!.email==MainActivity.user.email){
                        iAmTeacher = true
//                        if(mmenu!=null)
//                            mmenu.removeItem(R.id.action_leave)
                    } else {
                        btn_class_single_new_event.visibility = View.GONE
                    }
                    classTokens = classs!!.tokens


                    var q=FirebaseFirestore.getInstance().collection("Event").whereEqualTo("clss", classs)
                    eventAdapter = object : ClassSingleEventAdapter(q, this@ClassSingleActivity) {
                        override fun onDataChanged() {
                            // Show/hide content if the query returns empty.
                            Log.i("ClassFragment student", itemCount.toString())

                            tv_class_single_no_events.visibility = View.INVISIBLE
                        }

                        override fun onError(e: FirebaseFirestoreException) {
                            // Show a snackbar on errors
                            Log.e("ClassFragment", e.message)
                        }
                    }

                    eventRecycleView.adapter = eventAdapter
                    eventAdapter.startListening()


                } else {
                    // Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                //  Log.d(TAG, "get failed with ", exception)
            }

        fab.setOnClickListener { view ->


            getServerTimeStamp { a ->

                for (t in classTokens) {
                    if (t.user!!.email == MainActivity.user.email && t.expireDate!!.after(a.toDate())) {
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Bible-ly//:" + t.id)
                            putExtra(Intent.EXTRA_SUBJECT, "Join Bible-ly Class")
                        }.also { intent ->
                            val chooserIntent = Intent.createChooser(intent, "Bible-ly//:" + t.id)
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
                    putExtra(Intent.EXTRA_TEXT, "Bible-ly//:" + t.id)
                    putExtra(Intent.EXTRA_SUBJECT, "Join Bible-ly Class")
                }.also { intent ->
                    val chooserIntent = Intent.createChooser(intent, "Bible-ly//:" + t.id)
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
        studentAdapter = object : StudentAdapter(query, this@ClassSingleActivity) {
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
            val view = layoutInflater.inflate(R.layout.event_detailed_fragment, null)
            view.tf_event_frag_className.editText!!.setText(classs!!.name)
            view.tf_event_frag_className.isEnabled = false
            var dialoge = AlertDialog.Builder(this)
                .setTitle("New Event")
                .setCancelable(false)
                .setNegativeButton("Close", DialogInterface.OnClickListener { dialog, which ->
                    //Action goes here
                })
                .setPositiveButton("Create", DialogInterface.OnClickListener { dialog, which ->
                    var ev = Event(view.et_event_frag_title.text.toString(),view.et_event_frag_date.text.toString(),"",classs)
                    var ref = FirebaseFirestore.getInstance().collection("Event").document()
                    ref.set(ev)
                })
                .setView(view).create()

            dialoge.show()
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
//        eventAdapter.startListening()

    }

    override fun onStop() {
        super.onStop()
        studentAdapter.stopListening()
        eventAdapter.stopListening()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_class_single, menu)
        mmenu=menu

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_leave -> {

                var dialoge = AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Are you sure you want to leave?")
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                        //Action goes here
                    })
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                        FirebaseFirestore.getInstance()
                            .document(path).update(
                                "students",
                                FieldValue.arrayRemove(MainActivity.user)
                            )
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

    /* override fun onBackPressed() {

         this.finish()
            // super.onBackPressed()

     }*/

}