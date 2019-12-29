package com.technion.fitracker.models.workouts

data class RecentWorkoutFireStoreModel(
    var workout_name: String? = null,
    var date_time: String? = null
)