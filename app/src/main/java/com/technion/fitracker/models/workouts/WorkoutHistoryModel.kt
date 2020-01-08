package com.technion.fitracker.models.workouts

import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.technion.fitracker.models.exercise.ExerciseLogModel
import java.util.*

class WorkoutHistoryModel : ViewModel() {
    @Bindable
    val workoutName = MutableLiveData<String>()
    @Bindable
    val timeElapsed = MutableLiveData<String>()
    @Bindable
    val workoutDate = MutableLiveData<String>()
    @Bindable
    val workoutComment = MutableLiveData<String>()
    @Bindable
    val workoutRating = MutableLiveData<Long>()

    var workoutID: String? = null

    val workoutExercises = MutableLiveData<ArrayList<ExerciseLogModel>>()
    init {
        workoutExercises.value = arrayListOf()
    }

}