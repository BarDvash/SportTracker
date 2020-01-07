package com.technion.fitracker.user.business.customer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.fitracker.R
import com.technion.fitracker.adapters.viewPages.ExerciseTypeViewPageAdapter
import com.technion.fitracker.models.CustomerDataViewModel
import com.technion.fitracker.user.personal.workout.WorkoutsFragment
import kotlinx.android.synthetic.main.activity_view_customer_data.*

class ViewCustomerData : AppCompatActivity() {
    lateinit var navController: NavController
    lateinit var adapter: ExerciseTypeViewPageAdapter
    private lateinit var viewPager: ViewPager
    lateinit var viewModel: CustomerDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_customer_data)
        viewModel = ViewModelProviders.of(this)[CustomerDataViewModel::class.java]
        viewModel.customerID =  intent.getStringExtra("customerID")
        viewModel.customerName =  intent.getStringExtra("customerName")
        viewPager = findViewById(R.id.usersCategoryContent)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = "${viewModel.customerName}'s Data"
            setDisplayHomeAsUpEnabled(true)
        }
        adapter = ExerciseTypeViewPageAdapter(supportFragmentManager).apply {
            addFragment(CustomerWorkouts(), "Workouts")
            addFragment(CustomerWorkouts(), "Nutrition")
            addFragment(CustomerWorkouts(), "Measurements")
        }
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
    }


}
