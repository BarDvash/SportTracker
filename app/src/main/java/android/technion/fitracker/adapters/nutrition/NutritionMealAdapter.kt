package android.technion.fitracker.adapters.nutrition

import android.technion.fitracker.R
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.view.View
import androidx.databinding.ObservableArrayList
import androidx.recyclerview.widget.RecyclerView
import java.lang.StringBuilder

class NutritionMealAdapter(private val data: ObservableArrayList<Map<String, String>>, private val onItemClickListener: View.OnClickListener) :
    RecyclerView.Adapter<NutritionMealAdapter.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {

        val v =  LayoutInflater.from(parent.context)
            .inflate(R.layout.nutrition_meal_card,parent,false)
        v.setOnClickListener(onItemClickListener)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {
        val sbNames = StringBuilder()
        val sbCount = StringBuilder()
        for (item in data[position]){
            sbNames.appendln(item.key)
            sbCount.appendln(item.value)
        }
        holder.name.text = sbNames.toString().substringBeforeLast('\n')
        holder.count.text = sbCount.toString().substringBeforeLast('\n')
    }


    inner class ViewHolder(view : View) : RecyclerView.ViewHolder(view){

        val name : TextView = view.findViewById(R.id.nutrition_meal_name)
        val count: TextView = view.findViewById(R.id.nutrition_meal_info)
        init {
            view.tag = this
        }
    }
}