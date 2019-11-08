package edu.calbaptist.bible_ly

import android.content.Context
import android.content.DialogInterface
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
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.JsonObject
import java.util.*


private const val TAG = "UserFireStore"
private var currentDestination = R.id.nav_board

private val FCM_API = "https://fcm.googleapis.com/fcm/send"
private val serverKey = "key=" + "AAAA_Z8c2FM:APA91bFTvaDRR7T0VmD2NVKvmkUfWF5yU3ZFDsXsVUZnYD7wvHSk1rV3iU82kDd625Q5PKZDgYCWXpdsLN0tRkZePw00iu7ToIpD2Ixh5xYS6ku4uWSVqBhQ4H-lNURQZ-xSB9mz5vnK"
private val contentType = "application/json"

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    //private lateinit var database: DatabaseReference
    private lateinit var signOutButton: MaterialButton
    private lateinit var tvNavName: TextView
    private lateinit var tvNavEmail: TextView
    private lateinit var ivNav: ImageView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        var bundle: Bundle? = intent.extras



        currentDestination = bundle?.getInt("currentDestination") ?: R.id.nav_board



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
            val personEmail = acct.email
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
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        if(bundle != null)
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
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {



            R.id.action_settings -> {

                var TOPIC = "/topics/Events"; //topic must match with what the receiver subscribed to
                var NOTIFICATION_TITLE = "test from client"
                var NOTIFICATION_MESSAGE = "test from client mmm"

                var notification =  JSONObject()
                var notifcationBody =  JSONObject()
                try {
                    notifcationBody.put("title", NOTIFICATION_TITLE);
                    notifcationBody.put("message", NOTIFICATION_MESSAGE);

                    notification.put("to", TOPIC);
                    notification.put("data", notifcationBody);
                } catch (e: JSONException) {
                    Log.e(TAG, "onCreate: " + e.message );
                }
                sendNotification(notification);
                /*val TAG = "JSA-FCM"
                val SENDER_ID = "xxxxxxxxxxxx"
                val random = Random()
                val fm = FirebaseMessaging.getInstance()

                val message = RemoteMessage.Builder(SENDER_ID + "@gcm.googleapis.com")
                    .setMessageId(Integer.toString(random.nextInt(9999)))
                    .addData(edt_key1.text.toString(), edt_value1.text.toString())
                    .addData(edt_key2.text.toString(), edt_value2.text.toString())
                    .build()

                if (!message.data.isEmpty()) {
                    Log.e(TAG, "UpstreamData: " + message.data)
                }

                if (!message.messageId.isEmpty()) {
                    Log.e(TAG, "UpstreamMessageId: " + message.messageId)
                }

                fm.send(message)*/

            }

        }
        return super.onOptionsItemSelected(item)
    }
    private fun sendNotification(notification:JSONObject ) {
        var jsonObjectRequest  = object: JsonObjectRequest(
            Request.Method.POST, FCM_API, null,
            Response.Listener<JSONObject> { response ->
                Log.i(TAG, "onResponse: $response")
            },
            Response.ErrorListener {
                Toast.makeText(this, "That didn't work!", Toast.LENGTH_SHORT).show()
            })
        {
            override fun getHeaders(): MutableMap<String, String> {

                val headers = HashMap<String, String>()
                headers["Authorization"] = serverKey
                headers["Content-Type"] = contentType
                return headers
            }
        }
       // var jsonObjectRequest =  JsonObjectRequest("",notification,null,null)
       /* var jsonObjectRequest =  JsonObjectRequest("FCM_API", notification,
             Response.Listener<JSONObject>() {
                @Override
                void onResponse(JSONObject response) {
                    Log.i(TAG, "onResponse: " + response.toString());
                    edtTitle.setText("");
                    edtMessage.setText("");
                }
            },
            Response.ErrorListener() {
                @Override
                void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "Request error", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "onErrorResponse: Didn't work");
                }
            }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> params = new HashMap<>();
            params.put("Authorization", serverKey);
            params.put("Content-Type", contentType);
            return params;
        }
        };*/
        VolleySingleton.requestQueque.add(jsonObjectRequest)
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
