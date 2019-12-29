package com.technion.fitracker.models.workouts


import com.technion.fitracker.models.exercise.ExerciseBaseModel
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateWorkoutViewModel : ViewModel(){
    val workoutID = MutableLiveData<String>()
    @Bindable
    val workout_name = MutableLiveData<String>()
    @Bindable
    val workout_desc = MutableLiveData<String>()
    @Bindable
    val workout_exercises = MutableLiveData<ArrayList<ExerciseBaseModel>>()
    init {
        workout_exercises.value = arrayListOf()
    }

}