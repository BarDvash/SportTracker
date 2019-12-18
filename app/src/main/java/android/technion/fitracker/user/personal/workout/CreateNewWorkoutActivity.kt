package android.technion.fitracker.user.personal.workout

import android.content.Intent
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.ExerciseAdapter
import android.technion.fitracker.models.exercise.AerobicExerciseModel
import android.technion.fitracker.models.exercise.ExerciseBaseModel
import android.technion.fitracker.models.exercise.WeightExerciseModel
import android.technion.fitracker.user.Workout
import android.technion.fitracker.user.personal.workout.CreateNewWorkoutActivity.ResultCodes.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.ArrayList


class CreateNewWorkoutActivity : AppCompatActivity(), View.OnClickListener {
    enum class ResultCodes {
        AEROBIC, WEIGHT, RETURN
    }

    lateinit var fab: FloatingActionButton
    lateinit var noWorkoutHint: TextView
    lateinit var recyclerView: RecyclerView
    lateinit var firestore: FirebaseFirestore
    lateinit var firebase: FirebaseAuth
    lateinit var viewAdapter: ExerciseAdapter
    lateinit var viewManager: RecyclerView.LayoutManager
    lateinit var exercisesList: ArrayList<ExerciseBaseModel>
    lateinit var onItemClickListener: View.OnClickListener
    lateinit var nameField: TextInputEditText
    lateinit var descField: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_workout)
        setSupportActionBar(findViewById(R.id.create_workout_toolbar))
        supportActionBar?.title = "Create Workout"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        firestore = FirebaseFirestore.getInstance()
        firebase = FirebaseAuth.getInstance()
        noWorkoutHint = findViewById(R.id.no_workouts_hint)
        exercisesList = ArrayList()
        nameField = this.findViewById(R.id.create_workout_name_input)
        descField = findViewById(R.id.create_workout_desc_input)
        viewManager = LinearLayoutManager(this)
        onItemClickListener = View.OnClickListener { v ->
            val rvh = v.tag as RecyclerView.ViewHolder
            val s = exercisesList.get(rvh.adapterPosition).type
            Toast.makeText(this, "You clicked on " + s, Toast.LENGTH_LONG).show()
        }
        viewAdapter = ExerciseAdapter(exercisesList)
        viewAdapter.setOnItemClickListener(onItemClickListener)
        recyclerView = findViewById<RecyclerView>(R.id.create_workout_recyclev).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter

        }
        fab = findViewById(R.id.add_exercise_fab)
        fab.setOnClickListener(this)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_workout_save_item -> {
                saveWorkoutToFirestore()
            }
            else -> {
                //Handling Up button which has no ID
                safeExit()
            }
        }
        return true
    }

    fun setEmptyPlaceholderState() {
        if (exercisesList.isNotEmpty()) {
            noWorkoutHint.visibility = View.GONE
        } else {
            noWorkoutHint.visibility = View.VISIBLE
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            AEROBIC.ordinal -> {
                val aerobicExercise = AerobicExerciseModel(
                    data?.getStringExtra("name"),
                    data?.getStringExtra("duration"),
                    data?.getStringExtra("speed"),
                    data?.getStringExtra("intensity"),
                    data?.getStringExtra("notes")
                ) as ExerciseBaseModel
                exercisesList.add(aerobicExercise)
                setEmptyPlaceholderState()
                viewAdapter.notifyDataSetChanged()
            }
            WEIGHT.ordinal -> {
                val weightExercise = WeightExerciseModel(
                    data?.getStringExtra("name"),
                    data?.getStringExtra("weight"),
                    data?.getStringExtra("sets"),
                    data?.getStringExtra("repetitions"),
                    data?.getStringExtra("rest"),
                    data?.getStringExtra("notes")
                ) as ExerciseBaseModel
                exercisesList.add(weightExercise)
                setEmptyPlaceholderState()
                viewAdapter.notifyDataSetChanged()
            }
            RETURN.ordinal -> {
                return
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.add_new_workout_menu, menu)
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
        }else{
            this.finish()
        }
    }

    fun dataExist(): Boolean {
        return exercisesList.size > 0 ||
                !nameField.text.isNullOrEmpty() ||
                !descField.text.isNullOrEmpty()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.add_exercise_fab -> {
                val createNewWorkoutActivity = Intent(v.context!!, AddExerciseActivity::class.java)
                startActivityForResult(createNewWorkoutActivity, 1)
            }
            R.id.new_workout_save_item -> {
                saveWorkoutToFirestore()
            }
        }
    }

    private fun saveWorkoutToFirestore() {
        val nameInput: String = findViewById<TextInputEditText>(R.id.create_workout_name_input).text.toString()
        val descInput: String = findViewById<TextInputEditText>(R.id.create_workout_desc_input).text.toString()
        if (nameInput.isEmpty()) {
            Toast.makeText(this, "Name is a must field!", Toast.LENGTH_LONG).show()
            return
        }
        if (exercisesList.size < 1) {
            Toast.makeText(this, "Workout must have atleast one exercise!", Toast.LENGTH_LONG).show()
            return
        }
        val uid = firebase.currentUser?.uid
        val workout = Workout(nameInput, descInput, exercisesList)
        if (uid != null) {
            firestore.collection("regular_users")
                    .document(uid).collection("workouts").add(workout)
                    .addOnSuccessListener { documentReference -> Log.d(FragmentActivity.VIBRATOR_SERVICE, "DocumentSnapshot added with ID: " + documentReference.id)
                        this.finish()
                    }
                    .addOnFailureListener { e -> Log.w(FragmentActivity.VIBRATOR_SERVICE, "Error adding document", e) }
        }
    }
}
