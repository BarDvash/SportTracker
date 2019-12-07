package android.technion.fitracker.user.personal


import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.WorkoutsFireStoreAdapter
import android.technion.fitracker.models.WorkoutFireStoreModel
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


/**
 * A simple [Fragment] subclass.
 */
class WorkoutsFragment : Fragment() {
    lateinit var mAuth: FirebaseAuth

    lateinit var firestore: FirebaseFirestore
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FirestoreRecyclerAdapter<WorkoutFireStoreModel, WorkoutsFireStoreAdapter.ViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_workouts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        recyclerView = view.findViewById(R.id.workouts_rec_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val uid = mAuth.currentUser?.uid
        val query = firestore.collection("users").document(uid!!).collection("workouts").orderBy("name", Query.Direction.ASCENDING)
        val query_result = firestore.collection("users").document(uid).collection("workouts").document("PLKO4Z2m33A7ezeWZB4h").get().addOnSuccessListener {
                document ->

            Log.e("gavno!", document.toObject(WorkoutFireStoreModel::class.java).toString())
        }
        val options = FirestoreRecyclerOptions.Builder<WorkoutFireStoreModel>().setQuery(query, WorkoutFireStoreModel::class.java).build()
        adapter = WorkoutsFireStoreAdapter(options)
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

}
