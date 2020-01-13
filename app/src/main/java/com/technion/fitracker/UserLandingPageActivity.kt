package com.technion.fitracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private var viewed_user_landing_info: String? = null
    private var viewed_user_personal_trainer_uid: String? = null
    private var viewed_user_phone_number: String? = null

    private var current_user_type: String? = null
    private var current_user_id: String? = null
    private var current_user_name: String? = null
    private var current_user_phone_number: String? = null
    private var current_user_photo_url: String? = null
    private var current_user_personal_trainer_uid: String? = null

    private lateinit var name_view: TextView
    private lateinit var image_view: ImageView
    private lateinit var add_button: MaterialButton


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
        viewed_user_landing_info = bundle.getString("landing_info")
        viewed_user_personal_trainer_uid = bundle.getString("personal_trainer_uid")
        viewed_user_phone_number = bundle.getString("phone_number")
        if (viewed_user_landing_info.isNullOrEmpty()) {
            findViewById<LinearLayout>(R.id.info_container).visibility = View.GONE
        } else {
            findViewById<TextView>(R.id.info_text).text = viewed_user_landing_info
        }
        if (viewed_user_phone_number.isNullOrEmpty()) {
            findViewById<LinearLayout>(R.id.phoneContainer).visibility = View.GONE
        } else {
            findViewById<ImageView>(R.id.phone)?.apply {
                setOnClickListener {
                    val uri = "tel:" + viewed_user_phone_number
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse(uri)
                    context.startActivity(intent)
                }
            }
            findViewById<ImageView>(R.id.whatsapp)?.apply {
                setOnClickListener {
                    try {
                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=" + viewed_user_phone_number + "&text=")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Whatsapp not installed on this device.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        current_user_type = bundle.getString("current_user_type")
        current_user_name = bundle.getString("current_user_name")
        current_user_photo_url = bundle.getString("current_user_photo_url")
        current_user_phone_number = bundle.getString("current_user_phone_number")
        current_user_personal_trainer_uid = bundle.getString("current_user_personal_trainer_uid")


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
        if (current_user_type == "regular" && viewed_user_type == "business") {
            val customer_doc = firestore.collection("business_users").document(viewed_user_id!!).collection("customers").document(current_user_id!!)
            customer_doc.get().addOnSuccessListener {
                if (it.exists()) {
                    add_button.visibility = View.VISIBLE
                    if (current_user_personal_trainer_uid != viewed_user_id) {
                        add_button.isEnabled = false
                        add_button.text = getString(R.string.you_already_have_trainer)
                    } else {
                        add_button.text = getString(R.string.delete_your_trainer)
                    }
                } else {
                    add_button.visibility = View.VISIBLE
                    if (!current_user_personal_trainer_uid.isNullOrEmpty()) {
                        add_button.isEnabled = false
                        add_button.text = getString(R.string.you_already_have_trainer)
                    } else {
                        add_button.text = getString(R.string.add_as_trainer)
                    }
                }
            }
            //get document
            val user_doc = firestore.collection("business_users").document(viewed_user_id!!).collection("requests").document(current_user_id!!)
            user_doc.get().addOnSuccessListener {
                if (it.exists()) {
                    add_button.text = getString(R.string.cancel_request)
                }
            }

        } else if (current_user_type == "business" && viewed_user_type == "regular") {
            val customer_doc = firestore.collection("business_users").document(current_user_id!!).collection("customers").document(viewed_user_id!!)
            customer_doc.get().addOnSuccessListener {
                if (it.exists()) {
                    add_button.visibility = View.VISIBLE
                    add_button.text = getString(R.string.delete_trainee)
                } else {
                    if (viewed_user_personal_trainer_uid.isNullOrEmpty()) {
                        add_button.visibility = View.VISIBLE
                        add_button.text = getString(R.string.add_as_trainee)
                    } else {
                        add_button.visibility = View.VISIBLE
                        add_button.isEnabled = false
                        add_button.text = getString(R.string.already_has_trainer)
                    }
                }
            }

            val user_doc = firestore.collection("regular_users").document(viewed_user_id!!).collection("requests").document(current_user_id!!)
            user_doc.get().addOnSuccessListener {
                if (it.exists()) {
                    add_button.text = getString(R.string.cancel_request)
                }
            }
        }
        add_button.isEnabled = true

    }


    //TODO show a toast if customer already in  the list
    fun addAs(view: View) {

        if (add_button.text == getString(R.string.cancel_request)) {
            if (current_user_type == "regular" && viewed_user_type == "business") { //when user send request to trainer to be his personal trainer
                firestore.collection("business_users").document(viewed_user_id!!).collection("requests").document(current_user_id!!).delete()
                add_button.text = getString(R.string.add_as_trainer)
            } else if (viewed_user_type == "regular" && current_user_type == "business") {//when trainer send request to user to be his personal trainer
                firestore.collection("regular_users").document(viewed_user_id!!).collection("requests").document(current_user_id!!).delete()
                add_button.text = getString(R.string.add_as_trainer)
            }
            Toast.makeText(this, "canceled request", Toast.LENGTH_LONG).show()


        } else if (add_button.text == getString(R.string.delete_your_trainer)) {
            //TODO: notification!
            MaterialAlertDialogBuilder(this).setTitle("Warning").setMessage("Really remove your trainer?")
                    .setPositiveButton(
                            "Yes"
                    ) { _, _ ->
                        firestore.collection("regular_users").document(current_user_id!!).update("personal_trainer_uid", null)
                        firestore.collection("business_users").document(viewed_user_id!!).collection("customers").document(current_user_id!!).delete()
                        add_button.text = getString(R.string.add_as_trainer)
                    }
                    .setNegativeButton(
                            "No"
                    ) { _, _ ->
                    }.show()
        } else if (add_button.text == getString(R.string.delete_trainee)) {
            //TODO: notification!
            MaterialAlertDialogBuilder(this).setTitle("Warning").setMessage("Really remove " + viewed_user_name + "from your customers?")
                    .setPositiveButton(
                            "Yes"
                    ) { _, _ ->
                        firestore.collection("regular_users").document(viewed_user_id!!).update("personal_trainer_uid", null)
                        firestore.collection("business_users").document(current_user_id!!).collection("customers").document(viewed_user_id!!).delete()
                        add_button.text = getString(R.string.add_as_trainee)
                    }
                    .setNegativeButton(
                            "No"
                    ) { _, _ ->
                    }.show()

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
                //TODO: change button here ! and make sure the button stays changed by defined it somewhere


            } else if (viewed_user_type == "regular" && current_user_type == "business") {//when trainer send request to user to be his personal trainer
                firestore.collection("regular_users").document(viewed_user_id!!).collection("requests").document(current_user_id!!).set(user)
                Toast.makeText(this, "sent request to be " + viewed_user_name + "'s personal trainer", Toast.LENGTH_LONG).show()
                //TODO: change button here ! and make sure the button stays changed by defined it somewhere
            }

            add_button.text = getString(R.string.cancel_request)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }


}

