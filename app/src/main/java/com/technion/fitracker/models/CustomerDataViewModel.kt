package com.technion.fitracker.models

import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.technion.fitracker.adapters.WorkoutsFireStoreAdapter
import com.technion.fitracker.adapters.measurements.MeasurementsRecyclerViewAdapter
import com.technion.fitracker.adapters.nutrition.NutritionFireStoreAdapter
import com.technion.fitracker.models.nutrition.NutritionFireStoreModel

class CustomerDataViewModel: ViewModel() {
    //Nutrition fields
    var nutritionRV: RecyclerView? = null
    var nutritionAdapter: FirestoreRecyclerAdapter<NutritionFireStoreModel, NutritionFireStoreAdapter.ViewHolder>? = null

    //WorkoutFragment fields
    var workoutsRecyclerView: RecyclerView? = null
    var workoutsAdapter: FirestoreRecyclerAdapter<WorkoutFireStoreModel, WorkoutsFireStoreAdapter.ViewHolder>? = null

    //MeasurementsFragment fields
    var measurementRV: RecyclerView? = null
    var measurementsRVAdapter: MeasurementsRecyclerViewAdapter? = null

    //Customer Data
    var customerID: String? = null
    var customerName: String? = null
}