package edu.calbaptist.bible_ly

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
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
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import edu.calbaptist.bible_ly.adapter.ClassSingleEventAdapter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.collections.ArrayList


private lateinit var  studentRecycleView: RecyclerView
private lateinit var  eventRecycleView: RecyclerView
lateinit var studentAdapter: StudentAdapter
var eventAdapter: ClassSingleEventAdapter?=null
private lateinit var studentLinearLayoutManager: LinearLayoutManager
private lateinit var eventLinearLayoutManager: LinearLayoutManager
private lateinit var classTokens: ArrayList<Token>

private lateinit var classID: String
private var iAmTeacher: Boolean = false
private var mmenu: Menu? = null



class ClassSingleActivity : AppCompatActivity()
    , StudentAdapter.OnStudentItemSelectedListener
    , StudentAdapter.OnMoreItemSelectedListener
    , ClassSingleEventAdapter.OnClassSingleEventItemSelectedListener ,ClassDialog.Callback {
    override fun onClassCreated() {
        reload()
    }

    override fun onMoreItemSelected(view: View, StudentItem: DocumentSnapshot) {
        if (iAmTeacher) {
            showPopupStudent(view, StudentItem)
        }
    }

    private fun showPopupStudent(view: View, snapshot: DocumentSnapshot) {
        var popup: PopupMenu? = null;
        popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_students_item_more)


        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.students_item_action_delete -> {

                    var dialoge = AlertDialog.Builder(view.context)
                        .setCancelable(false)
                        .setTitle("Are you sure you want to remove the student?")
                        .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                            //Action goes here
                        })
                        .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                            val biblelyStudent = snapshot.toObject(User::class.java)
                            FirestoreRepository().removeStudentFromClass(classID,biblelyStudent!!.email)
                            Toast.makeText(view.context, "Student Removed", Toast.LENGTH_SHORT)
                                .show()

                        })
                        .create()

                    dialoge.show()

                }
                R.id.students_item_action_promote -> {

                   var tv = TextView(this)
                    tv.setPadding(10.toDp(Resources.getSystem().displayMetrics),10.toDp(Resources.getSystem().displayMetrics),0,0)
                    tv.text = "Are you sure you want to promote this student to teacher?\n" +
                            "You will be removed from the class"
                    var dialoge = AlertDialog.Builder(view.context)
                        .setCancelable(false)
                        .setTitle("Caution")
                        .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->

                        })
                        .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                            val biblelyStudent = snapshot.toObject(User::class.java)
                            FirestoreRepository().promoteStudentAsTeacher(classID,biblelyStudent!!)
                            Toast.makeText(view.context, "Student Promoted", Toast.LENGTH_SHORT)
                                .show()
                           returnToMainActivity()
                        })
                        .setView(tv)
                        .create()

                    dialoge.show()
                }
            }
            true
        })
        popup.show()
    }

    override fun onClassSingleEventItemSelected(ClassSingleEventItem: DocumentSnapshot) {
        var d =
            EventDialog.newInstance(false, iAmTeacher, classID, ClassSingleEventItem.reference.path)
        val fm = supportFragmentManager
        d.show(fm, "EventDialog")
    }

    override fun onStudentItemSelected(StudentItem: DocumentSnapshot) {
        var d = StudentDialog.newInstance(StudentItem.reference.path)
        val fm = supportFragmentManager
        d.show(fm, "StudentDialog")
    }

    //lateinit var mySupportActionBar: ActionBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_single)
        setSupportActionBar(toolbar)
        var bundle: Bundle? = intent.extras


        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        classID = bundle!!.getString("classID") ?: "noClass"
        reload()
        iv_class_single_teacher.setOnClickListener {

            var d = StudentDialog.newInstance("User/" + classs!!.teacher!!.email)
            val fm = supportFragmentManager
            d.show(fm, "StudentDialog")
        }

        tv_class_single_teacher.setOnClickListener {

            var d = StudentDialog.newInstance("User/" + classs!!.teacher!!.email)
            val fm = supportFragmentManager
            d.show(fm, "StudentDialog")
        }

        fab.setOnClickListener { view ->
            getServerTimeStamp { a ->
                for (t in classTokens) {
                    if (t.user!!.email == MainActivity.user.email && t.expireDate!!.after(a.toDate())) {
                        shareIntent(
                            this,
                            "Join Bible-ly with Token: " + t.id,
                            "Join Bible-ly BiblelyClass",
                            "Invite with token: " + t.id
                        )
                        return@getServerTimeStamp
                    }
                }

                val t = Token(
                    addDay(a, 2),
                    UUID.randomUUID().toString().substring(0, 8),
                    MainActivity.user
                )
                FirestoreRepository().updateClassToken(classID, t)
                shareIntent(
                    this,
                    "Join Bible-ly with Token: " + t.id,
                    "Join Bible-ly BiblelyClass",
                    "Invite with token: " + t.id
                )

            }
        }
        app_bar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {

                val toolBarHeight = toolbar.measuredHeight
                val appBarHeight = appBarLayout.getMeasuredHeight()
                var f: Float =
                    (appBarHeight.toFloat() - toolBarHeight + verticalOffset) / (appBarHeight.toFloat() - toolBarHeight) * 255
                if (f >= 254)
                    return
                if (f >= 180)
                    f = 255.0f

                if (f < 40)
                    f = 0.0f

                iv_class_single_logo.alpha = (f / 255)
            }
        })

        studentRecycleView = findViewById(R.id.rv_class_single_students)
        studentLinearLayoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL, false
        )
        studentRecycleView.layoutManager = studentLinearLayoutManager


        studentAdapter = object : StudentAdapter(
            FirestoreRepository().getStudentsInClassQuery(
                classID
            ), this@ClassSingleActivity, this@ClassSingleActivity
        ) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    tv_class_single_no_students.visibility = View.VISIBLE
                } else {
                    tv_class_single_no_students.visibility = View.GONE
                }
            }
        }

        studentRecycleView.adapter = studentAdapter


        eventRecycleView = findViewById(R.id.rv_class_single_events)
        eventLinearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        eventRecycleView.layoutManager = eventLinearLayoutManager





        btn_class_single_new_event.setOnClickListener {

            var d = EventDialog.newInstance(true, iAmTeacher, classID, "")
            val fm = supportFragmentManager
            d.show(fm, "EventDialog")
        }

    }

    private fun reload() {
        FirestoreRepository().getClass(classID) {
            classs = it
            tv_class_single_teacher.text = classs!!.teacher!!.userName
            tv_class_single_description.text = classs!!.description
            iv_class_single_teacher.setGlide(classs!!.teacher!!.photoID, true)
            iv_class_single_logo.setGlide(classs!!.classLogo, false)
            if (classs!!.teacher!!.email == MainActivity.user.email) {
                iAmTeacher = true
            } else {
                btn_class_single_new_event.visibility = View.GONE
                iAmTeacher = false
            }
            toolbar_layout.title = classs!!.name

            doAsync {
                Thread.sleep(200)
                uiThread {
                    if (mmenu != null) {
                        if (iAmTeacher)
                            mmenu!!.removeItem(R.id.action_leave_class)
                        else {
                            mmenu!!.removeItem(R.id.action_edit_class)
                            mmenu!!.removeItem(R.id.action_delete_class)
                        }
                    }
                }
            }

            classTokens = classs!!.tokens


            eventAdapter = object : ClassSingleEventAdapter(
                FirestoreRepository().getEventsInClassQuery(
                    classID
                ), this@ClassSingleActivity
            ) {
                override fun onDataChanged() {
                    if (itemCount != 0)
                        tv_class_single_no_events.visibility = View.INVISIBLE
                    else
                        tv_class_single_no_events.visibility = View.VISIBLE
                }

                override fun onError(e: FirebaseFirestoreException) {
                    Log.e("ClassFragment", e.message)
                }
            }
            eventRecycleView.adapter = eventAdapter
            eventAdapter!!.startListening()
        }

    }


    override fun onStart() {
        super.onStart()
        studentAdapter.startListening()
        if (eventAdapter != null)
            eventAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        studentAdapter.stopListening()
        eventAdapter!!.stopListening()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_class_single, menu)
        mmenu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                returnToMainActivity()
            }
            R.id.action_edit_class -> {
                val ft = supportFragmentManager.beginTransaction()
                val prev = supportFragmentManager.findFragmentByTag("dialog")
                if (prev != null) {
                    ft.remove(prev)
                }

                var d = ClassDialog.newInstance(false, classID)
                ft.addToBackStack(null)
                d.show(ft, "dialog")
            }
            R.id.action_delete_class -> {

                var dialoge = AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Are you sure you want to delete this class?")
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                        //Action goes here
                    })
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                        FirestoreRepository().deleteClass(classs!!.classID) {
                            if (it) {
                                Toast.makeText(this, "Class deleted", Toast.LENGTH_SHORT).show()
                                returnToMainActivity()
                            } else
                                Toast.makeText(
                                    this,
                                    "You can only delete empty classes",
                                    Toast.LENGTH_SHORT
                                ).show()
                        }
                    })
                    .create()
                dialoge.show()
            }
            R.id.action_leave_class -> {

                var dialoge = AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Are you sure you want to leave?")
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->

                    })
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                        FirestoreRepository().leaveClass(classID)
                        returnToMainActivity()
                    })
                    .create()

                dialoge.show()
            }

        }
        return super.onOptionsItemSelected(item)
    }


    private fun returnToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("currentDestination", R.id.nav_classes)
        startActivity(intent)
        this.finish()
    }

    override fun onBackPressed() {
        returnToMainActivity()

    }



    companion object {
        var classs: BiblelyClass? = null
    }
}