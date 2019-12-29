package com.technion.fitracker.models

import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {

    @Bindable
    val editTextWeight = MutableLiveData<String>()

    @Bindable
    val editTextBodyFat = MutableLiveData<String>()

    @Bindable
    val editTextChest = MutableLiveData<String>()

    @Bindable
    val editTextWaist = MutableLiveData<String>()

    @Bindable
    val editTextHips = MutableLiveData<String>()

    @Bindable
    val editTextBiceps = MutableLiveData<String>()

}