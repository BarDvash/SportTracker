package android.technion.fitracker.user.personal.nutrition


import android.content.Intent
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.nutrition.NutritionMealAdapter
import android.technion.fitracker.adapters.nutrition.NutritionNestedDishAdapter
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NutritionAddMealActivity: AppCompatActivity(), View.OnClickListener {

    lateinit var adapter: NutritionMealAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_meal)
        val fab = findViewById<FloatingActionButton>(R.id.fab_on_add_new_meal)
        fab.setOnClickListener(this)
        setSupportActionBar(findViewById(R.id.add_new_meal_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_meal_title)
        data = ArrayList()

        recyclerView = findViewById(R.id.add_meal_recview)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        adapter = NutritionMealAdapter(data)
        recyclerView?.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        this.finish()
        return true
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab_on_add_new_meal -> switchToAddActivity()
            R.id.add_meal_done_button -> {
                //TODO upload to db
                finish()
            }
        }
    }

    private fun switchToAddActivity() {
        val addDish = Intent(applicationContext, NutritionAddDishActivity::class.java)
        startActivity(addDish)

    }

    override fun onStart() {
        super.onStart()
        adapter.notifyDataSetChanged()
    }

    companion object {
        lateinit var data: ArrayList<Map<String,String>>
    }
}