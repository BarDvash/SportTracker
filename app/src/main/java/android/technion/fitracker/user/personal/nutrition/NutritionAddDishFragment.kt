package android.technion.fitracker.user.personal.nutrition

import android.app.Dialog
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.nutrition.NutritionNestedDishAdapter
import android.technion.fitracker.databinding.FragmentAddDishBinding
import android.technion.fitracker.models.nutrition.AddMealViewModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NutritionAddDishFragment: Fragment(), View.OnClickListener {

//    var dishes:HashMap<String,String> = HashMap()
    val names:ArrayList<String> = ArrayList()
    val counts:ArrayList<String> = ArrayList()
    lateinit var  adapter: NutritionNestedDishAdapter
    lateinit var navController: NavController
    lateinit var viewModel: AddMealViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[AddMealViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        viewModel.dishes = HashMap()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = DataBindingUtil.inflate<FragmentAddDishBinding>(inflater, R.layout.fragment_add_dish,container, false)
        view.viewmodel = viewModel
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_on_add_dish)
        fab.setOnClickListener(this)
        view.findViewById<Button>(R.id.add_dish_done_button).setOnClickListener(this)
        (activity as AppCompatActivity).setSupportActionBar(view.findViewById(R.id.activity_add_dish_toolbar))
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.add_new_dish)

        val recyclerView = view.findViewById<RecyclerView>(R.id.add_dish_recview)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = NutritionNestedDishAdapter(names,counts)
        recyclerView?.adapter = adapter
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab_on_add_dish -> switchToAddActivity()
            R.id.add_dish_done_button -> {
                if (viewModel.dishes.isNotEmpty())
                    viewModel.data.add(viewModel.dishes)
                navController.popBackStack()
            }
        }
    }


    private fun switchToAddActivity() {
        val dial = Dialog(context!!,R.style.WideDialog)
        dial.setContentView(R.layout.nutrition_add_optional_dish)
        dial.setTitle("Add a new dish")
        val nameEditText = dial.findViewById<EditText>(R.id.dish_name_edittext)
        val countEditText = dial.findViewById<EditText>(R.id.dish_count_edittext)
        val addButton = dial.findViewById<Button>(R.id.dish_add)
        addButton.setOnClickListener {
            val name = nameEditText.text.toString()
            if (name.isEmpty()){
                Toast.makeText(context,"Name should not be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val count = countEditText.text.toString()
            viewModel.dishes[name] = count
            names.add(name)
            counts.add(count)
            adapter.notifyDataSetChanged()
            dial.dismiss()
        }
        dial.show()
    }
}