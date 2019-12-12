package android.technion.fitracker.user.personal.workout

import android.content.Intent
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.ExerciseAdapter
import android.technion.fitracker.models.exercise.AerobicExerciseModel
import android.technion.fitracker.models.exercise.ExerciseBaseModel
import android.technion.fitracker.models.exercise.WeightExerciseModel
import android.technion.fitracker.user.personal.workout.CreateNewWorkoutActivity.ResultCodes.*
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton



class CreateNewWorkoutActivity : AppCompatActivity(), View.OnClickListener {
    enum class ResultCodes {
        AEROBIC, WEIGHT, RETURN
    }

    lateinit var fab: FloatingActionButton
    lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    lateinit var exercisesList: ArrayList<ExerciseBaseModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_workout)
        setSupportActionBar(findViewById(R.id.create_workout_toolbar))
        supportActionBar?.title = "Create Workout"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        exercisesList = ArrayList()
        viewManager = LinearLayoutManager(this)
        viewAdapter = ExerciseAdapter(exercisesList)
        recyclerView = findViewById<RecyclerView>(R.id.create_workout_recyclev).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }
        fab = findViewById(R.id.add_exercise_fab)
        fab.setOnClickListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        this.finish()
        return true
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onStop() {
        super.onStop()
    }

    fun createAerobicObjectFromData(
        name: String?,
        duration: String?,
        speed: String?,
        intensity: String?,
        notes: String?
    ): AerobicExerciseModel? {
        return AerobicExerciseModel(
            name,
            duration,
            speed,
            intensity,
            notes
        )
    }

    fun createWeightObjectFromData(
        name: String?,
        weight: String?,
        sets: String?,
        repetitions: String?,
        rest: String?,
        notes: String?
    ): WeightExerciseModel? {
        return WeightExerciseModel(
            name,
            weight,
            sets,
            repetitions,
            rest,
            notes
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            AEROBIC.ordinal -> {
                val aerobicExercise = createAerobicObjectFromData(
                    data?.getStringExtra("name"),
                    data?.getStringExtra("duration"),
                    data?.getStringExtra("speed"),
                    data?.getStringExtra("intensity"),
                    data?.getStringExtra("notes")
                ) as ExerciseBaseModel
                exercisesList.add(aerobicExercise)
                viewAdapter.notifyDataSetChanged()
            }
            WEIGHT.ordinal -> {
                val weightExercise = createWeightObjectFromData(
                    data?.getStringExtra("name"),
                    data?.getStringExtra("weight"),
                    data?.getStringExtra("sets"),
                    data?.getStringExtra("repetitions"),
                    data?.getStringExtra("rest"),
                    data?.getStringExtra("notes")
                ) as ExerciseBaseModel
                exercisesList.add(weightExercise)
                viewAdapter.notifyDataSetChanged()
            }
            RETURN.ordinal -> {
                return
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.add_exercise_fab -> {
                val createNewWorkoutActivity = Intent(v.context!!, AddExerciseActivity::class.java)
                startActivityForResult(createNewWorkoutActivity, 1)
            }
        }
    }
}
