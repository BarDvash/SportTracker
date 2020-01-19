package com.technion.fitracker.models

data class AppointmentCancelationModel (var customer_id: String? = null,
                                        var appointment_date: String? = null,
                                        var appointment_time: String? = null)