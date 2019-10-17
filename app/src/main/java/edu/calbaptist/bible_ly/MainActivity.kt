package edu.calbaptist.bible_ly

import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
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
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import edu.calbaptist.bible_ly.util.ClassUtil
import edu.calbaptist.bible_ly.util.EventUtil
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable Bible-ly Logging
        FirebaseFirestore.setLoggingEnabled(true)

        // Bible-ly
        database = FirebaseFirestore.getInstance()

        // commented code below. fixing database
//        // the below code is for firebase testing purposes
//        database.child("users").addValueEventListener(object : ValueEventListener {
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//               /// val userr = snapshot as User
//                val userr =snapshot.children.mapNotNull { it.getValue<User>(User::class.java) }
//                Log.v("rrr",userr[0].userName)
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })
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

//        val fab: FloatingActionButton = findViewById(R.id.fab)
//
//
//        fab.setOnClickListener { view ->
//            // Write a message to the database
//
//// ...
//
//            val user = User(1, "Sam","123","")
//            database.child("users").child("1").setValue(user)
//        }
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

    private fun onAddItemsClicked() {
        // Add a bunch of random restaurants
        val batch = database.batch()
        for (i in 0..9) {
            val restRef = database.collection("classes").document()

            // Create random events / classes
            val randomClass = ClassUtil.getRandom(this)
            val randomRatings = EventUtil.getRandomList(randomClass.numEvents)
            randomRestaurant.avgRating = RatingUtil.getAverageRating(randomRatings)

            // Add restaurant
            batch.set(restRef, randomRestaurant)

            // Add ratings to subcollection
            for (rating in randomRatings) {
                batch.set(restRef.collection("ratings").document(), rating)
            }
        }

        batch.commit().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Write batch succeeded.")
            } else {
                Log.w(TAG, "write batch failed.", task.exception)
            }
        }
    }
}
