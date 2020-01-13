package com.technion.fitracker.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.fitracker.R
import com.technion.fitracker.UserLandingPageActivity
import com.technion.fitracker.models.PersonalTrainer
import com.technion.fitracker.user.personal.HomeScreenFragment

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
        Glide.with(homeScreenFragment.activity!!) //1
                .load(p2.photoURL)
                .placeholder(R.drawable.user_avatar)
                .error(R.drawable.user_avatar)
                .skipMemoryCache(false) //2
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) //3
                .transform(CircleCrop()) //4
                .into(holder.trainerPhoto)

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
}