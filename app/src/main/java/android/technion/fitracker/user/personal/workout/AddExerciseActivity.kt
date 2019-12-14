package android.technion.fitracker.user.personal.workout

import android.os.Bundle
import android.technion.fitracker.R
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class AddExerciseActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var navController: NavController
    lateinit var weightChip: Chip
    lateinit var aerobicChip: Chip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_exercise)
        setSupportActionBar(findViewById(R.id.add_new_exercise_toolbar))
        navController = Navigation.findNavController(findViewById(R.id.exercise_navigation_host))
        weightChip = findViewById(R.id.weight_chip)
        aerobicChip = findViewById(R.id.aerobic_chip)
        weightChip.setOnClickListener(this)
        aerobicChip.setOnClickListener(this)
        supportActionBar?.title = "Add new exercise"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (checkIfDataExist()) {
            MaterialAlertDialogBuilder(this).setTitle("Warning").setMessage("Data will be lost, continue?")
                    .setPositiveButton(
                        "Yes"
                    ) { _, _ ->
                        this.setResult(CreateNewWorkoutActivity.ResultCodes.RETURN.ordinal)
                        this.finish()
                    }
                    .setNegativeButton(
                        "No"
                    ) { _, _ ->

                    }.show()
        }
        return true
    }

    override fun onBackPressed() {
        if (checkIfDataExist()) {
            MaterialAlertDialogBuilder(this)
                    .setTitle("Warning")
                    .setMessage("Data will be lost, continue?")
                    .setPositiveButton(
                        "Yes"
                    ) { _, _ ->
                        this.setResult(CreateNewWorkoutActivity.ResultCodes.RETURN.ordinal)
                        this.finish()
                    }
                    .setNegativeButton(
                        "No"
                    ) { _, _ ->

                    }.show()
        }

    }

    fun checkIfDataExist(): Boolean {
        return when (navController.currentDestination?.id) {
            R.id.weight_exercise -> {
                !findViewById<TextInputEditText>(R.id.weight_name_input).text.isNullOrBlank() ||
                        !findViewById<TextInputEditText>(R.id.weight_weight_input).text.isNullOrBlank() ||
                        !findViewById<TextInputEditText>(R.id.weight_notes_input).text.isNullOrBlank() ||
                        !findViewById<TextInputEditText>(R.id.weight_repetitions_input).text.isNullOrBlank() ||
                        !findViewById<TextInputEditText>(R.id.weight_sets_input).text.isNullOrBlank() ||
                        !findViewById<TextInputEditText>(R.id.weight_set_rest_input).text.isNullOrBlank()
            }
            R.id.aerobic_exercise -> {
                !findViewById<TextInputEditText>(R.id.aerobic_name_input).text.isNullOrBlank() ||
                        !findViewById<TextInputEditText>(R.id.aerobic_duration_input).text.isNullOrBlank() ||
                        !findViewById<TextInputEditText>(R.id.aerobic_intensity_input).text.isNullOrBlank() ||
                        !findViewById<TextInputEditText>(R.id.aerobic_notes_input).text.isNullOrBlank() ||
                        !findViewById<TextInputEditText>(R.id.aerobic_speed_input).text.isNullOrBlank()
            }
            else -> false
        }
    }

    override fun onClick(v: View?) {
        navController.popBackStack()
        when (v!!.id) {
            R.id.weight_chip -> {
                navController.navigate(R.id.weight_exercise)
                weightChip.isChecked = true
                aerobicChip.isChecked = false
            }
            R.id.aerobic_chip -> {
                navController.navigate(R.id.aerobic_exercise)
                weightChip.isChecked = false
                aerobicChip.isChecked = true
            }
        }
    }
}
