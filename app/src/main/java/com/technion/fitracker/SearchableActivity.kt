package com.technion.fitracker

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.technion.fitracker.adapters.SearchFireStoreAdapter
import com.technion.fitracker.adapters.viewPages.TabsFragmentPagerAdapter
import com.technion.fitracker.models.SearchFireStoreModel
import kotlinx.android.synthetic.main.search_activity.*


class SearchableActivity : AppCompatActivity() {

    //activity's variables declaration:
    lateinit var tabs_adapter: TabsFragmentPagerAdapter
    private lateinit var view_pager: ViewPager

    var current_user_type: String? = null
    var current_user_name: String? = null
    var current_user_photo_url: String? = null
    var current_user_phone_number: String? = null

    lateinit var trainees_results_fragment: TraineeSearchResultsFragment
    lateinit var trainers_results_fragment: TrainerSearchResultsFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) //calling the overridden onCreate function
        setContentView(R.layout.search_activity) //set the relevant activity layout
        setSupportActionBar(findViewById(R.id.search_results_toolbar))

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        //initialize the activity's variable:

        current_user_type = intent.getBundleExtra(SearchManager.APP_DATA)?.getString("user_type")
        current_user_name = intent.getBundleExtra(SearchManager.APP_DATA)?.getString("user_name")
        current_user_photo_url = intent.getBundleExtra(SearchManager.APP_DATA)?.getString("user_photo_url")
        current_user_phone_number = intent.getBundleExtra(SearchManager.APP_DATA)?.getString("user_phone_number")

        view_pager = findViewById(R.id.search_results_Content)
        tabs_adapter = TabsFragmentPagerAdapter(supportFragmentManager).apply {
            trainees_results_fragment = TraineeSearchResultsFragment()
            trainers_results_fragment = TrainerSearchResultsFragment()
            addFragment(trainees_results_fragment, "Users")
            addFragment(trainers_results_fragment, "Trainers")
        }
        view_pager.adapter = tabs_adapter
        search_results_tabs.setupWithViewPager(view_pager)

    }


    /**If the current activity is the searchable activity and if we set android:launchMode to "singleTop",
     * then the searchable activity receives the ACTION_SEARCH intent with a call to onNewIntent(Intent),
     * passing the new ACTION_SEARCH intent here.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)//calling the overridden onCreate function
        setIntent(intent) // this line make sure that the intent saved by the activity is updated in case you call getIntent() in the future
        trainees_results_fragment.handleIntent(intent)
        trainers_results_fragment.handleIntent(intent)
    }


    /**
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    super.onCreateOptionsMenu(menu)
    menuInflater.inflate(R.menu.search_menu, menu)
    return true

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) { //check on which item the user pressed and perform the appropriate action

    R.id.search_from_searchActivity -> {
    onSearchRequested()
    true
    }

    else -> {
    // If we got here, the user's action was not recognized.
    // Invoke the superclass to handle it.
    super.onOptionsItemSelected(item)
    }
    }
    }

     **/

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

