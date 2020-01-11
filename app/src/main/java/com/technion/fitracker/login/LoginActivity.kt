package com.technion.fitracker.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.technion.fitracker.R

class LoginActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        navController = Navigation.findNavController(findViewById(R.id.user_navigation_host))
    }

    override fun onBackPressed() {
        if(navController.currentDestination?.label == "fragment_sign_in"){
            super.onBackPressed()
        }
        navController.popBackStack()
        navController.navigate(R.id.signInFragment)
    }
}
