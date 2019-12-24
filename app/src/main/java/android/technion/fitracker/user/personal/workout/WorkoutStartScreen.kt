package android.technion.fitracker.user.personal.workout


import android.content.Intent
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.ExerciseAdapter
import android.technion.fitracker.databinding.FragmentWorkoutStartScreenBinding
import android.technion.fitracker.models.exercise.AerobicExerciseModel
import android.technion.fitracker.models.exercise.ExerciseBaseModel
import android.technion.fitracker.models.exercise.WeightExerciseModel
import android.technion.fitracker.models.workouts.WorkoutStartViewModel
import android.technion.fitracker.user.personal.workout.edit.CreateNewWorkoutActivity
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_workout_in_progress.*


class WorkoutStartScreen : Fragment(), View.OnClickListener {
    private lateinit var viewModel: WorkoutStartViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var viewAdapter: ExerciseAdapter
    private lateinit var navController: NavController

    lateinit var mAuth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[WorkoutStartViewModel::class.java]
        } ?: throw Exception("Invalid Activity WorkoutStartScreen aerobic fragment")
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
        val appCompatActivity = (activity as AppCompatActivity)
        appCompatActivity.setSupportActionBar(view.findViewById(R.id.start_workout_toolbar))
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        viewManager = LinearLayoutManager(activity)
        val exercises = viewModel.workoutExercises.value
        viewAdapter = ExerciseAdapter(exercises!!)
        recyclerView = activity?.findViewById<RecyclerView>(R.id.workouts_rec_view).apply {
            this?.setHasFixedSize(true)
            this?.layoutManager = viewManager
            this?.adapter = viewAdapter

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
        when(item.itemId){
            R.id.start_workout_edit_item ->{
                val intent = Intent(activity,CreateNewWorkoutActivity::class.java)
                intent.putExtra("workoutID", viewModel.workoutID.value)
                startActivityForResult(intent,0)
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

    private fun deleteWorkoutFromDB() {
        firestore.collection("regular_users").document(mAuth.currentUser?.uid!!).collection("workouts").document(viewModel.workoutID.value!!).delete().addOnSuccessListener {
            Log.d(FragmentActivity.VIBRATOR_SERVICE, "DocumentSnapshot deleted with ID: " + viewModel.workoutID.value)
        }
                .addOnFailureListener { e -> Log.w(FragmentActivity.VIBRATOR_SERVICE, "Error deleting document", e) }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.start_workout_fab -> {
                //TODO
            }
            //Back button
            else -> {
                //TODO
            }
        }
    }


//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        super.onCreateOptionsMenu(menu)
//        when (navController.currentDestination?.label) {
//            "fragment_workout_start_screen" -> {
//                supportActionBar?.title = viewModel.workoutName.value
//                menuInflater.inflate(R.menu.start_workout_menu, menu)
//            }
//            "fragment_workout_in_progress" -> {
//                supportActionBar?.title = viewModel.workoutName.value
//            }
//            "fragment_workout_summary_screen" -> {
//                supportActionBar?.title = "Summary"
//            }
//        }
//        return true
//    }
}
