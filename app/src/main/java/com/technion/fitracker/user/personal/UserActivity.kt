package com.technion.fitracker.user.personal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.ImageView
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
import com.technion.fitracker.models.UserViewModel
import com.technion.fitracker.user.User


class UserActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var historyAction: MenuItem
    lateinit var addAction: MenuItem
    lateinit var viewModel: UserViewModel

    //Google login token
    private val idToken = "227928727350-8scqikjnk6ta5lj5runh2o0dbd9p0nil.apps.googleusercontent.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[UserViewModel::class.java]
        setContentView(R.layout.activity_user)
        setSupportActionBar(findViewById(R.id.user_toolbar))
        navController = Navigation.findNavController(findViewById(R.id.user_navigation_host))
        auth = FirebaseAuth.getInstance()

        val userAvatar = findViewById<ImageView>(R.id.user_avatar)
        val userName = findViewById<TextView>(R.id.user_name)
        userAvatar.animation = AnimationUtils.loadAnimation(this,R.anim.user_avatar_anim)
        userName.animation = AnimationUtils.loadAnimation(this,R.anim.fab_transition)

        firestore = FirebaseFirestore.getInstance()
        if (auth.currentUser != null) {
            val docRef = firestore.collection("regular_users").document(auth.currentUser!!.uid)
            docRef.get().addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                findViewById<TextView>(R.id.user_name).text = user?.name ?: "Username"

                viewModel.personalTrainerUID = user?.personal_trainer_uid
                viewModel.user_name = user?.name
                viewModel.user_photo_url = user?.photoURL

                if (!user?.photoURL.isNullOrEmpty()) {
                    Glide.with(this) //1
                            .load(user?.photoURL)
                            .placeholder(R.drawable.user_avatar)
                            .error(R.drawable.user_avatar)
                            .skipMemoryCache(true) //2
                            .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                            .transform(CircleCrop()) //4
                            .into(userAvatar)

                }
            }
        }
        findViewById<BottomNavigationView>(R.id.user_bottom_navigation).setOnNavigationItemSelectedListener(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(idToken)
                .requestEmail()
                .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(applicationContext, gso)

        createNotificationChannel()
        //subscribe to unique topic:
        val topic1_name = "trainer_accept_trainee_request_push_notification"+auth.currentUser!!.uid
        FirebaseMessaging.getInstance().subscribeToTopic(topic1_name)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.user_activity_menu, menu)
        historyAction = menu?.findItem(R.id.measurements_history)!!
        addAction = menu.findItem(R.id.measurements_add)
        return true

    }


    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        val dest = navController.currentDestination?.label
        when (menuItem.itemId) {
            R.id.action_home -> {
                if(dest == "HomeScreen"){
                    return true
                }
                startFragmentAndPop(R.id.homeScreenFragment)
            }
            R.id.action_workouts -> {
                if(dest == "fragment_workouts"){
                    return true
                }
                startFragmentAndPop(R.id.workoutsFragment)
            }
            R.id.action_nutrition -> {
                if(dest == "fragment_nutrition"){
                    return true
                }
                startFragmentAndPop(R.id.nutritionFragment)
            }
            R.id.action_measurements -> {
                if(dest == "fragment_measurements"){
                    return true
                }
                startFragmentAndPop(R.id.measurementsFragment, true)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) { //check on which item the user pressed and perform the appropriate action
            R.id.user_menu_logout_ac -> {

                //unsubscribe from topics
                val topic1_name = "trainer_accept_trainee_request_push_notification"+auth.currentUser!!.uid
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic1_name)

                FirebaseAuth.getInstance().signOut()
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(this) {
                            startLoginActivity()
                        }
                true
            }

            R.id.search_from_personal -> {
                onSearchRequested()
                true
            }

            R.id.user_menu_settings_ac -> {
                val userHome = Intent(applicationContext, SettingsActivity::class.java)
                startActivity(userHome)
                true
            }

            R.id.user_menu_pending_requests_ac -> {
                val userHome = Intent(applicationContext, PendingRequestsActivity::class.java)
                userHome.putExtra("user_type", "regular")


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

    override fun onStop() {
        super.onStop()
        //TODO: questionable, might be hurting performance ?
        viewModel.homeRecentWorkoutsAdapter?.stopListening()
        viewModel.nutritionAdapter?.stopListening()
        viewModel.workoutsAdapter?.stopListening()
    }

    private fun startLoginActivity() {
        val userHome = Intent(applicationContext, LoginActivity::class.java)
        startActivity(userHome)
        finish()
    }

    private fun startFragmentAndPop(
        id: Int,
        historyActionVisible: Boolean = false,
        addActionVisible: Boolean = false,
        arrowBackVisible: Boolean = false
    ) {
        userActivityPopBackStack(arrowBackVisible, addActionVisible, historyActionVisible)
        navController.navigate(id)
    }

    fun userActivityPopBackStack(
        arrowBackVisible: Boolean,
        addActionVisible: Boolean,
        historyActionVisible: Boolean
    ) {
        supportActionBar?.setDisplayHomeAsUpEnabled(arrowBackVisible)
        addAction.isVisible = addActionVisible
        historyAction.isVisible = historyActionVisible
        navController.popBackStack()
    }

    fun userActivityStartFragment(
        id: Int,
        historyActionVisible: Boolean = false,
        addActionVisible: Boolean = false,
        arrowBackVisible: Boolean = false
    ) {
        supportActionBar?.setDisplayHomeAsUpEnabled(arrowBackVisible)
        addAction.isVisible = addActionVisible
        historyAction.isVisible = historyActionVisible
        navController.navigate(id)
    }

    override fun onSupportNavigateUp(): Boolean {
        userActivityPopBackStack(false, false, true)
        return true
    }

    override fun onBackPressed() {
        onSupportNavigateUp()
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
}
