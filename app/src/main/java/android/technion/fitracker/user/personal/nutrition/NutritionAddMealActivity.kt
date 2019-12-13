package android.technion.fitracker.user.personal.nutrition

import android.os.Bundle
import android.technion.fitracker.R
import androidx.appcompat.app.AppCompatActivity

class NutritionAddMealActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_meal)
        setSupportActionBar(findViewById(R.id.add_new_meal_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_meal_title)
    }

    override fun onSupportNavigateUp(): Boolean {
        this.finish()
        return true
    }
}