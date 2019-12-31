package com.technion.fitracker.adapters.nutrition

import com.technion.fitracker.R
import com.technion.fitracker.adapters.nutrition.NutritionFireStoreAdapter.ViewHolder
import com.technion.fitracker.models.exercise.AerobicExerciseModel
import com.technion.fitracker.models.exercise.ExerciseBaseModel
import com.technion.fitracker.models.nutrition.NutritionFireStoreModel
import com.technion.fitracker.user.Meal
import com.technion.fitracker.user.personal.nutrition.NutritionFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.technion.fitracker.utils.RecyclerCustomItemDecorator
import java.lang.StringBuilder

class NutritionFireStoreAdapter(
    options: FirestoreRecyclerOptions<NutritionFireStoreModel>,
    val onItemClickListener: View.OnClickListener,
    val nutritionFragment: NutritionFragment
) :
        FirestoreRecyclerAdapter<NutritionFireStoreModel, ViewHolder>(options) {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onDataChanged() {
        super.onDataChanged()
        if (itemCount <= 0) {
            nutritionFragment.placeholder.visibility = View.VISIBLE
        } else {
            nutritionFragment.placeholder.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.nutrition_ele, parent, false)
        view.setOnClickListener(onItemClickListener)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: NutritionFireStoreModel) {
        holder.name.text = item.name
        holder.overlay.setOnClickListener {
            holder.view.callOnClick()
        }
        firestore.collection("regular_users").document(auth.currentUser!!.uid).collection("meals")
                .whereEqualTo("name", item.name).get(Source.CACHE).addOnSuccessListener { documents ->
                    initInnerRecycler(documents, holder)
                    firestore.collection("regular_users").document(auth.currentUser!!.uid).collection("meals")
                            .whereEqualTo("name", item.name).get().addOnSuccessListener { documents2 ->
                                initInnerRecycler(documents2, holder)
                            }
                }.addOnFailureListener {
                    firestore.collection("regular_users").document(auth.currentUser!!.uid).collection("meals")
                            .whereEqualTo("name", item.name).get().addOnSuccessListener { documents2 ->
                                initInnerRecycler(documents2, holder)
                            }
                }
    }

    private fun initInnerRecycler(
        documents: QuerySnapshot,
        holder: ViewHolder
    ) {
        val meal = documents.first().toObject(Meal::class.java)
        val names = ArrayList<String>()
        val counts = ArrayList<String>()
        for (dish in meal.meals!!) {
            val sbNames = StringBuilder()
            val sbCount = StringBuilder()
            for (pair in dish) {
                sbNames.appendln(pair.key)
                sbCount.appendln(pair.value)
            }
            names.add(sbNames.toString().substringBeforeLast('\n'))
            counts.add(sbCount.toString().substringBeforeLast('\n'))
        }
        holder.recView.apply {
            layoutManager = LinearLayoutManager(holder.recView.context)
            adapter = NutritionNestedAdapter(names, counts)
            addItemDecoration(
                RecyclerCustomItemDecorator(context,DividerItemDecoration.VERTICAL)
            )
        }
    }

    class ViewHolder(var view: View) :
            RecyclerView.ViewHolder(view) {
        var name: TextView = view.findViewById(R.id.nutritionName)
        var recView: RecyclerView = view.findViewById(R.id.nutrition_dishes_rec_view)
        var overlay: View = view.findViewById(R.id.overlay)

        init {
            view.tag = this
        }
    }

}