package com.technion.fitracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserLandingPageActivity : AppCompatActivity() {

    //declaration of the activity instance variables:
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var viewed_user_name: String? = null
    private var viewed_user_photo_url: String? = null
    private var viewed_user_id: String? = null
    private var viewed_user_type: String? = null

    private var current_user_type: String? = null
    private var current_user_id: String? = null
    private var current_user_name: String? = null
    private var current_user_phone_number: String? = null
    private var current_user_photo_url: String? = null

    private lateinit var name_view: TextView
    private lateinit var image_view: ImageView
    private lateinit var add_button: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_landing_page)



        setSupportActionBar(findViewById(R.id.user_toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        //initialize instance variables:
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        var bundle: Bundle? = intent.extras
        viewed_user_name = bundle!!.getString("user_name")
        viewed_user_photo_url = bundle.getString("photo_url")
        viewed_user_id = bundle.getString("uid")
        viewed_user_type = bundle.getString("type")



        current_user_type = bundle.getString("current_user_type")
        current_user_name = bundle.getString("current_user_name")
        current_user_photo_url = bundle.getString("current_user_photo_url")
        current_user_phone_number = bundle.getString("current_user_phone_number")
        current_user_id = FirebaseAuth.getInstance().currentUser?.uid



        name_view = findViewById(R.id.search_result_landing_page_user_name)
        name_view.text = viewed_user_name
        image_view = findViewById(R.id.search_result_landing_page_user_avatar)
        if (!viewed_user_photo_url.isNullOrEmpty()) {
            Glide.with(this) //1
                    .load(viewed_user_photo_url)
                    .placeholder(R.drawable.user_avatar)
                    .error(R.drawable.user_avatar)
                    .skipMemoryCache(true) //2
                    .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                    .transform(CircleCrop()) //4
                    .into(image_view)
        }



        add_button = findViewById(R.id.add_as_button)
        add_button.isEnabled = false
        if(current_user_type == "regular" && viewed_user_type == "business"){
            add_button.visibility = View.VISIBLE
            //get document
            val user_doc = firestore.collection("business_users").document(viewed_user_id!!).collection("requests").document(current_user_id!!)
            user_doc.get().addOnSuccessListener { if(it.exists()){add_button.text = "cancel request"}else{add_button.text = "Add as trainer"}}

        }else if(current_user_type == "business" && viewed_user_type == "regular"){
            add_button.visibility = View.VISIBLE

            val user_doc = firestore.collection("regular_users").document(viewed_user_id!!).collection("requests").document(current_user_id!!)
            user_doc.get().addOnSuccessListener { if(it.exists()){add_button.text = "cancel request"}else{add_button.text = "Add as trainee"} }
        }
        add_button.isEnabled = true

    }




    //TODO show a toast if customer already in  the list
    fun addAs(view: View) {

        if (add_button.text == "cancel request") {
            if (current_user_type == "regular" && viewed_user_type == "business") { //when user send request to trainer to be his personal trainer
                firestore.collection("business_users").document(viewed_user_id!!).collection("requests").document(current_user_id!!).delete()
                add_button.text = "Add as trainer"
            } else if (viewed_user_type == "regular" && current_user_type == "business") {//when trainer send request to user to be his personal trainer
                firestore.collection("regular_users").document(viewed_user_id!!).collection("requests").document(current_user_id!!).delete()
                add_button.text = "Add as trainee"
            }
            Toast.makeText(this, "canceled request", Toast.LENGTH_LONG).show()


        } else {

            val user = hashMapOf(
                "user_name" to current_user_name,
                "user_photo_url" to current_user_photo_url,
                "user_id" to current_user_id,
                "user_phone_number" to current_user_phone_number
            )

            if (current_user_type == "regular" && viewed_user_type == "business") { //when user send request to trainer to be his personal trainer
                firestore.collection("business_users").document(viewed_user_id!!).collection("requests").document(current_user_id!!).set(user)
                Toast.makeText(this, "sent request to be " + viewed_user_name + "'s trainee", Toast.LENGTH_LONG).show()
                //TODO: change button here ! and make sure the button stays changed by defined it somwhere


            } else if (viewed_user_type == "regular" && current_user_type == "business") {//when trainer send request to user to be his personal trainer
                firestore.collection("regular_users").document(viewed_user_id!!).collection("requests").document(current_user_id!!).set(user)
                Toast.makeText(this, "sent request to be " + viewed_user_name + "'s personal trainer", Toast.LENGTH_LONG).show()
                //TODO: change button here ! and make sure the button stays changed by defined it somwhere
            }

            add_button.text = "cancel request"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }


}

