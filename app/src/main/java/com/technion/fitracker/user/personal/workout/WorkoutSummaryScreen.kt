package com.technion.fitracker.user.personal.workout


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.fitracker.R
import com.technion.fitracker.adapters.ExerciseSummaryAdapter
import com.technion.fitracker.databinding.FragmentWorkoutSummaryScreenBinding
import com.technion.fitracker.models.exercise.ExerciseLogModel
import com.technion.fitracker.models.workouts.WorkoutStartViewModel
import java.text.SimpleDateFormat
import java.util.*


class WorkoutSummaryScreen : Fragment(), View.OnClickListener {
    private lateinit var viewModel: WorkoutStartViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ExerciseSummaryAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var saveButton: MaterialButton
    private lateinit var discardButton: MaterialButton
    private lateinit var navController: NavController
    private lateinit var commentField: TextInputEditText
    private lateinit var timeElapsed: TextView
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sadRating: ImageView
    private lateinit var neutralRating: ImageView
    private lateinit var happyRating: ImageView
    private lateinit var coolRating: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[WorkoutStartViewModel::class.java]
        } ?: throw Exception("Invalid WorkoutSummaryScreen fragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = DataBindingUtil.inflate<FragmentWorkoutSummaryScreenBinding>(inflater, R.layout.fragment_workout_summary_screen, container, false)
        view.viewModel = viewModel
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        val appCompatActivity = (activity as AppCompatActivity)
        appCompatActivity.setSupportActionBar(view.findViewById(R.id.workout_summary_toolbar))
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewAdapter = ExerciseSummaryAdapter(viewModel.workoutExercises.value!!)
        viewManager = LinearLayoutManager(context)
        recyclerView = activity?.findViewById<RecyclerView>(R.id.workout_summary_recycler)?.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }!!
        activity?.let {
            saveButton = it.findViewById(R.id.workout_summary_save)
            discardButton = it.findViewById(R.id.workout_summary_discard)
            commentField = it.findViewById(R.id.workout_summary_comment)
            timeElapsed = it.findViewById(R.id.time_elapsed)
            sadRating = it.findViewById(R.id.exercise_rating_sad)
            neutralRating = it.findViewById(R.id.exercise_rating_neutral)
            happyRating = it.findViewById(R.id.exercise_rating_happy)
            coolRating = it.findViewById(R.id.exercise_rating_cool)
        }
        sadRating.setOnClickListener(this)
        neutralRating.setOnClickListener(this)
        happyRating.setOnClickListener(this)
        coolRating.setOnClickListener(this)
        saveButton.setOnClickListener(this)
        discardButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.workout_summary_save -> {
                firebaseAuth.currentUser?.uid?.let {
                    firebaseFirestore.collection("regular_users")
                            .document(it).collection("workouts_history").add(getSummary())
                            .addOnSuccessListener { documentReference ->
                                Log.d(FragmentActivity.VIBRATOR_SERVICE, "DocumentSnapshot added with ID: " + documentReference.id)
                                activity?.finish()
                            }
                            .addOnFailureListener { e -> Log.w(FragmentActivity.VIBRATOR_SERVICE, "Error adding document to workout_history collection", e) }
                }
            }
            R.id.workout_summary_discard -> {
                MaterialAlertDialogBuilder(activity).setTitle("Warning").setMessage("Discard workout result?")
                        .setPositiveButton(
                            "Yes"
                        ) { _, _ ->
                            activity?.finish()

                        }
                        .setNegativeButton(
                            "No"
                        ) { _, _ ->
                        }.show()
            }
            R.id.exercise_rating_sad -> {
                setBaseColorLightGray()
                setSelectedImage(ExerciseRatings.SAD.ordinal)
            }
            R.id.exercise_rating_neutral -> {
                setBaseColorLightGray()
                setSelectedImage(ExerciseRatings.NEUTRAL.ordinal)
            }
            R.id.exercise_rating_happy -> {
                setBaseColorLightGray()
                setSelectedImage(ExerciseRatings.HAPPY.ordinal)
            }
            R.id.exercise_rating_cool -> {
                setBaseColorLightGray()
                setSelectedImage(ExerciseRatings.COOL.ordinal)
            }
        }
    }

    enum class ExerciseRatings {
        SAD, NEUTRAL, HAPPY, COOL
    }

    private fun setBaseColorLightGray() {
        sadRating.setColorFilter(Color.LTGRAY)
        neutralRating.setColorFilter(Color.LTGRAY)
        happyRating.setColorFilter(Color.LTGRAY)
        coolRating.setColorFilter(Color.LTGRAY)
    }

    private fun setBaseColor() {
        sadRating.setColorFilter(Color.BLACK)
        neutralRating.setColorFilter(Color.BLACK)
        happyRating.setColorFilter(Color.BLACK)
        coolRating.setColorFilter(Color.BLACK)
    }

    private fun getSummary(): MutableMap<String, Any?> {
        val summary: MutableMap<String, Any?> = mutableMapOf()
        summary["workout_name"] = viewModel.workoutName.value
        summary["time_elapsed"] = viewModel.timeElapsed.value
        val exercisesLog: ArrayList<ExerciseLogModel> = arrayListOf()
        viewModel.workoutExercises.value?.let {
            for (exercise in it) {
                exercisesLog.add(exercise.extractLogModel())
            }
        }
        summary["exercises"] = exercisesLog
        summary["date_time"] = SimpleDateFormat("yyyy-MM-dd 'at' HH:mm").format(Calendar.getInstance().time)
        summary["comment"] = viewModel.comment.value
        summary["rating"] = viewModel.workoutRate
        return summary
    }

    private fun setSelectedImage(index: Int) {
        var color = Color.RED
        if (viewModel.workoutRate == index) {
            setBaseColor()
            viewModel.workoutRate = null
            return
        } else {
            viewModel.workoutRate = index
        }
        when (index) {
            ExerciseRatings.SAD.ordinal -> {
                sadRating.setColorFilter(color)
            }
            ExerciseRatings.NEUTRAL.ordinal -> {
                neutralRating.setColorFilter(color)

            }
            ExerciseRatings.HAPPY.ordinal -> {
                happyRating.setColorFilter(color)

            }
            ExerciseRatings.COOL.ordinal -> {
                coolRating.setColorFilter(color)

            }
        }
    }
}
