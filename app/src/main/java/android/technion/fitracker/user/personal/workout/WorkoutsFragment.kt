package android.technion.fitracker.user.personal.workout


import android.content.Intent
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.WorkoutsFireStoreAdapter
import android.technion.fitracker.models.WorkoutFireStoreModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


/**
 * A simple [Fragment] subclass.
 */
class WorkoutsFragment : Fragment(), View.OnClickListener {
    lateinit var mAuth: FirebaseAuth

    lateinit var firestore: FirebaseFirestore
    lateinit var recyclerView: RecyclerView
    lateinit var fab: ExtendedFloatingActionButton
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
        fab = view.findViewById(R.id.workouts_fab)
        recyclerView = view.findViewById(R.id.workouts_rec_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val uid = mAuth.currentUser?.uid
        val onItemClickListener = View.OnClickListener { v ->
            val rvh = v.tag as WorkoutsFireStoreAdapter.ViewHolder
            Toast.makeText(activity, "You clicked on " + (rvh.name.text ?: "None"), Toast.LENGTH_SHORT).show()
//            val s = exercises?.get(rvh.adapterPosition)
        }
        val query = firestore
                .collection("regular_users")
                .document(uid!!)
                .collection("workouts")
                .orderBy("name", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions.Builder<WorkoutFireStoreModel>()
                .setQuery(query, WorkoutFireStoreModel::class.java)
                .build()
        adapter = WorkoutsFireStoreAdapter(options).apply {
            mOnItemClickListener = onItemClickListener
        }
        recyclerView.adapter = adapter
        fab.setOnClickListener(this)
    }


    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.workouts_fab -> {
                val createNewWorkoutActivity = Intent(context!!, CreateNewWorkoutActivity::class.java)
                startActivity(createNewWorkoutActivity)
            }
        }
    }

}
