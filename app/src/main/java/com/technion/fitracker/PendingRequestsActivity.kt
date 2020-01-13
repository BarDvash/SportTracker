package com.technion.fitracker

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.technion.fitracker.adapters.PendingRequestFireStoreAdapter
import com.technion.fitracker.models.PendingRequestFireStoreModel

class PendingRequestsActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore

    var user_type: String? = null
    var user_name: String? = null
    var user_photo_url: String? = null

    private lateinit var recyclerView: RecyclerView
    lateinit var adapter: FirestoreRecyclerAdapter<PendingRequestFireStoreModel, PendingRequestFireStoreAdapter.ViewHolder>
    lateinit var placeholder: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_requests)


        setSupportActionBar(findViewById(R.id.pending_requests_toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Pending Requests"
        }

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.pending_requests_rec_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)


        val uid = mAuth.currentUser?.uid
        placeholder = findViewById(R.id.no_pending_request_placeholder)


        var bundle: Bundle? = intent.extras
        user_type = bundle!!.getString("user_type")
        user_name = bundle.getString("user_name")
        user_photo_url = bundle.getString("user_photo_url")

        val query = firestore.collection(user_type + "_users").document(uid!!).collection("requests").orderBy("user_name", Query.Direction.ASCENDING)


        val options =
            FirestoreRecyclerOptions.Builder<PendingRequestFireStoreModel>().setQuery(query, PendingRequestFireStoreModel::class.java).build()
        adapter = PendingRequestFireStoreAdapter(options, this)
        recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }


}
