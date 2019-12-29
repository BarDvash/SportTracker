package com.technion.fitracker.models.exercise

import com.technion.fitracker.models.exercise.ExerciseBaseModel


data class WeightExerciseModel(
    var name: String? = null,
    var weight: String? = null,
    var sets: String? = null,
    var repetitions: String? = null,
    var rest: String? = null,
    var notes: String? = null
) : ExerciseBaseModel("Weight")