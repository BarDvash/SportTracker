package com.technion.fitracker.models


import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.technion.fitracker.adapters.BusinessNotificationsFireStoreAdapter


class BusinessUserViewModel : ViewModel() {

    //bar dvash fields:
    var user_type: String? = null
    var user_name: String? = null
    var user_photo_url: String? = null
    var user_phone_number: String? = null



    //Home fields
    var notificaations_rec_view: RecyclerView? = null
    var notifications_adapter: BusinessNotificationsFireStoreAdapter? = null

}