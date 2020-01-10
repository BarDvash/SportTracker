package com.technion.fitracker.adapters

import android.content.Intent
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
        var trainerPhoto: ImageView = view.findViewById(R.id.hone_screen_personal_trainer_image_view)

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
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int, p2: PersonalTrainer) {
        p0.trainerName.text = p2.name
        Glide.with(homeScreenFragment.activity!!) //1
                .load(p2.photoURL)
                .placeholder(R.drawable.user_avatar)
                .error(R.drawable.user_avatar)
                .skipMemoryCache(false) //2
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) //3
                .transform(CircleCrop()) //4
                .into(p0.trainerPhoto)




        p0.itemView.setOnClickListener {

                val trainer_landing_page = Intent(p0.itemView.context, UserLandingPageActivity::class.java)
                    trainer_landing_page.putExtra("user_type", "business")
                    //sending next value to make the activity not show button :
                    trainer_landing_page.putExtra("current_user_type", "business")
                    //
                    trainer_landing_page.putExtra("user_name", p2.name)
                    trainer_landing_page.putExtra("user_photo_url", p2.photoURL)
            p0.itemView.context.startActivity(trainer_landing_page)

        }
    }
}