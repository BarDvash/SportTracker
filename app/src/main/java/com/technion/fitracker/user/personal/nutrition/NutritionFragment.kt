package com.technion.fitracker.user.personal.nutrition


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.technion.fitracker.R
import com.technion.fitracker.adapters.nutrition.NutritionFireStoreAdapter
import com.technion.fitracker.databinding.FragmentNutritionBinding
import com.technion.fitracker.models.UserViewModel
import com.technion.fitracker.models.nutrition.NutritionFireStoreModel

/**
 * A simple [Fragment] subclass.
 */
class NutritionFragment : Fragment(), View.OnClickListener {
    private lateinit var mAuth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    private lateinit var viewModel: UserViewModel
    lateinit var fab: ExtendedFloatingActionButton
    lateinit var placeholder: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[UserViewModel::class.java]
        } ?: throw Exception("Invalid Fragment,NutritionFragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            DataBindingUtil.inflate<FragmentNutritionBinding>(inflater, R.layout.fragment_nutrition, container, false)
        view.viewmodel = viewModel
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fab = view.findViewById(R.id.nutrition_fab)
        fab.setOnClickListener(this)
        fab.animation = AnimationUtils.loadAnimation(context!!,R.anim.fab_transition)
        val uid = mAuth.currentUser?.uid
        val query =
            firestore.collection("regular_users").document(uid!!).collection("meals")
                    .orderBy("name", Query.Direction.ASCENDING)
        val options =
            FirestoreRecyclerOptions.Builder<NutritionFireStoreModel>()
                    .setQuery(query, NutritionFireStoreModel::class.java).build()
        val onClickListener = View.OnClickListener {
            val element = it.tag as NutritionFireStoreAdapter.ViewHolder
            val name = element.name.text


            firestore.collection("regular_users").document(uid).collection("meals").whereEqualTo("name", name).get()
                    .addOnSuccessListener {
                        val doc = it.first().toObject(NutritionFireStoreModel::class.java)
                        val bundle = bundleOf("list" to doc.meals, "name" to name, "docId" to it.first().id)
                        val userHome = Intent(context, NutritionAddMealActivity::class.java)
                        userHome.putExtras(bundle)
                        startActivity(userHome)
                    }.addOnFailureListener {

                    }

        }
        viewModel.nutritionAdapter = NutritionFireStoreAdapter(options, onClickListener, this,context!!)
        viewModel.nutritionRV = view.findViewById<RecyclerView>(R.id.nutrition_rec_view).apply{
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
            adapter = viewModel.nutritionAdapter
        }
        placeholder = view.findViewById(R.id.nutrition_placeholder)
    }

    override fun onStart() {
        super.onStart()
        viewModel.nutritionAdapter?.startListening()


    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.nutrition_fab -> switchToAddActivity()
        }
    }

    private fun switchToAddActivity() {
        val userHome = Intent(context, NutritionAddMealActivity::class.java)
        startActivity(userHome)
    }
}
