package com.technion.fitracker.models

import androidx.databinding.Bindable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.technion.fitracker.adapters.*
import com.technion.fitracker.adapters.measurements.MeasurementsRecyclerViewAdapter
import com.technion.fitracker.adapters.nutrition.NutritionFireStoreAdapter
import com.technion.fitracker.models.nutrition.NutritionFireStoreModel

class UserViewModel : ViewModel() {

    //bar dvash fields:
    var user_type: String? = null
    var user_name: String? = null
    var user_photo_url: String? = null
    var user_phone_number: String? = null

    //Home fields
    var homeRecentWorkoutRV: RecyclerView? = null
    var homeRecentWorkoutsAdapter: RecentWorkoutsFireStoreAdapter? = null

    var personalTrainerUID: String? = null
    var personalTrainerRV: RecyclerView? = null
    var personalTrainerAdapter: MyTrainerFireStoreAdapter? = null


    var notificaations_rec_view: RecyclerView? = null
    var notifications_adapter: UserNotificationsFireStoreAdapter? = null

    //Nutrition fields
    var nutritionRV: RecyclerView? = null
    var nutritionAdapter: FirestoreRecyclerAdapter<NutritionFireStoreModel, NutritionFireStoreAdapter.ViewHolder>? = null

    //WorkoutFragment fields
    var workoutsRecyclerView: RecyclerView? = null
    var workoutsAdapter: FirestoreRecyclerAdapter<WorkoutFireStoreModel, WorkoutsFireStoreAdapter.ViewHolder>? = null

    var upcomingWorkoutsRV: RecyclerView? = null
    var upcomingWorkoutsAdapter: UpcomingTrainingsFireStoreAdapter? = null

    //MeasurementsFragment fields
    var measurementRV: RecyclerView? = null
    var measurementsRVAdapter: MeasurementsRecyclerViewAdapter? = null



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

    @Bindable
    val textViewDate = ObservableField("")


}