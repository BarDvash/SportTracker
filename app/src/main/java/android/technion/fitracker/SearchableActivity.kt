package android.technion.fitracker


import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.technion.fitracker.adapters.SearchFireStoreAdapter
import android.technion.fitracker.models.SearchFireStoreModel
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query




class SearchableActivity : AppCompatActivity() {
    lateinit var firestore: FirebaseFirestore
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FirestoreRecyclerAdapter<SearchFireStoreModel, SearchFireStoreAdapter.ViewHolder>
    lateinit var tab_layout: TabLayout


    //Google login token
    private val idToken = "227928727350-8scqikjnk6ta5lj5runh2o0dbd9p0nil.apps.googleusercontent.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) //calling the overridden onCreate function
        setContentView(R.layout.search_activity) //set the relevant activity layout
        //initializes:
        setSupportActionBar(findViewById(R.id.search_toolbar))
        firestore = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.search_rec_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)


        tab_layout = findViewById(R.id.tabLayout)


        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab?.text == "users") {
                    search_users()
                }else{
                    search_trainers()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }


        })

        handleIntent(intent)
    }




    /**If the current activity is the searchable activity and if we set android:launchMode to "singleTop",
     * then the searchable activity receives the ACTION_SEARCH intent with a call to onNewIntent(Intent),
     * passing the new ACTION_SEARCH intent here.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)//calling the overridden onCreate function
        setIntent(intent) // this line make sure that the intent saved by the activity is updated in case you call getIntent() in the future
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent, collection_name : String = "regular_users") {
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { str ->
                SearchRecentSuggestions(this, MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE).saveRecentQuery(str, null)
                val query = firestore.collection(collection_name).orderBy("name", Query.Direction.ASCENDING).startAt(str).endAt(str + "\uf8ff")
                val options = FirestoreRecyclerOptions.Builder<SearchFireStoreModel>().setQuery(query, SearchFireStoreModel::class.java).build()
                adapter = SearchFireStoreAdapter(options)
                recyclerView.adapter = adapter
                adapter.startListening()
            }
        }

    }

    private fun search_users() {handleIntent(intent)}
    private fun search_trainers() {handleIntent(intent,"business_users")}


    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }


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


}

