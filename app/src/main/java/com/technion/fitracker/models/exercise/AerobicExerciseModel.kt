package com.technion.fitracker.models.exercise


data class AerobicExerciseModel(
    var name: String? = null,
    var duration: String? = null,
    var speed: String? = null,
    var intensity: String? = null,
    var notes: String? = null
): ExerciseBaseModel("Aerobic")