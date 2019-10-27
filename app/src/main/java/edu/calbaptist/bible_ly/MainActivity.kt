package edu.calbaptist.bible_ly

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore


private const val TAG = "UserFireStore"
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var database: DatabaseReference
    private lateinit var signOutButton: Button
    private lateinit var tvNavName: TextView
    private lateinit var tvNavEmail: TextView
    private lateinit var ivNav: ImageView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        database = FirebaseDatabase.getInstance().reference



        // the below code is for firebase testing purposes
        database.child("users").addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
               /// val userr = snapshot as User
                val userr =snapshot.children.mapNotNull { it.getValue<User>(User::class.java) }
                Log.v("rrr",userr[0].userName)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
//        val postListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // Get Post object and use the values to update the UI
//                val post = dataSnapshot.getValue(User::class.java)
//                Log.v("rrr",post!!.userName)
//                // ...
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w("readfrmfirebase", "loadPost:onCancelled", databaseError.toException())
//                // ...
//            }
//        }
//        database.child("users").addValueEventListener(postListener)



        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            val personName = acct.displayName
            val personGivenName = acct.givenName
            val personFamilyName = acct.familyName
            val personEmail = acct.email
            val personId = acct.id
            val personPhoto = acct.photoUrl

            val firestore = FirebaseFirestore.getInstance()
            val usersRef = firestore.collection("User").document(personEmail!!)
            /*   // Add restaurant
               batch.set(eventRef, event)*/
            user = User(personName!!,personGivenName!!,personFamilyName!!,personEmail,personPhoto.toString()!!)
            usersRef.set( user)
           /* usersRef.get()
                .addOnSuccessListener { document ->
                 *//*   if (document != null) {
                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    } else {
                        Log.d(TAG, "No such document")
                     //   val usersRef = firestore.collection("User").document()*//*
                        firestore.batch().set(usersRef, user)
                //    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }*/
        }



        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_board, R.id.nav_notes, R.id.nav_bible,
                R.id.classes, R.id.nav_calendar, R.id.nav_send
            ), drawerLayout
        )
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
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun signOut() {
        startActivity(SignInActivity.getLaunchIntent(this))
        FirebaseAuth.getInstance().signOut();
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        lateinit var user:User

    }
}
