package com.technion.fitracker.models

data class PendingRequestFireStoreModel(
    var user_id: String? = null,
    var user_name: String? = null,
    var user_photo_url: String? = null,
    var user_phone_number: String? = null
)