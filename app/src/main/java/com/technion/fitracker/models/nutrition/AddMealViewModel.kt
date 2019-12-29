package com.technion.fitracker.models.nutrition

import androidx.databinding.Bindable
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddMealViewModel : ViewModel() {

    @Bindable
    val editTextMealName = MutableLiveData<String>()

    var data = ObservableArrayList<Map<String,String>>()

    var dishes :HashMap<String,String> = HashMap()

    var docId: String? = null


}