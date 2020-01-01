package com.technion.fitracker.user.personal.workout.edit

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.technion.fitracker.R
import com.technion.fitracker.databinding.ActivityEditWeightExerciseBinding
import com.technion.fitracker.models.nutrition.jsonDBModel
import com.technion.fitracker.models.workouts.CreateNewExerciseViewModel
import java.util.*

class EditWeightExercise : AppCompatActivity(), View.OnClickListener {
    private lateinit var viewModel: CreateNewExerciseViewModel
    private var index = 0
    private var cachedViewModel: Map<String, String?>? = null
    private lateinit var weightEditDoneFab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[CreateNewExerciseViewModel::class.java]
        val binding =
            DataBindingUtil.setContentView<ActivityEditWeightExerciseBinding>(this, R.layout.activity_edit_weight_exercise)
        binding.lifecycleOwner = this
        binding.myViewModel = viewModel
        setSupportActionBar(findViewById(R.id.edit_weight_toolbar))
        supportActionBar?.title = "Edit exercise"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initDB()
        val nameEditText = findViewById<AutoCompleteTextView>(R.id.weight_edit_name_input)
        val adapter = ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, viewModel.exerciseDB)
        nameEditText.setAdapter(adapter)
        intent.apply {
            viewModel.weight_name.value = getStringExtra("name")
            viewModel.weight_weight.value = getStringExtra("weight")
            viewModel.weight_sets.value = getStringExtra("sets")
            viewModel.weight_repetitions.value = getStringExtra("repetitions")
            viewModel.weight_rest.value = getStringExtra("rest")
            viewModel.weight_notes.value = getStringExtra("notes")
            index = getIntExtra("index", 0)
        }

        cacheViewModelValues()
        weightEditDoneFab = findViewById(R.id.weight_edit_done_fab)
        weightEditDoneFab.setOnClickListener(this)
    }

    private fun initDB() {
        val stream = this.assets.open("exercises.json")
        val s = Scanner(stream).useDelimiter("\\A")
        val json = if (s.hasNext()) {
            s.next()
        } else {
            ""
        }
        viewModel.exerciseDB = Gson().fromJson(json, jsonDBModel::class.java).array
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.edit_exercise_menu, menu)
        return true
    }

    private fun cacheViewModelValues() {
        if (cachedViewModel.isNullOrEmpty()) {
            cachedViewModel = mapOf(
                "name" to viewModel.weight_name.value,
                "weight" to viewModel.weight_weight.value,
                "sets" to viewModel.weight_sets.value,
                "repetitions" to viewModel.weight_repetitions.value,
                "rest" to viewModel.weight_rest.value,
                "notes" to viewModel.weight_notes.value
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        safeBack()
        return true
    }

    override fun onBackPressed() {
        safeBack()

    }

    private fun safeBack() {
        if (dataChanged()) {
            MaterialAlertDialogBuilder(this)
                    .setTitle("Warning")
                    .setMessage("You changes have not been saved")
                    .setPositiveButton(
                        "Save"
                    ) { _, _ ->
                        if (viewModel.weight_name.value == "") {
                            //TODO : refactor me to underline the field
                            Toast.makeText(this, "You must fill at least Name field!", Toast.LENGTH_LONG).show()
                        } else {
                            val intent = createIntentWithData()
                            this.setResult(CreateNewWorkoutActivity.ResultCodes.WEIGHT_EDIT.ordinal, intent)
                            this.finish()
                        }

                    }
                    .setNegativeButton(
                        "Discard"
                    ) { _, _ ->
                        this.setResult(CreateNewWorkoutActivity.ResultCodes.RETURN.ordinal)
                        this.finish()
                    }.show()
        } else {
            this.setResult(CreateNewWorkoutActivity.ResultCodes.RETURN.ordinal)
            this.finish()
        }
    }

    private fun dataChanged(): Boolean {
        return (cachedViewModel?.get("name") != viewModel.weight_name.value ||
                cachedViewModel?.get("weight") != viewModel.weight_weight.value ||
                cachedViewModel?.get("sets") != viewModel.weight_sets.value ||
                cachedViewModel?.get("repetitions") != viewModel.weight_repetitions.value ||
                cachedViewModel?.get("rest") != viewModel.weight_rest.value ||
                cachedViewModel?.get("notes") != viewModel.weight_notes.value)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_exercise -> {
                MaterialAlertDialogBuilder(this)
                        .setTitle("Warning")
                        .setMessage("Data will be lost, continue?")
                        .setPositiveButton(
                            "Yes"
                        ) { _, _ ->
                            val intent = Intent().apply { putExtra("index", index) }
                            this.apply {
                                setResult(CreateNewWorkoutActivity.ResultCodes.DELETE.ordinal, intent)
                                finish()
                            }
                        }
                        .setNegativeButton(
                            "No"
                        ) { _, _ ->

                        }.show()
            }
            //Back button
            else -> {
                safeBack()
            }
        }
        return true
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.weight_edit_done_fab -> {
                if (viewModel.weight_name.value == "") {
                    //TODO : refactor me to underline the field
                    Toast.makeText(v.context, "You must fill at least Name field!", Toast.LENGTH_LONG).show()
                } else {
                    val intent = createIntentWithData()
                    this.setResult(CreateNewWorkoutActivity.ResultCodes.WEIGHT_EDIT.ordinal, intent)
                    this.finish()
                }
            }
        }
    }

    private fun createIntentWithData(): Intent {
        return Intent().apply {
            putExtra("index", index)
            putExtra("name", viewModel.weight_name.value)
            putExtra("weight", viewModel.weight_weight.value)
            putExtra("sets", viewModel.weight_sets.value)
            putExtra("repetitions", viewModel.weight_repetitions.value)
            putExtra("rest", viewModel.weight_rest.value)
            putExtra("notes", viewModel.weight_notes.value)
        }
    }
}


