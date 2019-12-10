package android.technion.fitracker.adapters.nutrition

import android.technion.fitracker.R
import android.technion.fitracker.adapters.nutrition.NutritionFireStoreAdapter.ViewHolder
import android.technion.fitracker.models.DishesModel
import android.technion.fitracker.models.nutrition.NutritionFireStoreModel
import android.technion.fitracker.models.nutrition.NutritionNestedFireStoreModel
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.lang.StringBuilder

class NutritionFireStoreAdapter(options: FirestoreRecyclerOptions<NutritionFireStoreModel>) :
    FirestoreRecyclerAdapter<NutritionFireStoreModel, ViewHolder>(options) {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nutrition_ele, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: NutritionFireStoreModel) {
        holder.name.text = item.Name
        holder.rec_view.setHasFixedSize(true)
        firestore.collection("users").document(auth.currentUser!!.uid).collection("meals").whereEqualTo("Name",item.Name).get().addOnSuccessListener {
            documents ->
                val hz = documents.first().reference.collection("dishes")
                val options = FirestoreRecyclerOptions.Builder<NutritionNestedFireStoreModel>().setQuery(hz, NutritionNestedFireStoreModel::class.java).build()
                val childAdapter = NutritionNestedFireStoreAdapter(options)
//            childAdapter.startListening()
                holder.rec_view.apply {
                    layoutManager = LinearLayoutManager(holder.rec_view.context)
                    adapter = childAdapter
                    setRecycledViewPool(viewPool)
                }
        }



    }

    inner class ViewHolder(view: View) :
        RecyclerView.ViewHolder(view){
        var name: TextView = view.findViewById(R.id.nutritionName)
        var rec_view: RecyclerView = view.findViewById(R.id.nutrition_dishes_rec_view)
    }

}