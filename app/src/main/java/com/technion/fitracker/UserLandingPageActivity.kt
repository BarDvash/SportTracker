package com.technion.fitracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.fitracker.user.business.CustomerData
import kotlin.reflect.KClass

class UserLandingPageActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var user_name : String? = null
    private var photo_url : String? = null
    private var viewed_user_id : String? = null

    private lateinit var name_view: TextView
    private lateinit var image_view: ImageView
    private lateinit var add_button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_landing_page)
        setSupportActionBar(findViewById(R.id.search_toolbar))
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        var bundle: Bundle? = intent.extras
        user_name = bundle!!.getString("user_name")
        photo_url = bundle!!.getString("photo_url")
        viewed_user_id = bundle!!.getString("uid")

        name_view = findViewById(R.id.search_result_landing_page_user_name)
        image_view= findViewById(R.id.search_result_landing_page_user_avatar)
        add_button= findViewById(R.id.add_as_button)


        //set button, according to who is current user and who is the viewed user:
        var current_user_id = FirebaseAuth.getInstance().currentUser?.uid
        if(isUserAppeardInRegularUsersCollection(current_user_id) && (!(isUserAppeardInRegularUsersCollection(viewed_user_id)))){
            add_button.visibility = View.VISIBLE
            add_button.text = "Add as trainer"
        }else if(isUserAppeardInRegularUsersCollection(viewed_user_id) && (!(isUserAppeardInRegularUsersCollection(current_user_id)))){
            add_button.visibility = View.VISIBLE
            add_button.text = "Add as trainee"
        }



        name_view.text = user_name

        if (!photo_url.isNullOrEmpty()) {
            //Glide.with(activity).load(item.photoURL).into(holder.user_image)

            Glide.with(this) //1
                    .load(photo_url)
                    .placeholder(R.drawable.user_avatar)
                    .error(R.drawable.user_avatar)
                    .skipMemoryCache(true) //2
                    .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                    .transform(CircleCrop()) //4
                    .into(image_view)

        }

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

    fun addAs(view: View) {
        var current_user_id = FirebaseAuth.getInstance().currentUser?.uid
        var current_user_id_no_null  = ""//TODO: not elegant find another solution
        if(current_user_id!=null){
            current_user_id_no_null = current_user_id //TODO: not elegant find another solution
        }
        if(isUserAppeardInRegularUsersCollection(current_user_id) && (!(isUserAppeardInRegularUsersCollection(viewed_user_id)))){
            firestore.collection("regular_users").document(current_user_id_no_null).update("personal_trainer_uid",viewed_user_id)//
        }else if(isUserAppeardInRegularUsersCollection(viewed_user_id) && (!(isUserAppeardInRegularUsersCollection(current_user_id)))){
            firestore.collection("business_users").document(current_user_id_no_null).collection("customers").add(CustomerData(user_name,photo_url,viewed_user_id))
        }

    }

    fun isUserAppeardInRegularUsersCollection(uid: String?): Boolean{
        var result = false
        firestore.collection("regular_users").document(uid!!).get().addOnSuccessListener {user_doc->
            if (user_doc != null) { result =  true }
        }.addOnFailureListener {
            //TODO: what we wanna do here ?
            throw it
        }
        return result
    }

}

//val uid = FirebaseAuth.getInstance().currentUser?.uid
