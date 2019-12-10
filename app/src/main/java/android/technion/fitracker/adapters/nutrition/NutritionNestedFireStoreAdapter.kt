package android.technion.fitracker.adapters.nutrition

import android.technion.fitracker.R
import android.technion.fitracker.adapters.nutrition.NutritionNestedFireStoreAdapter.ViewHolder
import android.technion.fitracker.models.nutrition.NutritionNestedFireStoreModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class NutritionNestedFireStoreAdapter(options: FirestoreRecyclerOptions<NutritionNestedFireStoreModel>) :
    FirestoreRecyclerAdapter<NutritionNestedFireStoreModel, ViewHolder>(options){

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {

        val v =  LayoutInflater.from(parent.context)
            .inflate(R.layout.nutrition_detailed_card,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int,item: NutritionNestedFireStoreModel) {
        holder.name.text = item.Name
        holder.count.text = item.Count
    }


    inner class ViewHolder(view : View) : RecyclerView.ViewHolder(view){

        val name : TextView = view.findViewById(R.id.nutrition_ele_name)
        val count: TextView = view.findViewById(R.id.nutrition_ele_info)

    }
}