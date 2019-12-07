package android.technion.fitracker.user.business

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.login.LoginActivity
import android.technion.fitracker.user.User
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
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

class BusinessUserActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    //Google login token
    private val idToken = "227928727350-8scqikjnk6ta5lj5runh2o0dbd9p0nil.apps.googleusercontent.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_user)
        setSupportActionBar(findViewById(R.id.business_user_toolbar))
        navController = Navigation.findNavController(findViewById(R.id.business_fragment_host))
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            if(auth.currentUser!!.photoUrl != null){
                Glide.with(this) //1
                    .load(auth.currentUser!!.photoUrl)
                    .placeholder(R.drawable.user_avatar)
                    .error(R.drawable.user_avatar)
                    .skipMemoryCache(true) //2
                    .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                    .transform(CircleCrop()) //4
                    .into(findViewById(R.id.business_user_avatar))
            }
            val docRef = firestore.collection("users").document(auth.currentUser!!.uid)
            docRef.get().addOnSuccessListener {
                    document ->
                val user = document.toObject(User::class.java)
                findViewById<TextView>(R.id.business_user_name).text = user?.name ?: "Username"
            }
        }
        findViewById<BottomNavigationView>(R.id.business_bottom_navigation).setOnNavigationItemSelectedListener(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(idToken)
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(applicationContext, gso)
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

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.business_user_menu_logout_ac -> {
            FirebaseAuth.getInstance().signOut()
            mGoogleSignInClient.signOut()
                .addOnCompleteListener(this) {
                    startLoginActivity()
                }
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun startLoginActivity() {
        val userHome = Intent(applicationContext, LoginActivity::class.java)
        startActivity(userHome)
        finish()
    }
}
