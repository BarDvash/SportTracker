package com.technion.fitracker

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.technion.fitracker.adapters.SearchFireStoreAdapter
import com.technion.fitracker.models.SearchFireStoreModel

class TrainerSearchResultsFragment : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FirestoreRecyclerAdapter<SearchFireStoreModel, SearchFireStoreAdapter.ViewHolder>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trainer_search_results, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.trainers_search_rec_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)


        handleIntent(activity!!.intent)
    }






    fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { current_search_query ->
                SearchRecentSuggestions(activity, MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE).saveRecentQuery(current_search_query, null)
                val query = FirebaseFirestore.getInstance().collection("business_users").orderBy("name", Query.Direction.ASCENDING).startAt(current_search_query).endAt(current_search_query + "\uf8ff")
                val options = FirestoreRecyclerOptions.Builder<SearchFireStoreModel>().setQuery(query, SearchFireStoreModel::class.java).build()
                adapter = SearchFireStoreAdapter(options, context!!)
                recyclerView.adapter = adapter
                adapter.startListening()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }


}
