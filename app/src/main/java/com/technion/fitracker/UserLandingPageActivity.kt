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
import com.technion.fitracker.user.business.CustomerData

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
    private var current_user_photo_url: String? = null

    private lateinit var name_view: TextView
    private lateinit var image_view: ImageView
    private lateinit var add_button: Button
    //////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_landing_page)
        setSupportActionBar(findViewById(R.id.search_toolbar))

        //initialize instance variables:
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        var bundle: Bundle? = intent.extras
        viewed_user_name = bundle!!.getString("user_name")
        viewed_user_photo_url = bundle.getString("photo_url")
        viewed_user_id = bundle.getString("uid")
        viewed_user_type = bundle.getString("type")

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
        /////////////////////////////////////

        //set button visibility and text, according to who is current user and who is the viewed user:
        setButton()
        //////////////////////////////////////////////////////////////////////////////////////////////
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.search_menu, menu)
        return true

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) { //check on which item the user pressed and perform the appropriate action

            R.id.search_from_searchActivity -> {
                onSearchRequested()
                true
            }

            else -> {
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                super.onOptionsItemSelected(item)
            }
        }
    }

    //TODO show a toast if customer already in  the list
    fun addAs(view: View) {

        val user = hashMapOf(
            "user_name" to current_user_name,
            "user_photo_url" to current_user_photo_url,
            "user_id" to current_user_id
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

    }


    fun setButton() {
        //TODO: best way it wil be to use viewmodel and to pass current user type to searchableActivity
        var user_doc = firestore.collection("regular_users").document(current_user_id!!)
        user_doc.get().addOnSuccessListener {
            if (it.exists()) {
                if (viewed_user_type == "business") {
                    current_user_name = it.get("name") as String?
                    current_user_photo_url = it.get("photoURL") as String?
                    current_user_type = "regular"
                    add_button.visibility = View.VISIBLE
                    add_button.text = "Add as trainer"
                }
            } else {//if we arrived here current user is business user:
                user_doc = firestore.collection("business_users").document(current_user_id!!)
                user_doc.get().addOnSuccessListener {
                    if (viewed_user_type == "regular") {
                        current_user_name = it.get("name") as String?
                        current_user_photo_url = it.get("photoURL") as String?
                        current_user_type = "business"
                        add_button.visibility = View.VISIBLE
                        add_button.text = "Add as trainee"
                    }
                }
            }
        }.addOnFailureListener { Toast.makeText(this, "Lost internet connection", Toast.LENGTH_LONG).show() }//lost internet connection TODO:!
    }
}


