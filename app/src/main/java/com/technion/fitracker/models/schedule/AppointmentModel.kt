package com.technion.fitracker.models.schedule

class AppointmentModel(
    var customer_id: String? = null,
    var appointment_date: String? = null,
    var appointment_time: String? = null,
    var notes: String? = null
)