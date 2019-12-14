package android.technion.fitracker.user

import android.technion.fitracker.models.exercise.ExerciseBaseModel

data class Workout(val name: String? = null, val desc: String? = null,val exercises: ArrayList<ExerciseBaseModel>? = null)