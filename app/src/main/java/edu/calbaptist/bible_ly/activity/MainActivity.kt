package edu.calbaptist.bible_ly.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.common.api.Response
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FirebaseFirestoreSettings
import edu.calbaptist.bible_ly.FirestoreRepository
import edu.calbaptist.bible_ly.R
import edu.calbaptist.bible_ly.User
import edu.calbaptist.bible_ly.ui.event.EventDialog

import edu.calbaptist.bible_ly.ui.notes.NoteDialog
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


private const val TAG = "UserFireStore"
private var currentDestination = R.id.nav_board
private var previousDestination = -1
private var mainMenu: Menu? = null
lateinit var navController: NavController


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    //private lateinit var database: DatabaseReference
    private lateinit var signOutButton: MaterialButton
    private lateinit var tvNavName: TextView
    private lateinit var tvNavEmail: TextView
    private lateinit var ivNav: ImageView
    private lateinit var navView: NavigationView





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        var bundle: Bundle? = intent.extras


        currentDestination = bundle?.getInt("currentDestination") ?: R.id.nav_board

        if(currentDestination == 0)
            currentDestination =
                R.id.nav_board

        var db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings


//        FirebaseInstanceId.getInstance().instanceId
//            .addOnCompleteListener(OnCompleteListener { task ->
//                if (!task.isSuccessful) {
//                    Log.w(TAG, "getInstanceId failed", task.exception)
//                    return@OnCompleteListener
//                }
//
//                // Get new Instance ID token
//                val token = task.result?.token
//
//
//                // Log and toast
////                val msg = getString(R.string.msg_token_fmt, token)
////                Log.d(TAG, msg)
//                //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//            })

        //var a = getCurrentActivity(this)
        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            val personName = acct.displayName
            val personGivenName = acct.givenName
            val personFamilyName = acct.familyName
            val personEmail = acct.email// +"1111"
            val personId = acct.id
            val personPhoto = acct.photoUrl

            val firestore = FirebaseFirestore.getInstance()
            val usersRef = firestore.collection("User").document(personEmail!!)
            /*   // Add restaurant
               batch.set(eventRef, event)*/
            user =
                User(
                    personName!!,
                    personGivenName!!,
                    personFamilyName!!,
                    personEmail,
                    personPhoto.toString()!!
                )
            usersRef.set(user)
            FirebaseMessaging.getInstance().subscribeToTopic("SendToUser_"+personEmail.replace("@","_"))
        }




        drawerLayout = findViewById(R.id.drawer_layout)
        navView  = findViewById(R.id.nav_view)
//        navView.setNavigationItemSelectedListener{ menuItem ->
//            if(menuItem.itemId == R.id.nav_share)
//                Toast.makeText(this,"HAHA",Toast.LENGTH_SHORT).show()
//            // close drawer when item is tapped
//               drawerLayout.closeDrawers()
//
//            // Add code here to update the UI based on the item selected
//            // For example, swap UI fragments here
//
//            true
//        }
        navView2 = findViewById(R.id.nav_view2)
        navController = findNavController(R.id.nav_host_fragment)


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_board, R.id.nav_bible,
                R.id.nav_classes, R.id.nav_share
            ), drawerLayout
        )
        //navController.navigate( R.id.classes)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        var navHeader =navView.getHeaderView(0)
        signOutButton = navHeader.findViewById(R.id.nav_sign_out)
        tvNavName = navHeader.findViewById(R.id.tvNavName)
        tvNavEmail = navHeader.findViewById(R.id.tvNavEmail)
        ivNav = navHeader.findViewById(R.id.ivNav)
        signOutButton.setOnClickListener {
            signOut()
        }
        updateNavHeader()


        if(currentDestination !=0) {

            if (currentDestination == R.id.nav_classes) {
                mainMenu?.getItem(0)?.isVisible = false
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END)
                navController.navigate(currentDestination)
            }
            //when user clicks on new comment notification
            if (currentDestination == R.id.nav_bible) {
                mainMenu?.getItem(0)?.isVisible = true
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END)
                drawer_layout.openDrawer(GravityCompat.END)

                var noteID = bundle?.getString("noteID") ?: ""
                if(noteID!="") {
                    FirestoreRepository().getNote(noteID){ notee ->
                        var bundle = bundleOf("bookNum" to notee.book)
                        navController.navigate(currentDestination,bundle)
                        var item = notee
                        FirestoreRepository()
                            .getBookName(item.book.toInt()){ bookName ->
                            var d = NoteDialog.newInstance(
                                false,
                                noteID,
                                item.book,
                                item.verseNum,
                                item.verseChapter,
                                item.verseText,
                                bookName
                                )
                            val fm = supportFragmentManager
                            d.show(fm, "NoteDialog")
                        }

                    }

                }
            }
            if (currentDestination == R.id.nav_board) {
                mainMenu?.getItem(0)?.isVisible = false
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END)
                var eventID = bundle?.getString("eventID") ?: ""
                var classID = bundle?.getString("classID") ?: ""
                if(eventID!="") {

                    var d = EventDialog.newInstance(false, false , classID,eventID)
                    val fm = supportFragmentManager
                    d.show(fm,"EventDialog")
                }
                navController.navigate(currentDestination)
            }

            nav_view.setNavigationItemSelectedListener { menuItem ->


                when (menuItem.itemId) {

                    R.id.nav_board -> {
                        fragmentManager.popBackStack()
                        navController.navigate(R.id.nav_board)
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END)
                        mainMenu?.getItem(0)?.isVisible = false
//                        Toast.makeText(this,mainMenu?.getItem(0)?.isVisible.toString(),Toast.LENGTH_SHORT).show()
                        previousDestination =
                            currentDestination
                        currentDestination =
                            R.id.nav_board
                    }

                    R.id.nav_bible -> {
                        fragmentManager.popBackStack()
                        navController.navigate(R.id.nav_bible)
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END)
                        mainMenu?.getItem(0)?.isVisible = true
//                      Toast.makeText(this,mainMenu?.getItem(0)?.isVisible.toString(),Toast.LENGTH_SHORT).show()
                        previousDestination =
                            currentDestination
                        currentDestination =
                            R.id.nav_bible
                    }

                    R.id.nav_classes -> {
                        fragmentManager.popBackStack()
                        navController.navigate(R.id.nav_classes)
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END)
                        mainMenu?.getItem(0)?.isVisible = false
//                        Toast.makeText(this,mainMenu?.getItem(0)?.isVisible.toString(),Toast.LENGTH_SHORT).show()
                        previousDestination =
                            currentDestination
                        currentDestination =
                            R.id.nav_classes
                    }

                    R.id.nav_share -> { /*Toast.makeText(this,"For sharing Links",Toast.LENGTH_SHORT).show()*/
                       // navController.navigate(R.id.nav_bible,bundle)
                    }

                }
                drawerLayout.closeDrawers()
                true
            }


        }

    }


    fun updateNavHeader(){
        tvNavName.text = user.userName
        tvNavEmail.text = user.email
        Glide.with(ivNav.context)
            .load(user.photoID)
            .apply(RequestOptions.circleCropTransform())
            .into(ivNav)
    }

   override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
       mainMenu = menu
       if(currentDestination != R.id.nav_bible)
            menu.getItem(0).isVisible = false
    /*    if (currentDestination ==  R.id.nav_bible)
            menu.removeItem(R.id.action_settings)*/
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {



                  /*  android.R.id.home->{
                if(drawer_layout.isDrawerOpen(Gravity.END)) {
                  //  drawer_layout.closeDrawer(Gravity.LEFT);
                    Toast.makeText(this,"yaaa",Toast.LENGTH_LONG).show()
                }
                else {
                    drawer_layout.openDrawer(Gravity.END);
                    Toast.makeText(this,"yoo",Toast.LENGTH_LONG).show()
                }
            }*/

            R.id.action_notes -> {

                ll_bible_nav_chat.visibility = View.GONE
                ll_bible_nav_notes.visibility = View.VISIBLE


                // sendNotification("PEeMGAkbsMXXXbDz7lfE","hi","Hello",this)

                val orientation = resources.configuration.orientation
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END)

                    var ll = findViewById<LinearLayout>(R.id.ll_bible_nav_notes_land)
                        ll.let {
                        when(it.visibility){
                            View.GONE -> it.visibility = View.VISIBLE
                            View.VISIBLE -> it.visibility = View.GONE
                        }  }

                } else {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END)
                    if(drawer_layout.isDrawerOpen(GravityCompat.END)) {
                        //  drawer_layout.closeDrawer(Gravity.LEFT);
                        //Toast.makeText(this,"yaaa",Toast.LENGTH_LONG).show()
                    }
                    else {
                        drawer_layout.openDrawer(GravityCompat.END)
                        // Toast.makeText(this,"yoo",Toast.LENGTH_LONG).show()
                    }

                }



            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun signOut() {
        startActivity(SignInActivity.getLaunchIntent(this))
        FirebaseAuth.getInstance().signOut()
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        lateinit var user: User
         ////var mainMenu: Menu? = null
        lateinit var navView2: NavigationView
        lateinit var drawerLayout: DrawerLayout
        var currentNoteID = ""

        fun navigateDrawer(){
            navController.navigate(R.id.nav_bible)
            doAsync {
                Thread.sleep(200)
                uiThread {
                    drawerLayout.closeDrawer(GravityCompat.END)
                }
            }

        }

    }

    override fun onBackPressed() {



        if( drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        } else if (drawer_layout.isDrawerOpen(GravityCompat.END)) {
            drawer_layout.closeDrawer(GravityCompat.END)
        } else {
            if(currentDestination == R.id.nav_board)
                this.finish()
            else {

                currentDestination =
                    previousDestination
                super.onBackPressed()
            }

        }

        //additional code


    }


}
