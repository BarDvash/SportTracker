package com.technion.fitracker.adapters.schedule

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.technion.fitracker.R
import com.technion.fitracker.adapters.AppointmentsSpinnerAdapter
import com.technion.fitracker.models.AppointmentModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FirebaseScheduleAdapter(
    options: FirestoreRecyclerOptions<AppointmentModel>,
    private val traineesNames: ArrayList<String>,
    private val traineesIds: ArrayList<String>,
    private val traineesPhotos: ArrayList<String>,
    private val listener: View.OnClickListener
) :
        FirestoreRecyclerAdapter<AppointmentModel, FirebaseScheduleAdapter.ViewHolder>(options) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var context: Context
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy")
    private val dateCalendarFormat = SimpleDateFormat("yyyy MM dd")


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var customerName: TextView = view.findViewById(R.id.element_schedule_name)
        var date: TextView = view.findViewById(R.id.element_schedule_date)
        var customerImageView: ImageView = view.findViewById(R.id.element_schedule_imageView)
        var customerId: String? = null
        var notes: String? = null
        init {
            view.tag = this
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.element_schedule_trainee, parent, false)
        view.setOnClickListener(listener)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        item: AppointmentModel
    ) {
        val dateString = dateFormat.format(dateCalendarFormat.parse(item.appointment_date!!)!!)
        val timeString = item.appointment_time!!.replace(" ", ":")
        holder.date.text = "$dateString at $timeString"
        holder.customerId = item.customer_id
        holder.notes = item.notes
        db.collection("regular_users").document(item.customer_id!!).get(Source.CACHE).addOnCompleteListener {
            if (it.result != null) {
                initNameAndImage(holder, it.result!!)
                db.collection("regular_users").document(item.customer_id!!).get().addOnSuccessListener { innerIt ->
                    initNameAndImage(holder, innerIt)
                }
            } else {
                db.collection("regular_users").document(item.customer_id!!).get().addOnSuccessListener { innerIt ->
                    initNameAndImage(holder, innerIt)
                }
            }
        }
    }

    private fun initNameAndImage(
        holder: ViewHolder,
        it: DocumentSnapshot
    ) {
        holder.customerName.text = it.getString("name")
        Glide.with(context) //1
                .load(it.getString("photoURL"))
                .placeholder(R.drawable.user_avatar)
                .error(R.drawable.user_avatar)
                .skipMemoryCache(false) //2
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) //3
                .transform(CircleCrop()) //4
                .into(holder.customerImageView)

    }


}