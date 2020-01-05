package com.technion.fitracker.adapters



import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.fitracker.PendingRequestsActivity
import com.technion.fitracker.R
import com.technion.fitracker.models.PendingRequestFireStoreModel



class PendingRequestFireStoreAdapter(options: FirestoreRecyclerOptions<PendingRequestFireStoreModel>, val pending_request_activity: PendingRequestsActivity) :
        FirestoreRecyclerAdapter<PendingRequestFireStoreModel, PendingRequestFireStoreAdapter.ViewHolder>(options) {

    var mOnItemClickListener: View.OnClickListener? = null
    private lateinit var firestore: FirebaseFirestore
    private var current_user_id: String? = null



    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.tag = this
            view.setOnClickListener(mOnItemClickListener)
        }
        var user_name: TextView = view.findViewById(R.id.pending_request_user_name)
        var user_image_view: ImageView = view.findViewById(R.id.pending_request_imageView)
        var button: Button = view.findViewById(R.id.pending_request_button)
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.element_pending_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        item: PendingRequestFireStoreModel
    ) {
        val user_name_value = item.user_name
        val user_photo_url = item.user_photo_url
        val user_id = item.user_id

        holder.user_name.text = user_name_value
        if (!item?.user_photo_url.isNullOrEmpty()) {
            Glide.with(pending_request_activity) //1
                    .load(user_photo_url)
                    .placeholder(R.drawable.user_avatar)
                    .error(R.drawable.user_avatar)
                    .skipMemoryCache(true) //2
                    .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                    .transform(CircleCrop()) //4
                    .into(holder.user_image_view)
        }


        if(pending_request_activity.user_type == "business"){
            holder.button.text = "accept as trainee"
        }else{
            holder.button.text = "accept as trainer"
        }

        holder.button.setOnClickListener {
            //add viewed user to customers
            val cutomer = hashMapOf(
                "customer_name" to user_name_value,
                "customer_photo_url" to user_photo_url,
                "customer_id" to user_id
            )
            firestore = FirebaseFirestore.getInstance()
            current_user_id = FirebaseAuth.getInstance().currentUser?.uid
            firestore.collection("business_users").document(current_user_id!!).collection("customers").document(user_id!!).set(cutomer)

            //delete it from pending request
            firestore.collection("business_users").document(current_user_id!!).collection("requests").document(user_id!!).delete()
        }

    }

    override fun onDataChanged() {
        super.onDataChanged()
        if (itemCount <= 0) {
            pending_request_activity.placeholder.visibility = View.VISIBLE
        } else {
            pending_request_activity.placeholder.visibility = View.GONE
        }
    }
}