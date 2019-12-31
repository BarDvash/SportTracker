package com.technion.fitracker.models.workouts

import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.technion.fitracker.models.exercise.ExerciseBaseModel

class WorkoutStartViewModel : ViewModel() {
    @Bindable
    val workoutID = MutableLiveData<String>()
    @Bindable
    val workoutName = MutableLiveData<String>()
    @Bindable
    val timeElapsed = MutableLiveData<String>()
    @Bindable
    val workoutComment = MutableLiveData<String>()

    var stopwatch: Long = 0
    @Bindable
    val comment = MutableLiveData<String>()


    var workoutRate: Int? = null

    var started = false
    val workoutExercises = MutableLiveData<ArrayList<ExerciseBaseModel>>()

    init {
        workoutExercises.value = arrayListOf()
    }

    val times = MutableLiveData<Map<String, String>>()

    init {
        mapOf<String, String>()
    }
}