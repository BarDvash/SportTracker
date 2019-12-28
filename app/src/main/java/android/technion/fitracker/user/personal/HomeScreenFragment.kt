package android.technion.fitracker.user.personal


import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.RecentWorkoutsFireStoreAdapter
import android.technion.fitracker.models.workouts.RecentWorkoutFireStoreModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class HomeScreenFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    lateinit var adapter: FirestoreRecyclerAdapter<RecentWorkoutFireStoreModel, RecentWorkoutsFireStoreAdapter.ViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        recyclerView = view.findViewById(R.id.last_workouts_recycler)
        recyclerView.addItemDecoration( DividerItemDecoration(context, HORIZONTAL))
        recyclerView.layoutManager = LinearLayoutManager(context)
        val uid = firebaseAuth.currentUser?.uid

        val query = firebaseFirestore
                .collection("regular_users")
                .document(uid!!)
                .collection("workouts_history")
                .orderBy("date_time", Query.Direction.DESCENDING)
                .limit(5)
        val options = FirestoreRecyclerOptions.Builder<RecentWorkoutFireStoreModel>()
                .setQuery(query, RecentWorkoutFireStoreModel::class.java)
                .build()
        adapter = RecentWorkoutsFireStoreAdapter(options)
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
