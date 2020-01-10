package com.technion.fitracker.models


data class SearchFireStoreModel(
    var name: String? = null,
    var photoURL: String? = null,
    var phone_number: String? = null,
    var uid: String? = null,
    var type: String? = null,
    var landing_info: String? = null,
    var personal_trainer_uid: String? = null
)