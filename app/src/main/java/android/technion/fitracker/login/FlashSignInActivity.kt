package android.technion.fitracker.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.technion.fitracker.R
import android.technion.fitracker.user.User
import android.technion.fitracker.user.business.BusinessUserActivity
import android.technion.fitracker.user.personal.UserActivity
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore


class FlashSignInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flash_sign_in)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        if (auth.currentUser != null) {
            var regular_users_doc = firestore.collection("regular_users").document(auth.currentUser!!.uid)
            var business_users_doc = firestore.collection("business_users").document(auth.currentUser!!.uid)

            regular_users_doc.get().addOnSuccessListener {
                if (it.exists()) {
                    startUserActivity()
                } else {
                    business_users_doc.get().addOnSuccessListener {
                        startBusinessUserActivity()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(applicationContext, getString(R.string.database_read_error), Toast.LENGTH_SHORT).show()
                startLoginActivity()
            }
        } else {
            startLoginActivity()
        }
    }


    private fun startLoginActivity() {
        val userHome = Intent(this, LoginActivity::class.java)
        startActivity(userHome)
        finish()
    }

    private fun startUserActivity() {
        val userHome = Intent(this, UserActivity::class.java)
        startActivity(userHome)
        finish()

    }

    private fun startBusinessUserActivity() {

        val userHome = Intent(this, BusinessUserActivity::class.java)
        startActivity(userHome)
        finish()

    }


}


