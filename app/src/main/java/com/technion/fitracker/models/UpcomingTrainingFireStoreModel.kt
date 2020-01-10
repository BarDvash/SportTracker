package com.technion.fitracker.models

data class UpcomingTrainingFireStoreModel(
    var customer_id: String? = null,
    var appointment_date: String? = null,
    var appointment_time: String? = null,
    var notes: String? = null
)