package com.technion.fitracker.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.fitracker.PendingRequestsActivity
import com.technion.fitracker.R
import com.technion.fitracker.models.NotificationsModel
import com.technion.fitracker.user.personal.HomeScreenFragment


class UserNotificationsFireStoreAdapter(options: FirestoreRecyclerOptions<NotificationsModel>, val fragment: HomeScreenFragment) :
        FirestoreRecyclerAdapter<NotificationsModel, UserNotificationsFireStoreAdapter.ViewHolder>(options) {

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
        if(holder.notification.text == "you have new pending requests"){
            holder.notification.text = "You have new pending requests"
        }


        holder.itemView.setOnClickListener {
            if (notification == "you have new pending requests" || notification == "You have new pending requests") {
                val user_pending_requests = Intent(holder.itemView.context, PendingRequestsActivity::class.java)
                user_pending_requests.putExtra("user_type", "regular")
                user_pending_requests.putExtra("user_name", fragment.viewModel.user_name)
                user_pending_requests.putExtra("user_photo_url", fragment.viewModel.user_photo_url)
                holder.itemView.context.startActivity(user_pending_requests)
            }
        }
    }


    override fun onDataChanged() {
        super.onDataChanged()
        if (itemCount == 0) {
            fragment.notifications_content_view.visibility = View.GONE
        } else {
            fragment.notifications_content_view.visibility = View.VISIBLE
        }
        fragment.setPlaceholder()
    }
}