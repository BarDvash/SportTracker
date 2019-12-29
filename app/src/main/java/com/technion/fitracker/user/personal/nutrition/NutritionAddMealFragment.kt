package com.technion.fitracker.user.personal.nutrition

import android.content.ClipData
import android.os.Bundle
import com.technion.fitracker.R
import com.technion.fitracker.adapters.nutrition.NutritionMealAdapter
import com.technion.fitracker.databinding.FragmentAddMealBinding
import com.technion.fitracker.models.nutrition.AddMealViewModel
import com.technion.fitracker.user.Meal
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableList
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class NutritionAddMealFragment: Fragment(), View.OnClickListener {

    lateinit var adapter: NutritionMealAdapter
    lateinit var navController: NavController
    lateinit var viewModel: AddMealViewModel
    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var placeHolder: TextView

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
        val view = DataBindingUtil.inflate<FragmentAddMealBinding>(inflater, R.layout.fragment_add_meal,container, false)
        view.viewmodel = viewModel
        setHasOptionsMenu(true)
        return view.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.meal_fragment_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_on_add_new_meal)
        placeHolder = view.findViewById(R.id.meal_fragment_placeholder)
        fab.setOnClickListener(this)
        val rootActivity = (activity as NutritionAddMealActivity)
        rootActivity.setSupportActionBar(view.findViewById(R.id.add_new_meal_toolbar))
        rootActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (rootActivity.updateData) {
            rootActivity.supportActionBar?.title = getString(R.string.edit_meal)
        }
        else {
            rootActivity.supportActionBar?.title = getString(R.string.add_meal_title)
        }
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        navController = Navigation.findNavController(view)


        val recyclerView = view.findViewById<RecyclerView>(R.id.add_meal_recview)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        val onClickListener = View.OnClickListener {
            val recView = it.tag as RecyclerView.ViewHolder
            val pos = recView.adapterPosition
            val bundle = bundleOf("dishes" to viewModel.data[pos], "pos" to pos)
            navController.navigate(R.id.nutritionAddDishFragment,bundle)
        }
        adapter = NutritionMealAdapter(viewModel.data, onClickListener)

        recyclerView?.adapter = adapter
        setPlaceHolderVisibility()

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab_on_add_new_meal -> {
                navController.navigate(R.id.nutritionAddDishFragment)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) { //check on which item the user pressed and perform the appropriate action
            R.id.meal_fragment_delete -> {
                (activity as NutritionAddMealActivity).deleteFromDB()
                activity?.finish()
                true
            }

            R.id.meal_fragment_save -> {
                if (viewModel.editTextMealName.value.isNullOrEmpty()){
                    Toast.makeText(context, getString(R.string.provide_name), Toast.LENGTH_SHORT).show()
                    return true
                }
                (activity as NutritionAddMealActivity).writeToDB((activity as NutritionAddMealActivity).updateData)
                activity?.finish()
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
        if (viewModel.data.isEmpty()) {
            placeHolder.visibility = View.VISIBLE
        }
        else {
            placeHolder.visibility = View.GONE
        }
    }

}