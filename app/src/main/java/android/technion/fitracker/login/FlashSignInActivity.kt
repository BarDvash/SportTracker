package android.technion.fitracker.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.technion.fitracker.user.business.BusinessUserActivity
import android.technion.fitracker.user.personal.UserActivity


class FlashSignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(android.technion.fitracker.R.layout.activity_flash_sign_in)

        object : CountDownTimer(1000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                // do something after 1s
            }

            override fun onFinish() {
                // do something end times 5s

                val user_type = intent.getStringExtra("user_type")

                if(user_type == "regular"){
                    val userHome = Intent(applicationContext, UserActivity::class.java)
                    startActivity(userHome)
                    finish()
                }else{
                    val userHome = Intent(applicationContext, BusinessUserActivity::class.java)
                    startActivity(userHome)
                    finish()
                }
            }

        }.start()
    }

}


