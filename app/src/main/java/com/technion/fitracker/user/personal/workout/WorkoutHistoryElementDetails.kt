package com.technion.fitracker.user.personal.workout

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.fitracker.R
import com.technion.fitracker.adapters.WorkoutHistoryLogAdapter
import com.technion.fitracker.databinding.ActivityWorkoutHistoryElementDetailsBinding
import com.technion.fitracker.models.exercise.ExerciseLogModel
import com.technion.fitracker.models.workouts.WorkoutHistoryModel
import kotlinx.android.synthetic.main.activity_workout_history_element_details.*
import java.util.*
import kotlin.collections.HashMap

class WorkoutHistoryElementDetails : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth
    lateinit var mFirestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: WorkoutHistoryLogAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    lateinit var viewModel: WorkoutHistoryModel
    lateinit var ratingImage: ImageView
    lateinit var commentHolder: LinearLayout
    var uid: String? = null
    var isTrainer: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_history_element_details)
        viewModel = ViewModelProviders.of(this)[WorkoutHistoryModel::class.java]
        val binding =
            DataBindingUtil.setContentView<ActivityWorkoutHistoryElementDetailsBinding>(this, R.layout.activity_workout_history_element_details)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setSupportActionBar(findViewById(R.id.workout_history_toolbar))
        val params = intent.extras
        mAuth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        ratingImage = findViewById(R.id.workout_rating)
        commentHolder = findViewById(R.id.workout_comment_container)
        if(params?.get("userID") as String? != null){
            isTrainer = true
        }
        uid = params?.get("userID") as String? ?: mAuth.currentUser!!.uid
        viewModel.timeElapsed.value = params?.get("time_elapsed") as String?
        viewModel.workoutID = params?.get("id") as String?
        viewModel.workoutName.value = params?.get("workout_name") as String?
        viewModel.workoutComment.value = params?.get("comment") as String?
        viewModel.workoutDate.value = params?.get("date_time") as String?
        viewModel.workoutRating.value = params?.get("rating") as Long?
        if(viewModel.workoutComment.value == null || viewModel.workoutComment.value?.length == 0){
            commentHolder.visibility = View.GONE
        }
        val exercisesHashMap = (params?.get("exercises") as ArrayList<HashMap<String,String?>>?)
        if (exercisesHashMap != null) {
            for (exercise in exercisesHashMap){
                viewModel.workoutExercises.value?.add(ExerciseLogModel(exercise["name"],exercise["time_done"]))
            }
        }
        when (viewModel.workoutRating.value?.toInt()) {
            WorkoutSummaryScreen.ExerciseRatings.SAD.ordinal -> {
                ratingImage.setImageResource(R.drawable.ic_sad)
            }
            WorkoutSummaryScreen.ExerciseRatings.NEUTRAL.ordinal -> {
                ratingImage.setImageResource(R.drawable.ic_neutral)
            }
            WorkoutSummaryScreen.ExerciseRatings.HAPPY.ordinal -> {
                ratingImage.setImageResource(R.drawable.ic_happiness)
            }
            WorkoutSummaryScreen.ExerciseRatings.COOL.ordinal -> {
                ratingImage.setImageResource(R.drawable.ic_cool)
            }
            else -> {
                ratingImage.setImageResource(R.drawable.ic_happiness)
            }
        }

        supportActionBar?.title = viewModel.workoutName.value
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewAdapter = WorkoutHistoryLogAdapter(viewModel.workoutExercises.value!!)
        viewManager = LinearLayoutManager(this)
        recyclerView = findViewById<RecyclerView>(R.id.workout_history_recycler)?.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }!!
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.workout_history_menu, menu)
        if(isTrainer == false){
            menu?.add(0, 1, Menu.NONE, "Delete")?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menu?.add(0, 2, Menu.NONE, "Share")?.apply {
                setIcon(R.drawable.ic_share)
                setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }
        }
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val delete_action = 1
        val share_action = 2
        when (item.itemId) {
            share_action -> {
                //TODO:
            }
            delete_action -> {
                MaterialAlertDialogBuilder(this).setTitle("Warning").setMessage("Delete workout activity?")
                        .setPositiveButton(
                            "Yes"
                        ) { _, _ ->
                            mFirestore.collection("regular_users").document(uid!!).collection("workouts_history").document(viewModel.workoutID!!).delete()
                                    .addOnSuccessListener {
                                        Log.d(FragmentActivity.VIBRATOR_SERVICE, "DocumentSnapshot deleted with ID: " + viewModel.workoutID)
                                    }
                                    .addOnFailureListener { e -> Log.w(FragmentActivity.VIBRATOR_SERVICE, "Error deleting document", e) }
                            finish()
                        }
                        .setNegativeButton(
                            "No"
                        ) { _, _ ->
                        }.show()

            }
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }
}

//val bundle = bundleOf("dishes" to viewModel.data[pos], "pos" to pos)
//summary["workout_name"] = viewModel.workoutName.value
//summary["time_elapsed"] = viewModel.timeElapsed.value
//val exercisesLog: ArrayList<ExerciseLogModel> = arrayListOf()
//viewModel.workoutExercises.value?.let {
//    for (exercise in it) {
//        exercisesLog.add(exercise.extractLogModel())
//    }
//}
//summary["exercises"] = exercisesLog
//summary["date_time"] = SimpleDateFormat("yyyy-MM-dd 'at' HH:mm").format(Calendar.getInstance().time)
//summary["comment"] = viewModel.comment.value
//summary["rating"] = viewModel.workoutRate