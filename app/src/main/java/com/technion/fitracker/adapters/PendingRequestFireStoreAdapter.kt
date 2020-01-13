package com.technion.fitracker.adapters


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


class PendingRequestFireStoreAdapter(
    options: FirestoreRecyclerOptions<PendingRequestFireStoreModel>,
    val pending_request_activity: PendingRequestsActivity
) :
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
        var accept_button: Button = view.findViewById(R.id.accept_pending_request_button)
        var decline_button: Button = view.findViewById(R.id.decline_pending_request_button)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.element_pending_request, parent, false)
        firestore = FirebaseFirestore.getInstance()
        current_user_id = FirebaseAuth.getInstance().currentUser?.uid
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        item: PendingRequestFireStoreModel
    ) {
        val user_name_value = item.user_name
        val user_photo_url = item.user_photo_url
        val user_phone_number = item.user_phone_number
        val user_id = item.user_id

        holder.user_name.text = user_name_value
        if (!item.user_photo_url.isNullOrEmpty()) {
            Glide.with(pending_request_activity) //1
                    .load(user_photo_url)
                    .placeholder(R.drawable.user_avatar)
                    .error(R.drawable.user_avatar)
                    .skipMemoryCache(true) //2
                    .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                    .transform(CircleCrop()) //4
                    .into(holder.user_image_view)
        }


        holder.accept_button.setOnClickListener {


            if (pending_request_activity.user_type == "business") {
                //add viewed user to customers
                val cutomer = hashMapOf(
                        "customer_name" to user_name_value,
                        "customer_photo_url" to user_photo_url,
                        "customer_id" to user_id,
                        "customer_phone_number" to user_phone_number
                )

                //for cloud functions:
                firestore.collection("regular_users").document(user_id!!).collection("approved_requests").document(current_user_id!!)
                        .set(hashMapOf("id" to current_user_id))
                //until here

                firestore.collection("business_users").document(current_user_id!!).collection("customers").document(user_id).set(cutomer)

                //delete it from pending request
                firestore.collection("business_users").document(current_user_id!!).collection("requests").document(user_id).delete()

                //TODO: maybe we want to move it to cloud functions:
                //change the customer personal trainer to be the current business user
                firestore.collection("regular_users").document(user_id).update("personal_trainer_uid", current_user_id)
            } else {//if it's regular user

                val cutomer = hashMapOf(
                        "customer_name" to pending_request_activity.user_name,
                        "customer_photo_url" to pending_request_activity.user_photo_url,
                        "customer_id" to current_user_id
                )


                //for cloud functions:
                firestore.collection("business_users").document(user_id!!).collection("approved_requests").document(current_user_id!!)
                        .set(hashMapOf("id" to current_user_id))
                //until here


                firestore.collection("business_users").document(user_id).collection("customers").document(current_user_id!!).set(cutomer)

                //delete it from pending request
                firestore.collection("regular_users").document(current_user_id!!).collection("requests").document(user_id).delete()


                //change the current  user's personal trainer to be the one he accepted his invitation:
                firestore.collection("regular_users").document(current_user_id!!).update("personal_trainer_uid", user_id)
            }
        }



        holder.decline_button.setOnClickListener {
            if (pending_request_activity.user_type == "business") {
                //delete it from pending request
                firestore.collection("business_users").document(current_user_id!!).collection("requests").document(user_id!!).delete()
            } else {//if it's regular user
                //delete it from pending request
                firestore.collection("regular_users").document(current_user_id!!).collection("requests").document(user_id!!).delete()
            }
        }
    }


    override fun onDataChanged() {
        super.onDataChanged()




        if (itemCount <= 0) {

            firestore = FirebaseFirestore.getInstance()
            current_user_id = FirebaseAuth.getInstance().currentUser?.uid
            if (pending_request_activity.user_type == "business") {
                var user_doc =
                    firestore.collection("business_users").document(current_user_id!!).collection("notifications").document("pending_requests")
                user_doc.get().addOnSuccessListener {
                    if (it.exists()) {
                        firestore.collection("business_users").document(current_user_id!!).collection("notifications").document("pending_requests")
                                .delete()
                    }
                }

            } else {
                var user_doc =
                    firestore.collection("regular_users").document(current_user_id!!).collection("notifications").document("pending_requests")
                user_doc.get().addOnSuccessListener {
                    if (it.exists()) {
                        firestore.collection("regular_users").document(current_user_id!!).collection("notifications").document("pending_requests")
                                .delete()
                    }
                }
            }



            pending_request_activity.placeholder.visibility = View.VISIBLE
        } else {
            pending_request_activity.placeholder.visibility = View.GONE
        }
    }
}