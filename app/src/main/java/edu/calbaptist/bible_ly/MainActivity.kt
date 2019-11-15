package edu.calbaptist.bible_ly

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.common.api.Response
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONException
import org.json.JSONObject
import com.android.volley.AuthFailureError;
import com.android.volley.Request
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


private const val TAG = "UserFireStore"
private var currentDestination = R.id.nav_board



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

        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            val personName = acct.displayName
            val personGivenName = acct.givenName
            val personFamilyName = acct.familyName
            val personEmail = acct.email//+"1111"
            val personId = acct.id
            val personPhoto = acct.photoUrl

            val firestore = FirebaseFirestore.getInstance()
            val usersRef = firestore.collection("User").document(personEmail!!)
            /*   // Add restaurant
               batch.set(eventRef, event)*/
            user = User(personName!!,personGivenName!!,personFamilyName!!,personEmail,personPhoto.toString()!!)
            usersRef.set( user)

        }



        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView  = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        //if(bundle != null)
             navController.navigate(currentDestination)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_board, R.id.nav_notes, R.id.nav_bible,
                R.id.classes, R.id.nav_calendar, R.id.nav_send
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
       menu.getItem(0).isVisible = false
       menu.getItem(1).isVisible = false
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
            R.id.action_settings -> {

                ll_bible_nav_notes.visibility = View.GONE
                ll_bible_nav_chat.visibility = View.VISIBLE


               // sendNotification("PEeMGAkbsMXXXbDz7lfE","hi","Hello",this)

                    if(drawer_layout.isDrawerOpen(Gravity.END)) {
                        //  drawer_layout.closeDrawer(Gravity.LEFT);
                      //  Toast.makeText(this,"yaaa",Toast.LENGTH_LONG).show()
                    }
                    else {
                        drawer_layout.openDrawer(Gravity.END);
                        //Toast.makeText(this,"yoo",Toast.LENGTH_LONG).show()
                    }


            }

            R.id.action_notes -> {

                ll_bible_nav_chat.visibility = View.GONE
                ll_bible_nav_notes.visibility = View.VISIBLE


                // sendNotification("PEeMGAkbsMXXXbDz7lfE","hi","Hello",this)

                if(drawer_layout.isDrawerOpen(Gravity.END)) {
                    //  drawer_layout.closeDrawer(Gravity.LEFT);
                    //Toast.makeText(this,"yaaa",Toast.LENGTH_LONG).show()
                }
                else {
                    drawer_layout.openDrawer(Gravity.END);
                   // Toast.makeText(this,"yoo",Toast.LENGTH_LONG).show()
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
        lateinit var user:User
        lateinit var mainMenu: Menu

    }

}
