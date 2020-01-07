package com.technion.fitracker.adapters.nutrition

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.technion.fitracker.R
import com.technion.fitracker.adapters.nutrition.NutritionFireStoreAdapter.ViewHolder
import com.technion.fitracker.models.nutrition.NutritionFireStoreModel
import com.technion.fitracker.user.Meal
import com.technion.fitracker.user.business.customer.CustomerNutritionFragment
import com.technion.fitracker.user.personal.nutrition.NutritionFragment
import com.technion.fitracker.utils.RecyclerCustomItemDecorator

class NutritionFireStoreAdapter(
    options: FirestoreRecyclerOptions<NutritionFireStoreModel>,
    val onItemClickListener: View.OnClickListener,
    val fragment: Fragment,
    val mContext: Context,
    val userID: String? = null
) :
        FirestoreRecyclerAdapter<NutritionFireStoreModel, ViewHolder>(options) {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onDataChanged() {
        super.onDataChanged()
        when (fragment) {
            is NutritionFragment -> {
                (fragment as NutritionFragment).apply {
                    if (itemCount <= 0) {
                        placeholder.visibility = View.VISIBLE
                    } else {
                        placeholder.visibility = View.GONE
                    }
                }
            }
            is CustomerNutritionFragment -> {
                (fragment as CustomerNutritionFragment).apply {
                    if (itemCount <= 0) {
                        placeholder.visibility = View.VISIBLE
                    } else {
                        placeholder.visibility = View.GONE
                    }
                }
            }
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
        holder.container.animation = AnimationUtils.loadAnimation(mContext, R.anim.scale_in_card)
        holder.name.text = item.name
        holder.overlay.setOnClickListener {
            holder.view.callOnClick()
        }

        val uid = userID ?: auth.currentUser!!.uid
        firestore.collection("regular_users").document(uid).collection("meals")
                .whereEqualTo("name", item.name).get(Source.CACHE).addOnSuccessListener { documents ->
                    initInnerRecycler(documents, holder)
                    firestore.collection("regular_users").document(uid).collection("meals")
                            .whereEqualTo("name", item.name).get().addOnSuccessListener { documents2 ->
                                initInnerRecycler(documents2, holder)
                            }
                }.addOnFailureListener {
                    firestore.collection("regular_users").document(uid).collection("meals")
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
                RecyclerCustomItemDecorator(context, DividerItemDecoration.VERTICAL)
            )
        }
    }

    class ViewHolder(var view: View) :
            RecyclerView.ViewHolder(view) {
        var container: MaterialCardView = view.findViewById(R.id.card)
        var name: TextView = view.findViewById(R.id.nutritionName)
        var recView: RecyclerView = view.findViewById(R.id.nutrition_dishes_rec_view)
        var overlay: View = view.findViewById(R.id.overlay)

        init {
            view.tag = this
        }
    }

}