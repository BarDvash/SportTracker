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
        holder.name.text = item.name
        firestore.collection("users").document(auth.currentUser!!.uid).collection("meals").document(item.name!!).collection("dishes").get().addOnSuccessListener { documents ->
            val sb = StringBuilder()
            for (document in documents) {
                val doc = document.toObject(DishesModel::class.java)
                sb.appendln(doc.name)
            }
            holder.desc.text = sb.toString()
        }
            .addOnFailureListener { exception ->

            }
    }

    inner class ViewHolder(view: View) :
        RecyclerView.ViewHolder(view){
        var name: TextView = view.findViewById(R.id.nutritionName)
        var desc: TextView = view.findViewById(R.id.nutritionInfo)
    }

}