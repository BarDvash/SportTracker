package com.technion.fitracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserLandingPageActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_landing_page)
        setSupportActionBar(findViewById(R.id.search_toolbar))
        firestore = FirebaseFirestore.getInstance()


        var bundle: Bundle? = intent.extras
        var user_name = bundle!!.getString("user_name")
        var photo_url = bundle!!.getString("photo_url")

        var name: TextView = findViewById(R.id.search_result_landing_page_user_name)
        var image: ImageView = findViewById(R.id.search_result_landing_page_user_avatar)


        name.text = user_name

        if (!photo_url.isNullOrEmpty()) {
            //Glide.with(activity).load(item.photoURL).into(holder.user_image)

            Glide.with(this) //1
                    .load(photo_url)
                    .placeholder(R.drawable.user_avatar)
                    .error(R.drawable.user_avatar)
                    .skipMemoryCache(true) //2
                    .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                    .transform(CircleCrop()) //4
                    .into(image)

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
}

