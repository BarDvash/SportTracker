package com.technion.fitracker.user.personal.workout


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.fitracker.R
import com.technion.fitracker.adapters.ExerciseAdapter
import com.technion.fitracker.databinding.FragmentWorkoutStartScreenBinding
import com.technion.fitracker.models.exercise.AerobicExerciseModel
import com.technion.fitracker.models.exercise.ExerciseBaseModel
import com.technion.fitracker.models.exercise.WeightExerciseModel
import com.technion.fitracker.models.workouts.WorkoutStartViewModel
import com.technion.fitracker.user.personal.workout.edit.CreateNewWorkoutActivity


class WorkoutStartScreen : Fragment(), View.OnClickListener {
    private lateinit var viewModel: WorkoutStartViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var navController: NavController
    private lateinit var viewAdapter: ExerciseAdapter


    lateinit var mAuth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[WorkoutStartViewModel::class.java]
        } ?: throw Exception("Invalid  WorkoutStartScreen fragment")
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            DataBindingUtil.inflate<FragmentWorkoutStartScreenBinding>(inflater, R.layout.fragment_workout_start_screen, container, false)
        view.viewModel = viewModel
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        val appCompatActivity = (activity as AppCompatActivity)
        appCompatActivity.setSupportActionBar(view.findViewById(R.id.start_workout_toolbar))
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        viewManager = LinearLayoutManager(activity)
        val exercises = viewModel.workoutExercises.value
        viewAdapter = ExerciseAdapter(exercises!!, context!!)
        recyclerView = activity?.findViewById<RecyclerView>(R.id.workouts_rec_view)?.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
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
        }!!
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        extractWorkoutFromDB(appCompatActivity)


        fab = activity?.findViewById(R.id.start_workout_fab)!!
        fab.setOnClickListener(this)
    }

    fun extractWorkoutFromDB(appCompatActivity: AppCompatActivity) {
        val uid = mAuth.currentUser?.uid
        if ((viewModel.workoutExercises.value?.size ?: 0) == 0) {
            if (!uid.isNullOrEmpty()) {
                if (viewModel.workoutID.value != null) {
                    firestore.collection("regular_users").document(uid).collection("workouts")
                            .document(viewModel.workoutID.value!!).get()
                            .addOnSuccessListener { document ->
                                val data = document.data
                                viewModel.workoutName.value = data?.get("name")?.toString()
                                for (exercise in data?.get("exercises") as ArrayList<HashMap<String, String?>>) {
                                    when (exercise["type"]) {
                                        "Aerobic" -> {
                                            viewModel.workoutExercises.value?.add(
                                                AerobicExerciseModel(
                                                    exercise["name"],
                                                    exercise["duration"],
                                                    exercise["speed"],
                                                    exercise["intensity"],
                                                    exercise["notes"]
                                                ) as ExerciseBaseModel
                                            )
                                        }
                                        "Weight" -> {
                                            viewModel.workoutExercises.value?.add(
                                                WeightExerciseModel(
                                                    exercise["name"],
                                                    exercise["weight"],
                                                    exercise["sets"],
                                                    exercise["repetitions"],
                                                    exercise["rest"],
                                                    exercise["notes"]
                                                ) as ExerciseBaseModel
                                            )
                                        }
                                    }
                                }
                                appCompatActivity.supportActionBar?.title = viewModel.workoutName.value
                                viewAdapter.notifyDataSetChanged()
                            }
                            .addOnFailureListener { exception ->
                                Log.w(null, "Error getting documents: ", exception)
                            }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.start_workout_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.start_workout_edit_item -> {
                val intent = Intent(activity, CreateNewWorkoutActivity::class.java)
                intent.putExtra("workoutID", viewModel.workoutID.value)
                startActivityForResult(intent, 0)
            }
            R.id.start_workout_delete_item -> {
                MaterialAlertDialogBuilder(activity).setTitle("Warning").setMessage("Data will be lost, continue?")
                        .setPositiveButton(
                            "Yes"
                        ) { _, _ ->
                            deleteWorkoutFromDB()
                            activity?.finish()

                        }
                        .setNegativeButton(
                            "No"
                        ) { _, _ ->
                        }.show()
            }
            android.R.id.home -> {
                activity?.finish()
            }
        }
        return true
    }

    private fun startFragmentAndPop(id: Int) {
        navController.popBackStack()
        navController.navigate(id)
    }

    private fun deleteWorkoutFromDB() {
        firestore.collection("regular_users").document(mAuth.currentUser?.uid!!).collection("workouts")
                .document(viewModel.workoutID.value!!).delete().addOnSuccessListener {
                    Log.d(FragmentActivity.VIBRATOR_SERVICE, "DocumentSnapshot deleted with ID: " + viewModel.workoutID.value)
                }
                .addOnFailureListener { e -> Log.w(FragmentActivity.VIBRATOR_SERVICE, "Error deleting document", e) }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.start_workout_fab -> {
                startFragmentAndPop(R.id.workoutInProgress)
            }
            //Back button
            else -> {
                //TODO
            }
        }
    }


}
