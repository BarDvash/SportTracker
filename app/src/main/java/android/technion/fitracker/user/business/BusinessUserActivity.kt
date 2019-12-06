package android.technion.fitracker.user.business

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.login.LoginActivity
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class BusinessUserActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_user)
        setSupportActionBar(findViewById(R.id.business_user_toolbar))
        navController = Navigation.findNavController(findViewById(R.id.business_fragment_host))
        findViewById<BottomNavigationView>(R.id.business_bottom_navigation).setOnNavigationItemSelectedListener(this)
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
            startLoginActivity()
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
