package com.technion.fitracker.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.technion.fitracker.R

class AppointmentsSpinnerAdapter(val context: Context, val names: ArrayList<String>, val id: ArrayList<String>, val photos: ArrayList<String>) :
        BaseAdapter() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = mInflater.inflate(R.layout.element_add_apointment_trainees, parent, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        // setting adapter item height programatically.

//        val params = view.layoutParams
//        params.height = 60
//        view.layoutParams = params

        vh.name?.text = names[position]
        Glide.with(context) //1
                .load(photos[position])
                .placeholder(R.drawable.user_avatar)
                .error(R.drawable.user_avatar)
                .skipMemoryCache(false) //2
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) //3
                .transform(CircleCrop()) //4
                .into(vh.image!!)
        return view
    }

    override fun getItem(position: Int): Any {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return names.size
    }

    private class ItemRowHolder(row: View?) {

        val name: TextView? = row?.findViewById(R.id.add_apointment_name)
        val image: ImageView? = row?.findViewById(R.id.add_apointment_image)

    }
}