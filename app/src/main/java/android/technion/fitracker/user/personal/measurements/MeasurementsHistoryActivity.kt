package android.technion.fitracker.user.personal.measurements

import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.measurements.MeasurementsFireStoreAdapter
import android.technion.fitracker.models.measurements.MeasurementsHistoryModel
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class MeasurementsHistoryActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    var adapter: MeasurementsFireStoreAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.measurements_history_activity)
        setSupportActionBar(findViewById(R.id.measurements_toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val recyclerView = findViewById<RecyclerView>(R.id.measurements_history_rec_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        val query = db.collection("regular_users").document(auth.currentUser!!.uid).collection("measurements")
                .orderBy("data", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<MeasurementsHistoryModel>()
                .setQuery(query, MeasurementsHistoryModel::class.java)
                .build()
        adapter = MeasurementsFireStoreAdapter(options)
        recyclerView.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

}