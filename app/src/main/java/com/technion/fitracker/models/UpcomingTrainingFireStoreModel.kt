package com.technion.fitracker.models

data class UpcomingTrainingFireStoreModel(
    var customer_id: String? = null,
    var appointment_date: Long? = null,
    var notes: String? = null
)