package android.technion.fitracker.user.personal.workout

import android.content.Intent
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.ExerciseAdapter
import android.technion.fitracker.databinding.ActivityCreateNewWorkoutBinding
import android.technion.fitracker.models.exercise.AerobicExerciseModel
import android.technion.fitracker.models.exercise.ExerciseBaseModel
import android.technion.fitracker.models.exercise.WeightExerciseModel
import android.technion.fitracker.models.workouts.CreateWorkoutViewModel
import android.technion.fitracker.user.Workout
import android.technion.fitracker.user.personal.workout.CreateNewWorkoutActivity.ResultCodes.*
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

    lateinit var viewModel: CreateWorkoutViewModel
    lateinit var fab: ExtendedFloatingActionButton
    lateinit var noWorkoutHint: TextView
    lateinit var recyclerView: RecyclerView
    lateinit var firestore: FirebaseFirestore
    lateinit var firebase: FirebaseAuth
    lateinit var viewAdapter: ExerciseAdapter
    lateinit var viewManager: RecyclerView.LayoutManager

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
        firebase = FirebaseAuth.getInstance()
        noWorkoutHint = findViewById(R.id.no_workouts_hint)
        viewManager = LinearLayoutManager(this)
        val exercises = viewModel.workout_exercises.value
        val onItemClickListener = View.OnClickListener { v ->
            val rvh = v.tag as RecyclerView.ViewHolder
            var s = exercises?.get(rvh.adapterPosition)
            when (s?.type) {
                "Aerobic" -> {
                    s = s as AerobicExerciseModel
                    val intent = Intent(this, EditAerobicExercise::class.java)
                    intent.putExtra("name", s.name)
                    intent.putExtra("duration", s.duration)
                    intent.putExtra("speed", s.speed)
                    intent.putExtra("intensity", s.intensity)
                    intent.putExtra("notes", s.notes)
                    intent.putExtra("index", rvh.adapterPosition)
                    startActivityForResult(intent,ResultCodes.AEROBIC_EDIT.ordinal)
                }
                "Weight" -> {
                    s = s as WeightExerciseModel
                    val intent = Intent(this, EditWeightExercise::class.java)
                    intent.putExtra("name", s.name)
                    intent.putExtra("weight", s.weight)
                    intent.putExtra("sets", s.sets)
                    intent.putExtra("repetitions", s.repetitions)
                    intent.putExtra("rest", s.rest)
                    intent.putExtra("notes", s.notes)
                    intent.putExtra("index", rvh.adapterPosition)
                    startActivityForResult(intent, ResultCodes.WEIGHT_EDIT.ordinal)
                }
            }
        }
        viewAdapter = ExerciseAdapter(exercises!!).apply {
            mOnItemClickListener = onItemClickListener
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

    private fun dataExist(): Boolean {
        return viewModel.workout_exercises.value?.size!! > 0 ||
                !viewModel.workout_name.value.isNullOrEmpty() ||
                !viewModel.workout_name.value.isNullOrEmpty()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.add_exercise_fab -> {
                val createNewWorkoutActivity = Intent(v.context!!, AddExerciseActivity::class.java)
                startActivityForResult(createNewWorkoutActivity, ResultCodes.WEIGHT.ordinal)
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
        val uid = firebase.currentUser?.uid
        val workout =
            Workout(viewModel.workout_name.value, viewModel.workout_desc.value, viewModel.workout_exercises.value)
        if (uid != null) {
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
