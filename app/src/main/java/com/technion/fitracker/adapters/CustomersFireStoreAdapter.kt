package com.technion.fitracker.adapters

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
import com.technion.fitracker.R
import com.technion.fitracker.models.CustomersFireStoreModel
import com.technion.fitracker.user.business.CustomersFragment


class CustomersFireStoreAdapter(options: FirestoreRecyclerOptions<CustomersFireStoreModel>, val customersFragment: CustomersFragment) :
        FirestoreRecyclerAdapter<CustomersFireStoreModel, CustomersFireStoreAdapter.ViewHolder>(options) {

    var mOnItemClickListener: View.OnClickListener? = null



    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.tag = this
            view.setOnClickListener(mOnItemClickListener)
        }
        var customer_name: TextView = view.findViewById(R.id.customer_name)
        var customer_image_view: ImageView = view.findViewById(R.id.customer_imageView)
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.element_customer, parent, false)
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
                    .placeholder(R.drawable.user_avatar)
                    .error(R.drawable.user_avatar)
                    .skipMemoryCache(true) //2
                    .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                    .transform(CircleCrop()) //4
                    .into(holder.customer_image_view)
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