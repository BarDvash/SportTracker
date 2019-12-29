package com.technion.fitracker.user

data class Meal(
    val name: String? = null,
    val meals: List<Map<String,String>>? = null
)