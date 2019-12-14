package android.technion.fitracker.user.personal.nutrition

import android.app.Dialog
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.nutrition.NutritionNestedDishAdapter
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NutritionAddDishActivity: AppCompatActivity(), View.OnClickListener {

    var dishes:HashMap<String,String> = HashMap()
    val names:ArrayList<String> = ArrayList()
    val counts:ArrayList<String> = ArrayList()
    lateinit var  adapter: NutritionNestedDishAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutrition_add_dish)
        val fab = findViewById<FloatingActionButton>(R.id.fab_on_add_dish)
        fab.setOnClickListener(this)
        findViewById<Button>(R.id.add_dish_done_button).setOnClickListener(this)
        setSupportActionBar(findViewById(R.id.activity_add_dish_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_new_dish)

        recyclerView = findViewById(R.id.add_dish_recview)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        adapter = NutritionNestedDishAdapter(names,counts)
        recyclerView?.adapter = adapter

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab_on_add_dish -> switchToAddActivity()
            R.id.add_dish_done_button -> {
                NutritionAddMealActivity.data.add(dishes)
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        this.finish()
        return true
    }

    private fun switchToAddActivity() {
        val dial = Dialog(this,R.style.WideDialog)
        dial.setContentView(R.layout.nutrition_add_optional_dish)
        dial.setTitle("Add a new dish")
        val nameEditText = dial.findViewById<EditText>(R.id.dish_name_edittext)
        val countEditText = dial.findViewById<EditText>(R.id.dish_count_edittext)
        val addButton = dial.findViewById<Button>(R.id.dish_add)
        addButton.setOnClickListener {
            //TODO not empty
            val name = nameEditText.text.toString()
            val count = countEditText.text.toString()
            dishes[name] = count
            names.add(name)
            counts.add(count)
            adapter.notifyDataSetChanged()
            dial.dismiss()
        }
        dial.show()
    }
}