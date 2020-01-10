package com.technion.fitracker.adapters


import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.fitracker.R
import com.technion.fitracker.models.UpcomingTrainingFireStoreModel
import com.technion.fitracker.user.business.HomeScreenFragment
import java.text.SimpleDateFormat

class UpcomingTrainingsFireStoreAdapter(
    options: FirestoreRecyclerOptions<UpcomingTrainingFireStoreModel>,
    private val fragment: Fragment
) : FirestoreRecyclerAdapter<UpcomingTrainingFireStoreModel, UpcomingTrainingsFireStoreAdapter.ViewHolder>(options) {


    inner class ViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
        var date: TextView = view.findViewById(R.id.element_schedule_date)
        var name: TextView = view.findViewById(R.id.element_schedule_name)
        var image: ImageView = view.findViewById(R.id.element_schedule_imageView)
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

        }

    }


    override fun onBindViewHolder(holder: ViewHolder, p1: Int, model: UpcomingTrainingFireStoreModel) {

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("regular_users").document(model.customer_id!!).get().addOnSuccessListener {
            if (it.exists()) {
                val phone: String? = it.get("phone") as String?
                val picture_url: String? = it.get("photoURL") as String?
                holder.name.text = it.get("name") as String?
                Glide.with(fragment) //1
                        .load(picture_url)
                        .placeholder(R.drawable.user_avatar)
                        .error(R.drawable.user_avatar)
                        .skipMemoryCache(true) //2
                        .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                        .transform(CircleCrop()) //4
                        .into(holder.image)
                if (phone != null) {
                    holder.whatsapp_image.setOnClickListener {
                        try {
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=" + phone + "&text=")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            holder.itemView.context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(fragment.context, "Whatsapp not installed on this device.", Toast.LENGTH_LONG).show()
                        }
                    }

                    holder.phone_image.setOnClickListener {
                        val uri = "tel:" + phone
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse(uri)
                        holder.itemView.context.startActivity(intent)
                    }
                } else {
                    holder.whatsapp_image.visibility = View.GONE
                    holder.phone_image.visibility = View.GONE
                }
            }
        }
                .addOnFailureListener {
                    Toast.makeText(fragment.context, "Lost internet connection", Toast.LENGTH_LONG)
                            .show()
                }//lost internet connection TODO:!


        model.notes?.let {
            holder.notes_container.visibility = View.VISIBLE
            holder.notes.text = it
        }
        holder.date.text = SimpleDateFormat("dd MMMM yyyy 'at' hh:mm").format(model.appointment_date)


    }


}