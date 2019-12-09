package android.technion.fitracker.adapters

import android.technion.fitracker.R
import android.technion.fitracker.adapters.NutritionFireStoreAdapter.ViewHolder
import android.technion.fitracker.models.DishesModel
import android.technion.fitracker.models.NutritionFireStoreModel
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.StringBuilder

class NutritionFireStoreAdapter(options: FirestoreRecyclerOptions<NutritionFireStoreModel>) :
    FirestoreRecyclerAdapter<NutritionFireStoreModel, ViewHolder>(options) {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth



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
        firestore.collection("users").document(auth.currentUser!!.uid).collection("meals").whereEqualTo("Name",item.Name!!).get().addOnSuccessListener {
          documents ->
            documents.first().reference.collection("dishes").get().addOnSuccessListener {
                    dishes ->
                val sbNames = StringBuilder()
                val sbCounts = StringBuilder()
                for (dish in dishes) {
                    val doc = dish.toObject(DishesModel::class.java)
                    sbNames.appendln(doc.Name)
                    sbCounts.appendln(doc.Count)
                }
                holder.desc.text = sbNames.toString()
                holder.count.text = sbCounts.toString()
            }
                .addOnFailureListener { exception ->
                    Log.d("DB","Can't get subcollection from DB")
                }

        }.addOnFailureListener {
            Log.d("DB","Can't get meals from DB")
        }
    }

    inner class ViewHolder(view: View) :
        RecyclerView.ViewHolder(view){
        var name: TextView = view.findViewById(R.id.nutritionName)
        var desc: TextView = view.findViewById(R.id.nutritionInfo)
        var count: TextView = view.findViewById(R.id.nutritionQuantity)
    }

}