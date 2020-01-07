package com.technion.fitracker.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.fitracker.R
import com.technion.fitracker.models.NotificationsModel
import com.technion.fitracker.user.business.HomeScreenFragment


class BusinessNotificationsFireStoreAdapter(options: FirestoreRecyclerOptions<NotificationsModel>, val fragment: HomeScreenFragment) :
        FirestoreRecyclerAdapter<NotificationsModel, BusinessNotificationsFireStoreAdapter.ViewHolder>(options) {

    var mOnItemClickListener: View.OnClickListener? = null




    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.tag = this
            view.setOnClickListener(mOnItemClickListener)
        }
        var notification: TextView = view.findViewById(R.id.notification_text)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.element_notification_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        item: NotificationsModel
    ) {
        val notification = item.notification
        holder.notification.text = notification
    }


    override fun onDataChanged() {
        super.onDataChanged()
        if (itemCount == 0) {
            fragment.notifications_content_view.visibility = View.GONE
        } else {
            fragment.notifications_content_view.visibility = View.VISIBLE
        }
    }
}