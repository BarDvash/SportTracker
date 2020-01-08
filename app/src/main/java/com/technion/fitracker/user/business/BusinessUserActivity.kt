package com.technion.fitracker.user.business

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.technion.fitracker.PendingRequestsActivity
import com.technion.fitracker.R
import com.technion.fitracker.SettingsActivity
import com.technion.fitracker.login.LoginActivity
import com.technion.fitracker.models.BusinessUserViewModel
import com.technion.fitracker.models.UserViewModel
import com.technion.fitracker.user.User

class BusinessUserActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var viewModel: BusinessUserViewModel

    //Google login token
    private val idToken = "227928727350-8scqikjnk6ta5lj5runh2o0dbd9p0nil.apps.googleusercontent.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_user)
        setSupportActionBar(findViewById(R.id.business_user_toolbar))
        viewModel = ViewModelProviders.of(this)[BusinessUserViewModel::class.java]
        navController = Navigation.findNavController(findViewById(R.id.business_fragment_host))
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            val docRef = firestore.collection("business_users").document(auth.currentUser!!.uid)
            docRef.get().addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                findViewById<TextView>(R.id.business_user_name).text = user?.name ?: "Username"

                viewModel.user_name = user?.name
                viewModel.user_photo_url = user?.photoURL
                viewModel.user_phone_number = user?.phone_number


                if (!user?.photoURL.isNullOrEmpty()) {
                    Glide.with(this) //1
                            .load(user?.photoURL)
                            .placeholder(R.drawable.user_avatar)
                            .error(R.drawable.user_avatar)
                            .skipMemoryCache(true) //2
                            .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                            .transform(CircleCrop()) //4
                            .into(findViewById(R.id.business_user_avatar))
                }
            }
        }
        findViewById<BottomNavigationView>(R.id.business_bottom_navigation).setOnNavigationItemSelectedListener(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(idToken)
                .requestEmail()
                .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(applicationContext, gso)


        createNotificationChannel()
        //subscribe to topics
        val topic1_name = "trainee_sent_request"+auth.currentUser!!.uid
        FirebaseMessaging.getInstance().subscribeToTopic(topic1_name)
        val topic2_name = "trainee_accepted_trainer_request"+auth.currentUser!!.uid
        FirebaseMessaging.getInstance().subscribeToTopic(topic2_name)
    }



    override fun onStop() {
        super.onStop()
        //TODO: questionable, might be hurting performance ?
        viewModel.notifications_adapter?.stopListening()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.business_user_activity_menu, menu)
        return true

    }


    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        navController.popBackStack()
        when (menuItem.itemId) {
            R.id.action_home -> navController.navigate(R.id.homeScreenFragment)
            R.id.action_customers -> navController.navigate(R.id.customersFragment)
            R.id.action_schedule -> navController.navigate(R.id.scheduleFragment)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) { //check on which item the user pressed and perform the appropriate action
            R.id.business_user_menu_logout_ac -> {


                //unsubscribe from topics
                val topic1_name = "trainee_sent_request"+auth.currentUser!!.uid
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic1_name)
                val topic2_name = "trainee_accepted_trainer_request"+auth.currentUser!!.uid
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic2_name)

                FirebaseAuth.getInstance().signOut()
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(this) {
                            startLoginActivity()
                        }
                true
            }

            R.id.search_from_buisness -> {
                onSearchRequested()
                true
            }

            R.id.business_user_menu_settings_ac -> {
                val userHome = Intent(applicationContext, SettingsActivity::class.java)
                startActivity(userHome)
                true
            }
            R.id.business_user_menu_pending_requests_ac -> {
                val userHome = Intent(applicationContext, PendingRequestsActivity::class.java)
                userHome.putExtra("user_type", "business")

                userHome.putExtra("user_name", viewModel.user_name)
                userHome.putExtra("user_photo_url",  viewModel.user_photo_url)

                startActivity(userHome)
                true
            }


            else -> {
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun startLoginActivity() {
        val userHome = Intent(applicationContext, LoginActivity::class.java)
        startActivity(userHome)
        finish()
    }


    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val id = "M_CH_ID"
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onSearchRequested(): Boolean {
        val appData = Bundle().apply {
            putString("user_type","business")
            putString("user_name", viewModel.user_name )
            putString("user_photo_url", viewModel.user_photo_url)
            putString("user_phone_number_url", viewModel.user_phone_number)
        }
        startSearch(null, false, appData, false)
        return true
    }
}
