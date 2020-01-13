package com.technion.fitracker.user.business.customer

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.fitracker.R
import com.technion.fitracker.adapters.viewPages.TabsFragmentPagerAdapter
import com.technion.fitracker.models.CustomerDataViewModel
import kotlinx.android.synthetic.main.activity_view_customer_data.*

class ViewCustomerData : AppCompatActivity() {
    lateinit var navController: NavController
    lateinit var adapter: TabsFragmentPagerAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var viewModel: CustomerDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_customer_data)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        viewModel = ViewModelProviders.of(this)[CustomerDataViewModel::class.java]
        viewModel.customerID = intent.getStringExtra("customerID")
        viewModel.customerName = intent.getStringExtra("customerName")
        viewPager = findViewById(R.id.usersCategoryContent)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = "${viewModel.customerName}'s Data"
            setDisplayHomeAsUpEnabled(true)
        }
        adapter = TabsFragmentPagerAdapter(supportFragmentManager).apply {
            addFragment(CustomerMeasurementFragment(), "Feed")
            addFragment(CustomerWorkoutsFragment(), "Workouts")
            addFragment(CustomerNutritionFragment(), "Nutrition")
        }
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.customer_info_menu, menu)

        return true

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) { //check on which item the user pressed and perform the appropriate action

            R.id.delete_trainee -> {
                MaterialAlertDialogBuilder(this).setTitle("Warning").setMessage("Really remove " + viewModel.customerName + "from your customers?")
                        .setPositiveButton(
                                "Yes"
                        ) { _, _ ->
                            firebaseFirestore.collection("regular_users").document(viewModel.customerID!!).update("personal_trainer_uid", null)
                            firebaseFirestore.collection("business_users").document(firebaseAuth.currentUser?.uid!!).collection("customers")
                                    .document(viewModel.customerID!!).delete()
                            this.finish()

                        }
                        .setNegativeButton(
                                "No"
                        ) { _, _ ->
                        }.show()
                true
            }

            else -> {

                super.onOptionsItemSelected(item)
            }
        }
    }
}
