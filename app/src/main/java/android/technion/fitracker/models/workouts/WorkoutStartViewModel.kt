package android.technion.fitracker.models.workouts

import android.technion.fitracker.models.exercise.ExerciseBaseModel
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WorkoutStartViewModel : ViewModel(){
    @Bindable
    val workoutID = MutableLiveData<String>()
    @Bindable
    val workoutName = MutableLiveData<String>()
    @Bindable
    val stopwatch = MutableLiveData<Int>()
    @Bindable
    val comment = MutableLiveData<String>()

    val workoutExercises = MutableLiveData<ArrayList<ExerciseBaseModel>>()
    init {
        workoutExercises.value = arrayListOf()
    }
    val ratings = MutableLiveData<Map<String,Int>>()
    init{
        mapOf<String,String>()
    }
    val times = MutableLiveData<Map<String,String>>()
    init{
        mapOf<String,String>()
    }
}