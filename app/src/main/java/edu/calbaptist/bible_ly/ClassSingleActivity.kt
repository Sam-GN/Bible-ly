package edu.calbaptist.bible_ly

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



private lateinit var title: String
private lateinit var  classesAsStudentRecycleView: RecyclerView
lateinit var firestore: FirebaseFirestore
lateinit var studentAdapter: StudentAdapter
lateinit var query: Query
private lateinit var studentLinearLayoutManager: LinearLayoutManager


class ClassSingleActivity : AppCompatActivity(), StudentAdapter.OnStudentItemSelectedListener {
    override fun onStudentItemSelected(StudentItem: DocumentSnapshot) {

    }

    //lateinit var mySupportActionBar: ActionBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_single)
        setSupportActionBar(toolbar)
        var bundle :Bundle ?=intent.extras
        var path = bundle!!.getString("path")
        var title = bundle!!.getString("title")
        FirebaseFirestore.getInstance().document(path).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    var classs = document.toObject(Class::class.java)
                   // mySupportActionBar.title = classs!!.name
                    tv_class_single_teacher.text = classs!!.teacher!!.userName

                } else {
                   // Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
              //  Log.d(TAG, "get failed with ", exception)
            }

        fab.setOnClickListener { view ->
            /*Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT,"text")
                putExtra(Intent.EXTRA_SUBJECT, "Share via")
            }.also { intent -> val chooserIntent = Intent.createChooser(intent,"sdcsdcdsc")
                startActivity(chooserIntent)
            }*/
            val map = mapOf("SSDSD" to FieldValue.serverTimestamp())

            FirebaseFirestore.getInstance().document(path).update("tokens",map)

        }
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        app_bar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {

                    val toolBarHeight = toolbar.measuredHeight
                    val appBarHeight = appBarLayout.getMeasuredHeight()
                    var f: Float =
                        (appBarHeight.toFloat() - toolBarHeight + verticalOffset) / (appBarHeight.toFloat() - toolBarHeight) * 255
                    if (f >=254 )
                        return
                    if (f < 40)
                        f = 0.0f

                    iv_class_single_logo.alpha=(f / 255)
//                    Log.v("asghar",""+(f))
//                    Log.v("asghar2",""+verticalOffset)
//                    Log.v("asghar3",""+(Math.round(f) -255 ))

            }
        })

        classesAsStudentRecycleView = findViewById(R.id.rv_class_single_students)
        studentLinearLayoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL ,false)
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
                Log.i("ClassFragment student",itemCount.toString())
                //updateUi()
            }

            override fun onError(e: FirebaseFirestoreException) {
                // Show a snackbar on errors
                Log.e("ClassFragment",e.message)
            }
        }

        classesAsStudentRecycleView.adapter = studentAdapter
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

    }

    override fun onStop() {
        super.onStop()
        studentAdapter.stopListening()

    }


    /* override fun onBackPressed() {

         this.finish()
            // super.onBackPressed()

     }*/
}
