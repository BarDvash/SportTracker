package com.technion.fitracker.models.workouts

import androidx.databinding.Bindable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.technion.fitracker.models.exercise.ExerciseWithGifDBElement

class CreateNewExerciseViewModel : ViewModel() {

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
    val weight_muscle_category = ObservableField<String>()
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

    var weight_gif_url: String? = null

    var exerciseDB: Map<String, Array<ExerciseWithGifDBElement>> = mapOf()

    fun findExercise(exerciseName: String): ExerciseWithGifDBElement{
        for (exe in exerciseDB){
            for (e in exe.value){
                if(e.name == exerciseName){
                    return ExerciseWithGifDBElement(e.name,e.gif_url,exe.key)
                }
            }
        }
        return ExerciseWithGifDBElement("NONE","NONE",null)
    }
}
