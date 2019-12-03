package android.technion.fitracker.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.technion.fitracker.R

class UserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        setSupportActionBar(findViewById(R.id.my_toolbar))
    }
}
