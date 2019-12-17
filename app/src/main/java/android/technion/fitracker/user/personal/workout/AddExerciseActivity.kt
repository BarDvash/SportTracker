package android.technion.fitracker.user.personal.workout

import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.models.workouts.CreateNewExerciseViewModel
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import android.technion.fitracker.databinding.ActivityAddExerciseBinding
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class AddExerciseActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var navController: NavController
    lateinit var weightChip: Chip
    lateinit var aerobicChip: Chip
    lateinit var viewModel: CreateNewExerciseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[CreateNewExerciseViewModel::class.java]
//        viewModel.weightFields.value?.set("name", "lol")
        val binding = DataBindingUtil.setContentView<ActivityAddExerciseBinding>(this, R.layout.activity_add_exercise)
        binding.lifecycleOwner = this
        binding.myViewModel = viewModel
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
        if (!checkIfDataExist()) {
            this.finish()
        }else{
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
        if (!checkIfDataExist()) {
            this.finish()
        }else{
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
                !viewModel.weight_name.value.isNullOrBlank() ||
                        !viewModel.weight_weight.value.isNullOrBlank() ||
                        !viewModel.weight_sets.value.isNullOrBlank() ||
                        !viewModel.weight_repetitions.value.isNullOrBlank() ||
                        !viewModel.weight_rest.value.isNullOrBlank() ||
                        !viewModel.weight_notes.value.isNullOrBlank()
            }
            R.id.aerobic_exercise -> {
                !viewModel.aerobic_name.value.isNullOrBlank() ||
                        !viewModel.aerobic_duration.value.isNullOrBlank() ||
                        !viewModel.aerobic_speed.value.isNullOrBlank() ||
                        !viewModel.aerobic_intensity.value.isNullOrBlank() ||
                        !viewModel.aerobic_notes.value.isNullOrBlank()
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
