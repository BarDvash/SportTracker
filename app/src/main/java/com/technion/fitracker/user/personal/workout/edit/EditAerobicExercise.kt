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
import com.technion.fitracker.databinding.ActivityEditAerobicExerciseBinding
import com.technion.fitracker.models.nutrition.jsonDBModel
import com.technion.fitracker.models.workouts.CreateNewExerciseViewModel
import java.util.*

class EditAerobicExercise : AppCompatActivity(), View.OnClickListener {
    private lateinit var viewModel: CreateNewExerciseViewModel
    private var index = 0
    private var cachedViewModel: Map<String, String?>? = null
    private lateinit var aerobic_edit_done_fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[CreateNewExerciseViewModel::class.java]
        val binding =
            DataBindingUtil.setContentView<ActivityEditAerobicExerciseBinding>(this, R.layout.activity_edit_aerobic_exercise)
        binding.lifecycleOwner = this
        binding.myViewModel = viewModel
        setSupportActionBar(findViewById(R.id.edit_aerobic_toolbar))
        supportActionBar?.title = "Edit exercise"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        intent.apply {
            viewModel.aerobic_name.value = getStringExtra("name")
            viewModel.aerobic_duration.value = getStringExtra("duration")
            viewModel.aerobic_speed.value = getStringExtra("speed")
            viewModel.aerobic_intensity.value = getStringExtra("intensity")
            viewModel.aerobic_notes.value = getStringExtra("notes")
            index = getIntExtra("index", 0)
        }
        cacheViewModelValues()
        aerobic_edit_done_fab = findViewById(R.id.aerobic_edit_done_fab)
        aerobic_edit_done_fab.setOnClickListener(this)
    }

    private fun cacheViewModelValues() {
        if (cachedViewModel.isNullOrEmpty()) {
            cachedViewModel = mapOf(
                    "name" to viewModel.aerobic_name.value,
                    "duration" to viewModel.aerobic_duration.value,
                    "speed" to viewModel.aerobic_speed.value,
                    "intensity" to viewModel.aerobic_intensity.value,
                    "notes" to viewModel.aerobic_notes.value
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.edit_exercise_menu, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        safeBack()
        return true
    }

    override fun onBackPressed() {
        safeBack()

    }

    private fun dataChanged(): Boolean {
        return (cachedViewModel?.get("name") != viewModel.aerobic_name.value ||
                cachedViewModel?.get("duration") != viewModel.aerobic_duration.value ||
                cachedViewModel?.get("speed") != viewModel.aerobic_speed.value ||
                cachedViewModel?.get("intensity") != viewModel.aerobic_intensity.value ||
                cachedViewModel?.get("notes") != viewModel.aerobic_notes.value)
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
                            if (viewModel.weight_name.value == "") {
                                //TODO : refactor me to underline the field
                                Toast.makeText(this, "You must fill at least Name field!", Toast.LENGTH_LONG).show()
                            } else {
                                val intent = Intent().apply { putExtra("index", index) }
                                this.setResult(CreateNewWorkoutActivity.ResultCodes.DELETE.ordinal, intent)
                                this.finish()
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

    private fun safeBack() {
        if (dataChanged()) {
            MaterialAlertDialogBuilder(this)
                    .setTitle("Warning")
                    .setMessage("You changes have not been saved")
                    .setPositiveButton(
                            "Save"
                    ) { _, _ ->
                        val intent = createIntentWithData()
                        this.apply {
                            setResult(CreateNewWorkoutActivity.ResultCodes.AEROBIC_EDIT.ordinal, intent)
                            finish()
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

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.aerobic_edit_done_fab -> {
                if (viewModel.aerobic_name.value == "") {
                    //TODO : refactor me to underline the field
                    Toast.makeText(v.context, "You must fill at least Name field!", Toast.LENGTH_LONG).show()
                } else {
                    val intent = createIntentWithData()
                    this.apply {
                        setResult(CreateNewWorkoutActivity.ResultCodes.AEROBIC_EDIT.ordinal, intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun createIntentWithData(): Intent {
        return Intent().apply {
            putExtra("index", index)
            putExtra("name", viewModel.aerobic_name.value)
            putExtra("duration", viewModel.aerobic_duration.value)
            putExtra("speed", viewModel.aerobic_speed.value)
            putExtra("intensity", viewModel.aerobic_intensity.value)
            putExtra("notes", viewModel.aerobic_notes.value)
        }
    }
}
