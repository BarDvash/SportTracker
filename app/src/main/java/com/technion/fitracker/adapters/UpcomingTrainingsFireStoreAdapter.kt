package com.technion.fitracker.adapters


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.fitracker.R
import com.technion.fitracker.models.AppointmentCancelationModel
import com.technion.fitracker.models.UpcomingTrainingFireStoreModel
import com.technion.fitracker.user.business.HomeScreenFragment
import java.io.*
import java.text.DateFormatSymbols

class UpcomingTrainingsFireStoreAdapter(
    options: FirestoreRecyclerOptions<UpcomingTrainingFireStoreModel>,
    private val context: Context,
    private val fragment: Fragment
) : FirestoreRecyclerAdapter<UpcomingTrainingFireStoreModel, UpcomingTrainingsFireStoreAdapter.ViewHolder>(options) {


    inner class ViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
        var date: TextView = view.findViewById(R.id.element_schedule_date)
        var name: TextView = view.findViewById(R.id.element_schedule_name)
        var image: ImageView = view.findViewById(R.id.element_schedule_imageView)
        var divider: View = view.findViewById(R.id.user_upcoming_divider)
        var notes: TextView = view.findViewById(R.id.notes)
        var notes_container: LinearLayout = view.findViewById(R.id.notes_container)
        var whatsapp_image: ImageView = view.findViewById(R.id.element_schedule_whatsapp)
        var phone_image: ImageView = view.findViewById(R.id.element_schedule_phone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.element_home_upcoming_training, parent, false)
        return ViewHolder(view)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        when (fragment) {
            is HomeScreenFragment -> {
                if (itemCount == 0) {
                    fragment.trainings_content_view.visibility = View.GONE
                } else {
                    fragment.trainings_content_view.visibility = View.VISIBLE
                }
                fragment.setPlaceholder()
            }
            is com.technion.fitracker.user.personal.HomeScreenFragment -> {
                if (itemCount == 0) {
                    fragment.upcoming_container.visibility = View.GONE
                } else {
                    fragment.upcoming_container.visibility = View.VISIBLE
                }
                fragment.setPlaceholder()
            }

        }

    }


    override fun onBindViewHolder(holder: ViewHolder, p1: Int, fModel: UpcomingTrainingFireStoreModel) {

        when (fragment) {
            is HomeScreenFragment -> {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("regular_users").document(fModel.customer_id!!).get().addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val phone: String? = doc.get("phone_number") as String?
                        val picture_url: String? = doc.get("photoURL") as String?

                        val imagePath = File(fragment.activity?.filesDir, "/")
                        val imageUserPath = File(imagePath, fModel.customer_id!!)
                        if (!imageUserPath.exists()) {
                            imageUserPath.mkdir()
                        }
                        val imageFile = File(imageUserPath, "profile_picture.jpg")
                        if (imageFile.exists() && checkPictureURL(fModel.customer_id!!, picture_url!!)) {
                            Glide.with(context).load(imageFile.path).placeholder(R.drawable.user_avatar)
                                    .error(R.drawable.user_avatar)
                                    .skipMemoryCache(true) //2
                                    .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                                    .transform(CircleCrop()) //4
                                    .into(holder.image)
                        } else {
                            Glide.with(context) //1
                                    .load(picture_url)
                                    .placeholder(R.drawable.user_avatar)
                                    .error(R.drawable.user_avatar)
                                    .skipMemoryCache(false) //2
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) //3
                                    .transform(CircleCrop()) //4\
                                    .listener(object : RequestListener<Drawable> {
                                        override fun onLoadFailed(
                                            e: GlideException?,
                                            model: Any?,
                                            target: Target<Drawable>?,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            return true
                                        }

                                        override fun onResourceReady(
                                            resource: Drawable?,
                                            model: Any?,
                                            target: Target<Drawable>?,
                                            dataSource: DataSource?,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            saveProfilePicture(resource?.toBitmap()!!, fModel.customer_id!!, picture_url!!)
                                            holder.image.setImageDrawable(resource)
                                            return true
                                        }

                                    })
                                    .into(holder.image)
                        }

                        holder.name.text = doc.get("name") as String?

                        phone?.let {
                            if (phone.length > 1) {
                                holder.whatsapp_image.visibility = View.VISIBLE
                                holder.whatsapp_image.setOnClickListener {
                                    try {
                                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=" + phone + "&text=")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        holder.itemView.context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Whatsapp not installed on this device.", Toast.LENGTH_LONG).show()
                                    }
                                }
                                holder.phone_image.visibility = View.VISIBLE
                                holder.phone_image.setOnClickListener {
                                    val uri = "tel:" + phone
                                    val intent = Intent(Intent.ACTION_DIAL)
                                    intent.data = Uri.parse(uri)
                                    holder.itemView.context.startActivity(intent)
                                }
                            }
                        }
                    }
                }
                        .addOnFailureListener {
                            Toast.makeText(fragment.context, "Lost internet connection", Toast.LENGTH_LONG)
                                    .show()
                        }//lost internet connection TODO:!


                fModel.notes?.let {
                    if (it.isNotEmpty()) {
                        holder.notes_container.visibility = View.VISIBLE
                        holder.notes.text = it
                    }
                }
                val split_date = fModel.appointment_date!!.split(" ")
                val month = DateFormatSymbols().months[split_date[1].toInt() - 1]
                holder.date.text = month + " " + split_date[2] + " at " + fModel.appointment_time!!.replace(" ", ":")
            }
            is com.technion.fitracker.user.personal.HomeScreenFragment -> {
                holder.phone_image.visibility = View.GONE
                holder.whatsapp_image.visibility = View.GONE
                holder.image.visibility = View.GONE
                holder.name.visibility = View.GONE
                fModel.notes?.let {
                    if (it.isNotEmpty()) {
                        holder.notes_container.visibility = View.VISIBLE
                        holder.notes.text = it
                    }
                }
                val split_date = fModel.appointment_date!!.split(" ")
                val month = DateFormatSymbols().months[split_date[1].toInt() - 1]
                holder.date.text = month + " " + split_date[2] + " at " + fModel.appointment_time!!.replace(" ", ":")
                holder.date.textSize = 18.0F
                holder.divider.visibility = View.VISIBLE
                holder.itemView.setOnLongClickListener {
                    val alertDialog: AlertDialog? = fragment.let { itHome ->
                        val builder = AlertDialog.Builder(itHome.context)
                        builder.apply {
                            setPositiveButton(R.string.yes, DialogInterface.OnClickListener { dialog, id ->
                                val personalTrainerUID =
                                    (fragment as com.technion.fitracker.user.personal.HomeScreenFragment).viewModel.personalTrainerUID!!
                                val notificationPackage = AppointmentCancelationModel(FirebaseAuth.getInstance().currentUser?.uid!!,
                                                                                      fModel.appointment_date,
                                                                                      fModel.appointment_time)
                                FirebaseFirestore.getInstance().collection("business_users").document(personalTrainerUID)
                                        .collection("appointment_cancellations").add(notificationPackage).addOnSuccessListener {  }
                                        .addOnFailureListener {  }

                            })
                            setMessage(R.string.cancel_appointment)
                            setTitle(R.string.cancel_appointment_title)
                        }
                        // Create the AlertDialog
                        builder.create()
                    }
                    alertDialog?.show()
                    true
                }
            }
        }

    }

    private fun checkPictureURL(uid: String, photoURL: String): Boolean {
        fragment.activity?.apply {
            val imagePath = File(filesDir, "/")
            val imageUserPath = File(imagePath, uid)!!
            if (!imageUserPath.exists()) {
                imageUserPath.mkdir()
            }
            val urlName = File(imageUserPath, "picture_url.txt")
            return try {
                FileInputStream(urlName).readBytes().contentEquals(photoURL.toByteArray())
            } catch (e: Throwable) {
                false
            }
        }
        return false
    }

    private fun saveProfilePicture(bitmap: Bitmap, uid: String, photoURL: String?) {
        // Initializing a new file
        // The bellow line return a directory in internal storage
        fragment.activity?.apply {
            val imagePath = File(filesDir, "/")
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

}