package android.technion.fitracker.user.personal.workout.edit

import android.content.Intent
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.ExerciseAdapter
import android.technion.fitracker.databinding.ActivityCreateNewWorkoutBinding
import android.technion.fitracker.models.exercise.AerobicExerciseModel
import android.technion.fitracker.models.exercise.ExerciseBaseModel
import android.technion.fitracker.models.exercise.WeightExerciseModel
import android.technion.fitracker.models.workouts.CreateWorkoutViewModel
import android.technion.fitracker.user.personal.workout.WorkoutStarter
import android.technion.fitracker.user.personal.workout.edit.CreateNewWorkoutActivity.ResultCodes.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[CreateWorkoutViewModel::class.java]
        val binding =
            DataBindingUtil.setContentView<ActivityCreateNewWorkoutBinding>(this, R.layout.activity_create_new_workout)
        binding.lifecycleOwner = this
        binding.newWorkoutViewModel = viewModel
        setSupportActionBar(findViewById(R.id.create_workout_toolbar))
        supportActionBar?.title = "Create Workout"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        firestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        viewManager = LinearLayoutManager(this)
        noWorkoutHint = findViewById(R.id.no_workouts_hint)
        viewModel.workoutID.value = intent.getStringExtra("workoutID")
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
        viewAdapter = ExerciseAdapter(exercises!!).apply {
            mOnItemClickListener = onItemClickListener
        }
        if(!viewModel.workoutID.value.isNullOrEmpty()){
            extractWorkoutFromDB()

        }else{
            supportActionBar?.title = "Create Workout"
        }
        recyclerView = findViewById<RecyclerView>(R.id.create_workout_recycler).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter

        }
        fab = findViewById(R.id.add_exercise_fab)
        fab.setOnClickListener(this)


    }

    override fun onResume() {
        super.onResume()
        setEmptyPlaceholderState()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_workout_save_item -> {
                saveWorkoutToFirestore()
            }
            //Back button
            else -> {
                safeExit()
            }
        }
        return true
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
        if(inEditMode()){
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
        }else {
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
        val uid = mAuth.currentUser?.uid
        val workout =
            WorkoutData(viewModel.workout_name.value, viewModel.workout_desc.value, viewModel.workout_exercises.value)
        if (uid != null) {
            if(inEditMode()){
                firestore.collection("regular_users")
                        .document(uid).collection("workouts").document(viewModel.workoutID.value!!).set(workout)
                        .addOnSuccessListener { documentReference ->
                            Log.d(FragmentActivity.VIBRATOR_SERVICE, "DocumentSnapshot updated with ID: " + viewModel.workoutID.value)
                            this.setResult(WorkoutStarter.ResultCodes.EDIT.ordinal)
                            this.finish()
                        }
                        .addOnFailureListener { e -> Log.w(FragmentActivity.VIBRATOR_SERVICE, "Error updating document document", e) }
            }else {
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

    private fun inEditMode(): Boolean{
        return !viewModel.workoutID.value.isNullOrEmpty()
    }

    private fun extractWorkoutFromDB() {
        val uid = mAuth.currentUser?.uid
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
