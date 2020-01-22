package com.technion.fitracker.models.exercise

data class ExerciseWithGifDBElement(
    val name: String,
    var gif_url: String,
    var type: String? = null
)