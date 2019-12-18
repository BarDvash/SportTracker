package android.technion.fitracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserLandingPageActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private  lateinit var name : String
    private  lateinit var photo : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_landing_page)
        setSupportActionBar(findViewById(R.id.search_toolbar))
        firestore = FirebaseFirestore.getInstance()

        //get the name and photoUrl

        ////////////////////////////////


        /**
        if (auth.currentUser != null) {
            val docRef = firestore.collection("regular_users").document(auth.currentUser!!.uid)
            docRef.get().addOnSuccessListener {
                    document ->
                val user = document.toObject(User::class.java)
                findViewById<TextView>(R.id.user_name).text = user?.name ?: "Username"
                if (!user?.photoURL.isNullOrEmpty()) {
                    Glide.with(this) //1
                            .load(user?.photoURL)
                            .placeholder(R.drawable.user_avatar)
                            .error(R.drawable.user_avatar)
                            .skipMemoryCache(true) //2
                            .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                            .transform(CircleCrop()) //4
                            .into(findViewById(R.id.user_avatar))
                }
            }
        }
        **/
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

