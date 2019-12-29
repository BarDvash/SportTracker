package com.technion.fitracker.models.workouts

import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateNewExerciseViewModel : ViewModel(){

    @Bindable
    val aerobic_name = MutableLiveData<String>()
    @Bindable
    val aerobic_duration = MutableLiveData<String>()
    @Bindable
    val aerobic_speed = MutableLiveData<String>()
    @Bindable
    val aerobic_intensity = MutableLiveData<String>()
    @Bindable
    val aerobic_notes = MutableLiveData<String>()

    @Bindable
    val weight_name = MutableLiveData<String>()
    @Bindable
    val weight_weight = MutableLiveData<String>()
    @Bindable
    val weight_sets = MutableLiveData<String>()
    @Bindable
    val weight_repetitions = MutableLiveData<String>()
    @Bindable
    val weight_rest = MutableLiveData<String>()
    @Bindable
    val weight_notes = MutableLiveData<String>()

}