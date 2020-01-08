package com.technion.fitracker.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.fitracker.models.CustomersFireStoreModel
import com.technion.fitracker.user.business.CustomersFragment
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.lang.Exception
import androidx.core.content.ContextCompat.startActivity
import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_NEUTRAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.fitracker.R


class CustomersFireStoreAdapter(options: FirestoreRecyclerOptions<CustomersFireStoreModel>, val customersFragment: CustomersFragment) :
        FirestoreRecyclerAdapter<CustomersFireStoreModel, CustomersFireStoreAdapter.ViewHolder>(options) {

    var mOnItemClickListener: View.OnClickListener? = null



    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.tag = this
            view.setOnClickListener(mOnItemClickListener)
        }
        var customer_name: TextView = view.findViewById(com.technion.fitracker.R.id.customer_name)
        var customer_image_view: ImageView = view.findViewById(com.technion.fitracker.R.id.customer_imageView)
        var whatsapp_image: ImageView  = view.findViewById(com.technion.fitracker.R.id.customer_whatsapp_icon)
        var phone_image: ImageView = view.findViewById(com.technion.fitracker.R.id.customer_phone_icon)
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(com.technion.fitracker.R.layout.element_customer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        item: CustomersFireStoreModel
    ) {
        holder.customer_name.text = item.customer_name
        if (!item?.customer_photo_url.isNullOrEmpty()) {
            Glide.with(customersFragment) //1
                    .load(item?.customer_photo_url)
                    .placeholder(com.technion.fitracker.R.drawable.user_avatar)
                    .error(com.technion.fitracker.R.drawable.user_avatar)
                    .skipMemoryCache(true) //2
                    .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                    .transform(CircleCrop()) //4
                    .into(holder.customer_image_view)
        }

        holder.whatsapp_image.setOnClickListener {
            if (item.customer_phone_number != null) {
                try{
                    val uri = Uri.parse("https://api.whatsapp.com/send?phone="+item.customer_phone_number+"&text=")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    holder.itemView.context.startActivity(intent)
                }catch (e: Exception){
                    Toast.makeText(customersFragment.context, "Whatsapp not installed in this device.", Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(customersFragment.context, item.customer_name +" didn't provide phone number", Toast.LENGTH_LONG).show()
            }
        }

        holder.phone_image.setOnClickListener{
            if (item.customer_phone_number != null) {
                val uri = "tel:" + item.customer_phone_number
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse(uri)
                holder.itemView.context.startActivity(intent)
            }else{
                Toast.makeText(customersFragment.context, item.customer_name +" didn't provide phone number", Toast.LENGTH_LONG).show()
            }
        }

        holder.itemView.setOnLongClickListener{
            val alertDialog: AlertDialog? = customersFragment?.let {
                val builder = AlertDialog.Builder(it.context)
                builder.apply {
                    setPositiveButton(R.string.remove, DialogInterface.OnClickListener { dialog, id ->
                        FirebaseFirestore.getInstance().collection("business_users").document(FirebaseAuth.getInstance().currentUser?.uid!!).collection("customers").document(item.customer_id!!).delete() })
                    setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
                                          // User cancelled the dialog
                                      })

                    setMessage(R.string.dialog_remove_trainee_message)
                    setTitle(R.string.dialog_remove_trainee_title)
                }
                // Create the AlertDialog
                builder.create()
            }
            alertDialog?.show()
            true
        }
    }

    override fun onDataChanged() {
        super.onDataChanged()
        if (itemCount <= 0) {
            customersFragment.placeholder.visibility = View.VISIBLE
        } else {
            customersFragment.placeholder.visibility = View.GONE
        }
    }
}