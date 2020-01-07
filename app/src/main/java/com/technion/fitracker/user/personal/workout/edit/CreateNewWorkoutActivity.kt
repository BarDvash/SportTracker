package com.technion.fitracker.user.personal.workout.edit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.SHOW_AS_ACTION_NEVER
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.fitracker.R
import com.technion.fitracker.adapters.ExerciseAdapter
import com.technion.fitracker.databinding.ActivityCreateNewWorkoutBinding
import com.technion.fitracker.models.exercise.AerobicExerciseModel
import com.technion.fitracker.models.exercise.ExerciseBaseModel
import com.technion.fitracker.models.exercise.WeightExerciseModel
import com.technion.fitracker.models.workouts.CreateWorkoutViewModel
import com.technion.fitracker.user.personal.workout.WorkoutStarter
import com.technion.fitracker.user.personal.workout.edit.CreateNewWorkoutActivity.ResultCodes.*


class CreateNewWorkoutActivity : AppCompatActivity(), View.OnClickListener {
    enum class ResultCodes {
        AEROBIC, WEIGHT, AEROBIC_EDIT, WEIGHT_EDIT, DELETE, RETURN
    }

    private lateinit var viewModel: CreateWorkoutViewModel
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var noWorkoutHint: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var viewAdapter: ExerciseAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var traineeUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[CreateWorkoutViewModel::class.java]
        val binding =
            DataBindingUtil.setContentView<ActivityCreateNewWorkoutBinding>(this, R.layout.activity_create_new_workout)
        binding.lifecycleOwner = this
        binding.newWorkoutViewModel = viewModel
        traineeUid = intent.getStringExtra("customerID")
        viewModel.workoutID.value = intent.getStringExtra("workoutID")
        setSupportActionBar(findViewById(R.id.create_workout_toolbar))
        if (viewModel.workoutID.value != null) {
            supportActionBar?.title = "Create Workout"
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        firestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        viewManager = LinearLayoutManager(this)
        noWorkoutHint = findViewById(R.id.no_workouts_hint)
        val exercises = viewModel.workout_exercises.value
        val onItemClickListener = View.OnClickListener { v ->
            val viewHolder = v.tag as RecyclerView.ViewHolder
            var model = exercises?.get(viewHolder.adapterPosition)
            when (model?.type) {
                "Aerobic" -> {
                    model = model as AerobicExerciseModel
                    val intent = Intent(this, EditAerobicExercise::class.java)
                    intent.putExtra("name", model.name)
                    intent.putExtra("duration", model.duration)
                    intent.putExtra("speed", model.speed)
                    intent.putExtra("intensity", model.intensity)
                    intent.putExtra("notes", model.notes)
                    intent.putExtra("index", viewHolder.adapterPosition)
                    startActivityForResult(intent, AEROBIC_EDIT.ordinal)
                }
                "Weight" -> {
                    model = model as WeightExerciseModel
                    val intent = Intent(this, EditWeightExercise::class.java)
                    intent.putExtra("name", model.name)
                    intent.putExtra("weight", model.weight)
                    intent.putExtra("sets", model.sets)
                    intent.putExtra("repetitions", model.repetitions)
                    intent.putExtra("rest", model.rest)
                    intent.putExtra("notes", model.notes)
                    intent.putExtra("index", viewHolder.adapterPosition)
                    startActivityForResult(intent, WEIGHT_EDIT.ordinal)
                }
            }
        }
        viewAdapter = ExerciseAdapter(exercises!!, this).apply {
            mOnItemClickListener = onItemClickListener
        }
        if (!viewModel.workoutID.value.isNullOrEmpty()) {
            extractWorkoutFromDB()

        } else {
            supportActionBar?.title = "Create Workout"
        }
        recyclerView = findViewById<RecyclerView>(R.id.create_workout_recycler).apply {
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

        }
        fab = findViewById(R.id.add_exercise_fab)
        fab.setOnClickListener(this)


    }

    override fun onResume() {
        super.onResume()
        setEmptyPlaceholderState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val deleteAction = 1
        when (item.itemId) {
            R.id.new_workout_save_item -> {
                saveWorkoutToFirestore()

                //for cloud functions:
                if(traineeUid != null){//if the user who updated the workout is business
                    //create this document to make the cloud function to operate and notify trainee
                    firestore.collection("regular_users").document(traineeUid!!).collection("updates").document("workout_update").set(hashMapOf("workout_update" to "yes"))
                }
                //until here

            }
            deleteAction -> {
                MaterialAlertDialogBuilder(this).setTitle("Warning").setMessage("Data will be lost, continue?")
                        .setPositiveButton(
                            "Yes"
                        ) { _, _ ->
                            deleteWorkoutFromDB()
                            finish()

                        }
                        .setNegativeButton(
                            "No"
                        ) { _, _ ->
                        }.show()
            }
            else -> {
                safeExit()
            }
        }
        return true
    }

    private fun deleteWorkoutFromDB() {
        firestore.collection("regular_users").document(traineeUid!!).collection("workouts")
                .document(viewModel.workoutID.value!!).delete().addOnSuccessListener {
                    Log.d(FragmentActivity.VIBRATOR_SERVICE, "DocumentSnapshot deleted with ID: " + viewModel.workoutID.value)
                }
                .addOnFailureListener { e -> Log.w(FragmentActivity.VIBRATOR_SERVICE, "Error deleting document", e) }
    }

    private fun setEmptyPlaceholderState() {
        if (viewModel.workout_exercises.value?.isNotEmpty()!!) {
            noWorkoutHint.visibility = View.GONE
        } else {
            noWorkoutHint.visibility = View.VISIBLE
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            AEROBIC.ordinal -> {
                val aerobicExercise: ExerciseBaseModel = getExerciseBaseModelFromAerobicData(data)
                viewModel.workout_exercises.value?.add(aerobicExercise)
            }
            WEIGHT.ordinal -> {
                val weightExercise: ExerciseBaseModel = getExerciseBaseModelFromWeightData(data)
                viewModel.workout_exercises.value?.add(weightExercise)
            }
            AEROBIC_EDIT.ordinal -> {
                val aerobicModel: ExerciseBaseModel = getExerciseBaseModelFromAerobicData(data)
                val index = data?.getIntExtra("index", -1)
                if (index != null && index != -1) {
                    viewModel.workout_exercises.value?.set(
                        index,
                        aerobicModel
                    )
                }

            }
            WEIGHT_EDIT.ordinal -> {
                val index = data?.getIntExtra("index", -1)
                if (index != null && index != -1) {
                    val weightModel: ExerciseBaseModel = getExerciseBaseModelFromWeightData(data)
                    viewModel.workout_exercises.value?.set(
                        index, weightModel

                    )
                }
            }
            DELETE.ordinal -> {
                val index = data?.getIntExtra("index", -1)
                if (index != null && index != -1) {
                    viewModel.workout_exercises.value?.removeAt(index)
                }
            }
            RETURN.ordinal -> {
                return
            }
        }
        setEmptyPlaceholderState()
        viewAdapter.notifyDataSetChanged()

    }

    private fun getExerciseBaseModelFromWeightData(data: Intent?): ExerciseBaseModel {
        return WeightExerciseModel(
            data?.getStringExtra("name"),
            data?.getStringExtra("weight"),
            data?.getStringExtra("sets"),
            data?.getStringExtra("repetitions"),
            data?.getStringExtra("rest"),
            data?.getStringExtra("notes")
        )
    }

    private fun getExerciseBaseModelFromAerobicData(data: Intent?): ExerciseBaseModel {
        return AerobicExerciseModel(
            data?.getStringExtra("name"),
            data?.getStringExtra("duration"),
            data?.getStringExtra("speed"),
            data?.getStringExtra("intensity"),
            data?.getStringExtra("notes")
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.add_new_workout_menu, menu)

        if(traineeUid != null){
            menu?.add(0, 1, Menu.NONE, "Delete")?.setShowAsAction(SHOW_AS_ACTION_NEVER)
        }

        return true

    }

    override fun onSupportNavigateUp(): Boolean {
        safeExit()
        return true
    }

    override fun onBackPressed() {
        safeExit()
    }

    private fun safeExit() {
        if (inEditMode()) {
            MaterialAlertDialogBuilder(this).setTitle("Warning").setMessage("Unsaved data will be lost, continue?")
                    .setPositiveButton(
                        "Yes"
                    ) { _, _ ->
                        this.finish()

                    }
                    .setNegativeButton(
                        "No"
                    ) { _, _ ->
                    }.show()
        } else {
            if (dataExist()) {
                MaterialAlertDialogBuilder(this).setTitle("Warning").setMessage("Data will be lost, continue?")
                        .setPositiveButton(
                            "Yes"
                        ) { _, _ ->
                            this.finish()

                        }
                        .setNegativeButton(
                            "No"
                        ) { _, _ ->
                        }.show()
            } else {
                this.finish()
            }
        }
    }

    private fun dataExist(): Boolean {
        return viewModel.workout_exercises.value?.size!! > 0 ||
                !viewModel.workout_name.value.isNullOrEmpty() ||
                !viewModel.workout_name.value.isNullOrEmpty()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.add_exercise_fab -> {
                val createNewWorkoutActivity = Intent(v.context!!, AddExerciseActivity::class.java)
                startActivityForResult(createNewWorkoutActivity, WEIGHT.ordinal)
            }
            R.id.new_workout_save_item -> {
                saveWorkoutToFirestore()
            }
        }
    }

    private fun saveWorkoutToFirestore() {
        if (viewModel.workout_name.value.isNullOrEmpty()) {
            Toast.makeText(this, "Name is a must field!", Toast.LENGTH_LONG).show()
            return
        }
        if (viewModel.workout_exercises.value?.size!! < 1) {
            Toast.makeText(this, "Workout must have at least one exercise!", Toast.LENGTH_LONG).show()
            return
        }
        val uid = traineeUid ?: mAuth.currentUser?.uid
        val workout =
            WorkoutData(viewModel.workout_name.value, viewModel.workout_desc.value, viewModel.workout_exercises.value)
        if (uid != null) {
            if (inEditMode()) {
                firestore.collection("regular_users")
                        .document(uid).collection("workouts").document(viewModel.workoutID.value!!).set(workout)
                        .addOnSuccessListener { documentReference ->
                            Log.d(FragmentActivity.VIBRATOR_SERVICE, "DocumentSnapshot updated with ID: " + viewModel.workoutID.value)
                            this.setResult(WorkoutStarter.ResultCodes.EDIT.ordinal)
                            this.finish()
                        }
                        .addOnFailureListener { e -> Log.w(FragmentActivity.VIBRATOR_SERVICE, "Error updating document document", e) }
            } else {
                firestore.collection("regular_users")
                        .document(uid).collection("workouts").add(workout)
                        .addOnSuccessListener { documentReference ->
                            Log.d(FragmentActivity.VIBRATOR_SERVICE, "DocumentSnapshot added with ID: " + documentReference.id)
                            this.finish()
                        }
                        .addOnFailureListener { e -> Log.w(FragmentActivity.VIBRATOR_SERVICE, "Error adding document", e) }
            }
        }
    }

    private fun inEditMode(): Boolean {
        return !viewModel.workoutID.value.isNullOrEmpty()
    }

    private fun extractWorkoutFromDB() {
        val uid = traineeUid ?: mAuth.currentUser?.uid
        if ((viewModel.workout_exercises.value?.size ?: 0) == 0) {
            if (!uid.isNullOrEmpty()) {
                if (viewModel.workoutID.value != null) {
                    firestore.collection("regular_users").document(uid).collection("workouts")
                            .document(viewModel.workoutID.value!!).get()
                            .addOnSuccessListener { document ->
                                val data = document.data
                                viewModel.workout_name.value = data?.get("name")?.toString()
                                viewModel.workout_desc.value = data?.get("desc")?.toString()
                                for (exercise in data?.get("exercises") as ArrayList<HashMap<String, String?>>) {
                                    when (exercise["type"]) {
                                        "Aerobic" -> {
                                            viewModel.workout_exercises.value?.add(
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
                                            viewModel.workout_exercises.value?.add(
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
                                setEmptyPlaceholderState()
                                supportActionBar?.title = "Edit Workout"
                                viewAdapter.notifyDataSetChanged()
                            }
                            .addOnFailureListener { exception ->
                                Log.w(null, "Error getting documents: ", exception)
                            }
                }
            }
        }
    }
}
