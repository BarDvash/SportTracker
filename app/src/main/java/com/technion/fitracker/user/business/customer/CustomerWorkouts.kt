package com.technion.fitracker.user.business.customer


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

import com.technion.fitracker.R
import com.technion.fitracker.adapters.WorkoutsFireStoreAdapter
import com.technion.fitracker.models.CustomerDataViewModel
import com.technion.fitracker.models.WorkoutFireStoreModel
import com.technion.fitracker.user.personal.workout.edit.CreateNewWorkoutActivity
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
class CustomerWorkouts : Fragment(), View.OnClickListener {
    private lateinit var mAuth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    private lateinit var fab: ExtendedFloatingActionButton
    lateinit var placeholder: TextView
    lateinit var viewModel: CustomerDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[CustomerDataViewModel::class.java]
        } ?: throw Exception("Invalid fragment, customer workout fragment!")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customer_workouts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab = view.findViewById<ExtendedFloatingActionButton>(R.id.customer_workouts_fab)
        fab.animation = AnimationUtils.loadAnimation(context!!, R.anim.scale_in_card)
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        placeholder = view.findViewById(R.id.customer_no_workout_placeholder)
        val query = firestore
                .collection("regular_users")
                .document(viewModel.customerID!!)
                .collection("workouts")
                .orderBy("name", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions.Builder<WorkoutFireStoreModel>()
                .setQuery(query, WorkoutFireStoreModel::class.java)
                .build()
        viewModel.workoutsAdapter = WorkoutsFireStoreAdapter(options, this,context!!,"Trainer").apply {
            mOnItemClickListener = View.OnClickListener { v ->
                val rvh = v.tag as WorkoutsFireStoreAdapter.ViewHolder
                val snapshot: DocumentSnapshot = viewModel.workoutsAdapter?.snapshots?.getSnapshot(rvh.adapterPosition)!!
                val workoutID = snapshot.id
                val workoutEdit = Intent(context!!, CreateNewWorkoutActivity::class.java)
                workoutEdit.putExtra("workoutID", workoutID)
                workoutEdit.putExtra("customerID", viewModel.customerID)
                startActivity(workoutEdit)
            }
        }
        viewModel.workoutsRecyclerView = view.findViewById<RecyclerView>(R.id.customer_workouts_rec_view).apply{
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        if (fab.isShown) {
                            fab.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_go_down))
                            fab.visibility = View.GONE
                        }
                    } else if (dy < 0) {
                        if (!fab.isShown) {
                            fab.visibility = View.VISIBLE
                            fab.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_go_up))
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
            R.id.customer_workouts_fab -> {
                val workoutCreate = Intent(context!!, CreateNewWorkoutActivity::class.java)
                workoutCreate.putExtra("customerID", viewModel.customerID)
                startActivity(workoutCreate)
            }
        }
    }
}
