package android.technion.fitracker.user.personal.workout

import android.os.Bundle
import android.technion.fitracker.R
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.chip.Chip

class AddExerciseActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_exercise)
        setSupportActionBar(findViewById(R.id.add_new_exercise_toolbar))
        navController = Navigation.findNavController(findViewById(R.id.exercise_navigation_host))
        val weightChip = findViewById<Chip>(R.id.weight_chip)
        val aerobicChip = findViewById<Chip>(R.id.aerobic_chip)
        weightChip.setOnClickListener(this)
        aerobicChip.setOnClickListener(this)
        supportActionBar?.title = "Add new exercise"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        this.setResult(CreateNewWorkoutActivity.ResultCodes.RETURN.ordinal)
        this.finish()
        return true
    }


    override fun onClick(v: View?) {

        when (v!!.id) {
            R.id.weight_chip -> {
                navController.navigate(R.id.weight_exercise)
                findViewById<Chip>(R.id.weight_chip).isChecked = true
                findViewById<Chip>(R.id.aerobic_chip).isChecked = false
            }
            R.id.aerobic_chip -> {
                navController.navigate(R.id.aerobic_exercise)
                findViewById<Chip>(R.id.weight_chip).isChecked = false
                findViewById<Chip>(R.id.aerobic_chip).isChecked = true
            }
        }
    }
}
