package android.technion.fitracker.models.nutrition

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddMealViewModel : ViewModel() {

    @Bindable
    val editTextMealName = MutableLiveData<String>()

    val data = ObservableArrayList<Map<String,String>>()
}