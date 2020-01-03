package com.technion.fitracker.user.personal.workout


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.ViewCompat.animate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.technion.fitracker.R
import com.technion.fitracker.adapters.WorkoutsFireStoreAdapter
import com.technion.fitracker.databinding.FragmentUserWorkoutsBinding
import com.technion.fitracker.databinding.FragmentWorkoutInProgressBinding
import com.technion.fitracker.models.UserViewModel
import com.technion.fitracker.models.WorkoutFireStoreModel
import com.technion.fitracker.user.personal.workout.edit.CreateNewWorkoutActivity


class WorkoutsFragment : Fragment(), View.OnClickListener {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var viewModel: UserViewModel
    lateinit var firestore: FirebaseFirestore
    private lateinit var fab: ExtendedFloatingActionButton
    lateinit var placeholder: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[UserViewModel::class.java]
        } ?: throw Exception("Invalid Fragment, workouts fragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            DataBindingUtil.inflate<FragmentUserWorkoutsBinding>(inflater, R.layout.fragment_user_workouts, container, false)
        view.viewmodel = viewModel
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab = view.findViewById<ExtendedFloatingActionButton>(R.id.workouts_fab)
        fab.animation = AnimationUtils.loadAnimation(context!!, R.anim.fab_transition)
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val uid = mAuth.currentUser?.uid
        placeholder = view.findViewById(R.id.no_workout_placeholder)
        val query = firestore
                .collection("regular_users")
                .document(uid!!)
                .collection("workouts")
                .orderBy("name", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions.Builder<WorkoutFireStoreModel>()
                .setQuery(query, WorkoutFireStoreModel::class.java)
                .build()
        viewModel.workoutsAdapter = WorkoutsFireStoreAdapter(options, this,context!!).apply {
            mOnItemClickListener = View.OnClickListener { v ->
                val rvh = v.tag as WorkoutsFireStoreAdapter.ViewHolder
                val snapshot: DocumentSnapshot = viewModel.workoutsAdapter?.snapshots?.getSnapshot(rvh.adapterPosition)!!
                val workoutID = snapshot.id
                val workoutStart = Intent(context!!, WorkoutStarter::class.java)
                workoutStart.putExtra("workoutID", workoutID)
                startActivity(workoutStart)
            }
        }
        viewModel.workoutsRecyclerView = view.findViewById<RecyclerView>(R.id.workouts_rec_view).apply{
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        if (fab.isShown) {
                            fab.hide()
                        }
                    } else if (dy < 0) {
                        if (!fab.isShown) {
                            fab.show()
                        }
                    }
                }
            })
            adapter = viewModel.workoutsAdapter
        }
        fab.setOnClickListener(this)
    }


    override fun onStart() {
        super.onStart()
        viewModel.workoutsAdapter?.startListening()
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
