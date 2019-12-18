package android.technion.fitracker.user.personal.nutrition

import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.nutrition.NutritionMealAdapter
import android.technion.fitracker.databinding.FragmentAddMealBinding
import android.technion.fitracker.models.nutrition.AddMealViewModel
import android.technion.fitracker.user.Meal
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableList
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class NutritionAddMealFragment: Fragment(), View.OnClickListener {

    lateinit var adapter: NutritionMealAdapter
    lateinit var navController: NavController
    lateinit var viewModel: AddMealViewModel
    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[AddMealViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        viewModel.data.addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableList<Map<String,String>>>() {
            override fun onChanged(sender: ObservableList<Map<String,String>>) {
                adapter.notifyDataSetChanged()
            }

            override fun onItemRangeRemoved(sender: ObservableList<Map<String,String>>, positionStart: Int, itemCount: Int) {
                adapter.notifyDataSetChanged()
            }

            override fun onItemRangeInserted(sender: ObservableList<Map<String,String>>, positionStart: Int, itemCount: Int) {
                adapter.notifyDataSetChanged()
            }

            override fun onItemRangeMoved(sender: ObservableList<Map<String,String>>, fromPosition: Int, toPosition: Int, itemCount: Int) {
                adapter.notifyDataSetChanged()
            }

            override fun onItemRangeChanged(sender: ObservableList<Map<String,String>>, positionStart: Int, itemCount: Int) {
                adapter.notifyDataSetChanged()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = DataBindingUtil.inflate<FragmentAddMealBinding>(inflater, R.layout.fragment_add_meal,container, false)
        view.viewmodel = viewModel
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab_on_add_new_meal)
        fab.setOnClickListener(this)
        (activity as AppCompatActivity).setSupportActionBar(view.findViewById(R.id.add_new_meal_toolbar))
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.add_meal_title)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        navController = Navigation.findNavController(view)


        val recyclerView = view.findViewById<RecyclerView>(R.id.add_meal_recview)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = NutritionMealAdapter(viewModel.data)
        recyclerView?.adapter = adapter

        view.findViewById<Button>(R.id.add_meal_done_button).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab_on_add_new_meal -> {
                navController.navigate(R.id.nutritionAddDishFragment)
            }
            R.id.add_meal_done_button -> {
                val data = Meal(viewModel.editTextMealName.value,viewModel.data)
                db.collection("regular_users").document(auth.currentUser!!.uid).collection("meals").add(data).addOnSuccessListener {
//                    Toast.makeText(context,"done",Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
//                    Toast.makeText(context,"nope",Toast.LENGTH_SHORT).show()
                }
                activity?.finish()
            }
        }
    }
}