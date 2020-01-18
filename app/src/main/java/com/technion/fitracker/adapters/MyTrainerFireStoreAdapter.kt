package com.technion.fitracker.adapters

import android.content.ContextWrapper
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
import androidx.core.view.drawToBitmap
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
import com.technion.fitracker.R
import com.technion.fitracker.UserLandingPageActivity
import com.technion.fitracker.models.PersonalTrainer
import com.technion.fitracker.user.personal.HomeScreenFragment
import java.io.*

class MyTrainerFireStoreAdapter(
    options: FirestoreRecyclerOptions<PersonalTrainer>,
    private val homeScreenFragment: HomeScreenFragment
) :
        FirestoreRecyclerAdapter<PersonalTrainer, MyTrainerFireStoreAdapter.ViewHolder>(options) {

    inner class ViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {

        var trainerName: TextView = view.findViewById(R.id.home_screen_personal_trainer_name)
        var trainerPhoto: ImageView = view.findViewById(R.id.home_screen_personal_trainer_image_view)
        var whatsapp_image: ImageView = view.findViewById(R.id.home_screen_personal_trainer_whatsapp)
        var phone_image: ImageView = view.findViewById(R.id.home_screen_personal_trainer_phone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.element_personal_trainer, parent, false)
        return ViewHolder(view)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        if (itemCount == 0) {
            homeScreenFragment.personalTrainerContainer.visibility = View.GONE
        } else {
            homeScreenFragment.personalTrainerContainer.visibility = View.VISIBLE
        }
        homeScreenFragment.setPlaceholder()
    }

    override fun onBindViewHolder(holder: ViewHolder, p1: Int, p2: PersonalTrainer) {
        holder.trainerName.text = p2.name
        val imagePath = File(homeScreenFragment.activity?.filesDir, "/")
        val imageUserPath = File(imagePath, p2.uid!!)
        if(!imageUserPath.exists()){
            imageUserPath.mkdir()
        }
        val imageFile = File(imageUserPath, "profile_picture.jpg")
        if (imageFile.exists() && checkPictureURL(p2.uid!!, p2.photoURL!!)) {
            Glide.with(homeScreenFragment.activity!!).load(imageFile.path).placeholder(R.drawable.user_avatar)
                    .error(R.drawable.user_avatar)
                    .skipMemoryCache(true) //2
                    .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                    .transform(CircleCrop()) //4
                    .into(holder.trainerPhoto)
        } else {
            Glide.with(homeScreenFragment.activity!!) //1
                    .load(p2.photoURL)
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
                            saveProfilePicture(resource?.toBitmap()!!, p2.uid!!,p2.photoURL)
                            holder.trainerPhoto.setImageDrawable(resource)
                            return true
                        }

                    })
                    .into(holder.trainerPhoto)
        }

        if (p2.phone_number != null) {
            holder.whatsapp_image.setOnClickListener {
                try {
                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=" + p2.phone_number + "&text=")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    holder.itemView.context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(homeScreenFragment.context, "Whatsapp not installed on this device.", Toast.LENGTH_LONG).show()
                }
            }

            holder.phone_image.setOnClickListener {
                val uri = "tel:" + p2.phone_number
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse(uri)
                holder.itemView.context.startActivity(intent)
            }
        } else {
            holder.whatsapp_image.visibility = View.GONE
            holder.phone_image.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {

            val trainer_landing_page = Intent(holder.itemView.context, UserLandingPageActivity::class.java)
            //sending next value to make the activity not show button :
            trainer_landing_page.putExtra("phone_number", p2.phone_number)
            trainer_landing_page.putExtra("landing_info", p2.landing_info)
            trainer_landing_page.putExtra("user_name", p2.name)
            trainer_landing_page.putExtra("photo_url", p2.photoURL)
            trainer_landing_page.putExtra("uid", p2.uid)


            trainer_landing_page.putExtra("current_user_type", "regular")
            //Sending trainers uid because we know its already our trainer
            trainer_landing_page.putExtra("current_user_personal_trainer_uid", p2.uid)
            trainer_landing_page.putExtra("type", "business")
            holder.itemView.context.startActivity(trainer_landing_page)

        }


    }

    private fun checkPictureURL(uid:String, photoURL: String):Boolean {
        val imagePath = File(homeScreenFragment.activity?.filesDir, "/")
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
        val imagePath = File(homeScreenFragment.activity?.filesDir, "/")
        val imageUserPath = File(imagePath, uid)!!
        if(!imageUserPath.exists()){
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
        } catch (e: IOException){ // Catch the exception
            e.printStackTrace()
        }

    }
}