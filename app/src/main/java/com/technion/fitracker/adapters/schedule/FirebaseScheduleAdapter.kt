package com.technion.fitracker.adapters.schedule

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.technion.fitracker.R
import com.technion.fitracker.models.schedule.AppointmentModel
import java.io.*
import java.text.SimpleDateFormat

class FirebaseScheduleAdapter(
    options: FirestoreRecyclerOptions<AppointmentModel>,
    private val listener: View.OnClickListener,
    private val fragment: Fragment
) :
        FirestoreRecyclerAdapter<AppointmentModel, FirebaseScheduleAdapter.ViewHolder>(options) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var context: Context
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy")
    private val dateCalendarFormat = SimpleDateFormat("yyyy MM dd")


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var customerName: TextView = view.findViewById(R.id.element_schedule_name)
        var whatsappImage: ImageView = view.findViewById(R.id.element_schedule_whatsapp)
        var phoneImage: ImageView = view.findViewById(R.id.element_schedule_phone)
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
        db.collection("regular_users").document(item.customer_id!!).get(Source.CACHE).addOnSuccessListener {
                initCustomerInfo(holder, it,item.customer_id!!)
                db.collection("regular_users").document(item.customer_id!!).get().addOnSuccessListener { innerIt ->
                    initCustomerInfo(holder, innerIt, item.customer_id!!)
            }.addOnFailureListener{
                db.collection("regular_users").document(item.customer_id!!).get().addOnSuccessListener { innerIt ->
                    initCustomerInfo(holder, innerIt, item.customer_id!!)
                }
            }
        }
    }

    private fun initCustomerInfo(
        holder: ViewHolder,
        it: DocumentSnapshot,
        uid:String
    ) {
        var photoURL: String? = it.getString("photoURL")
        var phone = it.getString("phone_number")
        if(photoURL != null){
            holder.customerName.text = it.getString("name")
            val imagePath = File(fragment.activity?.filesDir, "/")
            val imageUserPath = File(imagePath, uid)
            if(!imageUserPath.exists()){
                imageUserPath.mkdir()
            }
            val imageFile = File(imageUserPath, "profile_picture.jpg")
            if (imageFile.exists() && checkPictureURL(uid, photoURL)) {
                Glide.with(context).load(imageFile.path).placeholder(R.drawable.user_avatar)
                        .error(R.drawable.user_avatar)
                        .skipMemoryCache(true) //2
                        .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                        .transform(CircleCrop()) //4
                        .into(holder.customerImageView)
            } else {
                Glide.with(context) //1
                        .load(photoURL)
                        .placeholder(R.drawable.user_avatar)
                        .error(R.drawable.user_avatar)
                        .skipMemoryCache(false) //2
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) //3
                        .transform(CircleCrop()) //4\
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                Log.d("GLIDE-ERROR", "Failed to load image")
                                return true
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.d("GLIDE-LOAD", "Loaded profile picture!")
                                saveProfilePicture(resource?.toBitmap()!!, uid, photoURL)
                                holder.customerImageView.setImageDrawable(resource)
                                return true
                            }

                        })
                        .into(holder.customerImageView)
            }
        }
        phone?.let {
            if (phone.length > 1) {
                holder.whatsappImage.visibility = View.VISIBLE
                holder.whatsappImage.setOnClickListener {
                    try {
                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=" + phone + "&text=")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        holder.itemView.context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this.context, "Whatsapp not installed on this device.", Toast.LENGTH_LONG).show()
                    }
                }
                holder.phoneImage.visibility = View.VISIBLE
                holder.phoneImage.setOnClickListener {
                    val uri = "tel:" + phone
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse(uri)
                    holder.itemView.context.startActivity(intent)
                }
            }
        }

    }

    private fun checkPictureURL(uid:String, photoURL: String):Boolean {
        val imagePath = File(fragment.activity?.filesDir, "/")
        val imageUserPath = File(imagePath, uid)!!
        if(!imageUserPath.exists()){
            imageUserPath.mkdir()
        }
        val urlName = File(imageUserPath, "picture_url.txt")
        return try{
            FileInputStream(urlName).readBytes().contentEquals(photoURL.toByteArray())
        }catch (e: Throwable){
            false
        }
    }

    private fun saveProfilePicture(bitmap: Bitmap, uid: String, photoURL: String?) {
        // Initializing a new file
        // The bellow line return a directory in internal storage
        val imagePath = File(fragment.activity?.filesDir, "/")
        val imageUserPath = File(imagePath, uid)!!
        if (!imageUserPath.exists()) {
            imageUserPath.mkdir()
        }
        val imageFile = File(imageUserPath, "profile_picture.jpg")
        val urlName = File(imageUserPath, "picture_url.txt")
        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(imageFile)
            val streamName: OutputStream = FileOutputStream(urlName)
            streamName.write(photoURL?.toByteArray()!!)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            streamName.flush()
            stream.flush()
            streamName.close()
            stream.close()
        } catch (e: IOException) { // Catch the exception
            e.printStackTrace()
        }
    }

}