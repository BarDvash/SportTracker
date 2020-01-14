package com.technion.fitracker.adapters

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.technion.fitracker.models.CustomersFireStoreModel
import com.technion.fitracker.user.business.CustomersFragment
import java.io.*


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
        var whatsapp_image: ImageView = view.findViewById(com.technion.fitracker.R.id.customer_whatsapp_icon)
        var phone_image: ImageView = view.findViewById(com.technion.fitracker.R.id.customer_phone_icon)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(com.technion.fitracker.R.layout.element_customer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        fModel: CustomersFireStoreModel
    ) {
        holder.customer_name.text = fModel.customer_name
        if (!fModel.customer_photo_url.isNullOrEmpty()) {
            val imagePath = File(customersFragment.activity?.filesDir, "/")
            val imageUserPath = File(imagePath, fModel.customer_id!!)
            if(!imagePath.exists()){
                imagePath.mkdir()
            }
            val imageFile = File(imageUserPath, "profile_picture.jpg")
            if (imageFile.exists() && checkPictureURL(fModel.customer_id!!, fModel.customer_photo_url!!)) {
                Glide.with(customersFragment).load(imageFile.path).placeholder(R.drawable.user_avatar)
                        .error(R.drawable.user_avatar)
                        .skipMemoryCache(true) //2
                        .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                        .transform(CircleCrop()) //4
                        .into(holder.customer_image_view)
            } else {
                Glide.with(customersFragment) //1
                        .load(fModel.customer_photo_url)
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
                                saveProfilePicture(resource?.toBitmap()!!, fModel.customer_id!!, fModel.customer_photo_url)
                                holder.customer_image_view.setImageDrawable(resource)
                                return true
                            }

                        })
                        .into(holder.customer_image_view)
            }
        }

        holder.whatsapp_image.setOnClickListener {
            if (fModel.customer_phone_number != null) {
                try {
                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=" + fModel.customer_phone_number + "&text=")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    holder.itemView.context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(customersFragment.context, "Whatsapp not installed in this device.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(customersFragment.context, fModel.customer_name + " didn't provide phone number", Toast.LENGTH_LONG).show()
            }
        }

        holder.phone_image.setOnClickListener {
            if (fModel.customer_phone_number != null) {
                val uri = "tel:" + fModel.customer_phone_number
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse(uri)
                holder.itemView.context.startActivity(intent)
            } else {
                Toast.makeText(customersFragment.context, fModel.customer_name + " didn't provide phone number", Toast.LENGTH_LONG).show()
            }
        }

        holder.itemView.setOnLongClickListener {
            val alertDialog: AlertDialog? = customersFragment.let {
                val builder = AlertDialog.Builder(it.context)
                builder.apply {
                    setPositiveButton(R.string.remove, DialogInterface.OnClickListener { dialog, id ->
                        FirebaseFirestore.getInstance().collection("business_users").document(FirebaseAuth.getInstance().currentUser?.uid!!)
                                .collection("customers").document(fModel.customer_id!!).delete()
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

    private fun checkPictureURL(uid:String, photoURL: String):Boolean {
        val imagePath = File(customersFragment.activity?.filesDir, "/")
        val imageUserPath = File(imagePath, uid)!!
        if(!imagePath.exists()){
            imagePath.mkdir()
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
        val imagePath = File(customersFragment.activity?.filesDir, "/")
        val imageUserPath = File(imagePath, uid)!!
        if (!imagePath.exists()) {
            imagePath.mkdir()
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

    override fun onDataChanged() {
        super.onDataChanged()
        if (itemCount <= 0) {
            customersFragment.placeholder.visibility = View.VISIBLE
        } else {
            customersFragment.placeholder.visibility = View.GONE
        }
    }
}