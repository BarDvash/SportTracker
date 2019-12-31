package com.technion.fitracker.user.personal.nutrition

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.gson.Gson
import com.technion.fitracker.R
import com.technion.fitracker.adapters.nutrition.NutritionNestedDishAdapter
import com.technion.fitracker.databinding.FragmentAddDishBinding
import com.technion.fitracker.models.nutrition.AddMealViewModel
import com.technion.fitracker.models.nutrition.jsonDBModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NutritionAddDishFragment : Fragment(), View.OnClickListener {

    val names: ArrayList<String> = ArrayList()
    val counts: ArrayList<String> = ArrayList()
    lateinit var adapter: NutritionNestedDishAdapter
    lateinit var navController: NavController
    lateinit var viewModel: AddMealViewModel
    lateinit var placeHolder: TextView

    var foodDb: Array<String> = arrayOf("")

    private var isEditing = false
    private var editPos = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[AddMealViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =
            DataBindingUtil.inflate<FragmentAddDishBinding>(inflater, R.layout.fragment_add_dish, container, false)
        view.viewmodel = viewModel
        setHasOptionsMenu(true)
        return view.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dish_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        initDB()
        placeHolder = view.findViewById(R.id.dish_fragment_placeholder)
        val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_on_add_dish)
        fab.setOnClickListener(this)
        val toolBarView = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.activity_add_dish_toolbar)
        val rootActivity = (activity as AppCompatActivity)
        rootActivity.setSupportActionBar(toolBarView)
        rootActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var dishes: HashMap<String, String>?
        if (arguments?.get("dishes") != null) {
            isEditing = true
            editPos = arguments?.getInt("pos")!!
            dishes = arguments?.get("dishes") as HashMap<String, String>
            viewModel.dishes = dishes
            for (item in dishes) {
                names.add(item.key)
                counts.add(item.value)
            }
            rootActivity.supportActionBar?.title = getString(R.string.edit_dish)
        } else {
            viewModel.dishes = HashMap()
            rootActivity.supportActionBar?.title = getString(R.string.add_new_dish)
        }


        val recyclerView = view.findViewById<RecyclerView>(R.id.add_dish_recview)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    if (fab.isShown) {
                        fab.hide()
                    }
                } else if (dy < 0) {
                    if (!fab.isShown) {
                        fab.show()
                    }
                }
            }
        })
        adapter = NutritionNestedDishAdapter(names, counts)
        adapter.onItemClickListener = View.OnClickListener {
            val recView = it.tag as RecyclerView.ViewHolder
            val pos = recView.adapterPosition
            val dial = Dialog(context!!, R.style.WideDialog)
            dial.setContentView(R.layout.nutrition_add_optional_dish)
            dial.setTitle(getString(R.string.edit_dish))


            val nameEditText = dial.findViewById<AutoCompleteTextView>(R.id.dish_name_edittext)
            val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_dropdown_item_1line, foodDb)
            nameEditText.setAdapter(adapter)
            nameEditText.threshold = 1

            val countEditText = dial.findViewById<EditText>(R.id.dish_count_edittext)
            val oldName = Editable.Factory.getInstance().newEditable(names[pos])
            nameEditText.text = Editable.Factory.getInstance().newEditable(names[pos])
            countEditText.text = Editable.Factory.getInstance().newEditable(counts[pos])
            val addButton = dial.findViewById<Button>(R.id.dish_add)
            addButton.setOnClickListener {
                val name = nameEditText.text.toString()
                if (name.isEmpty()) {
                    Toast.makeText(context, getString(R.string.name_shouldnt_be_empty), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val count = countEditText.text.toString()
                viewModel.dishes.remove(oldName.toString())
                viewModel.dishes[name] = count
                names[pos] = name
                counts[pos] = count
                adapter.notifyDataSetChanged()
                setPlaceHolderVisibility()
                dial.dismiss()
            }
            val deleteButton = dial.findViewById<Button>(R.id.dish_delete)
            deleteButton.setOnClickListener {
                viewModel.dishes.remove(oldName.toString())
                names.removeAt(pos)
                counts.removeAt(pos)
                adapter.notifyDataSetChanged()
                setPlaceHolderVisibility()
                dial.dismiss()
            }
            dial.show()
        }
        recyclerView?.adapter = adapter
        setPlaceHolderVisibility()
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab_on_add_dish -> switchToAddActivity()
        }
    }


    private fun initDB() {
        val stream = context!!.assets.open("db.json")
        val s = Scanner(stream).useDelimiter("\\A")
        val json = if (s.hasNext()) {
            s.next()
        } else {
            ""
        }
        foodDb = Gson().fromJson(json, jsonDBModel::class.java).array
    }

    private fun switchToAddActivity() {
        val dial = Dialog(context!!, R.style.WideDialog)
        dial.setContentView(R.layout.nutrition_add_optional_dish)
        dial.setTitle(getString(R.string.add_dish))

        val nameEditText = dial.findViewById<AutoCompleteTextView>(R.id.dish_name_edittext)
        val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_dropdown_item_1line, foodDb)
        nameEditText.setAdapter(adapter)
        nameEditText.threshold = 1

        val countEditText = dial.findViewById<EditText>(R.id.dish_count_edittext)
        val addButton = dial.findViewById<Button>(R.id.dish_add)
        val deleteButton = dial.findViewById<Button>(R.id.dish_delete)
        addButton.setOnClickListener {
            val name = nameEditText.text.toString()
            if (name.isEmpty()) {
                Toast.makeText(context, getString(R.string.name_shouldnt_be_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val count = countEditText.text.toString()
            viewModel.dishes[name] = count
            names.add(name)
            counts.add(count)
            adapter.notifyDataSetChanged()
            setPlaceHolderVisibility()
            dial.dismiss()
        }
        deleteButton.setOnClickListener {
            setPlaceHolderVisibility()
            dial.dismiss()
        }
        dial.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) { //check on which item the user pressed and perform the appropriate action
            R.id.dish_fragment_delete -> {
                viewModel.data.removeAt(editPos)
                navController.popBackStack()
                true
            }
            R.id.dish_fragment_save -> {
                if (viewModel.dishes.isNotEmpty() && !isEditing)
                    viewModel.data.add(viewModel.dishes)
                else if (viewModel.dishes.isNotEmpty() && isEditing) {
                    viewModel.data[editPos] = viewModel.dishes
                }
                navController.popBackStack()
                true
            }
            else -> {
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun setPlaceHolderVisibility() {
        if (names.isEmpty()) {
            placeHolder.visibility = View.VISIBLE
        } else {
            placeHolder.visibility = View.GONE
        }
    }
}