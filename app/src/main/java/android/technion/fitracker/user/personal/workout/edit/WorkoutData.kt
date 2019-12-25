package android.technion.fitracker.user.personal.workout.edit

import android.technion.fitracker.models.exercise.ExerciseBaseModel

data class WorkoutData(
    val name: String? = null,
    val desc: String? = null,
    val exercises: ArrayList<ExerciseBaseModel>? = null
)