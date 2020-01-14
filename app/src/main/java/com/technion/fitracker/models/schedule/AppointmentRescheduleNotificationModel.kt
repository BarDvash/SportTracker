package com.technion.fitracker.models.schedule

data class AppointmentRescheduleNotificationModel(var trainerId: String? = null,
                                                  var traineeId: String? = null,
                                                  var oldDate: String? = null,
                                                  var newDate: String? = null)