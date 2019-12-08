package android.technion.fitracker.adapters

import android.technion.fitracker.R
import android.technion.fitracker.adapters.NutritionFireStoreAdapter.ViewHolder
import android.technion.fitracker.models.NutritionFireStoreModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class NutritionFireStoreAdapter(options: FirestoreRecyclerOptions<NutritionFireStoreModel>) :
    FirestoreRecyclerAdapter<NutritionFireStoreModel, ViewHolder>(options) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nutrition_ele, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: NutritionFireStoreModel) {
        holder.name.text = item.name
        holder.desc.text = item.desc
    }

    inner class ViewHolder(view: View) :
        RecyclerView.ViewHolder(view){
        var name: TextView = view.findViewById(R.id.nutritionName)
        var desc: TextView = view.findViewById(R.id.nutritionInfo)
    }

}