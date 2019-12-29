package com.technion.fitracker.adapters.nutrition

import com.technion.fitracker.R
import com.technion.fitracker.adapters.nutrition.NutritionNestedAdapter.ViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class NutritionNestedAdapter(private val nameData: ArrayList<String>, private val countData: ArrayList<String>) :
    RecyclerView.Adapter<ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {

        val v =  LayoutInflater.from(parent.context)
            .inflate(R.layout.nutrition_detailed_card,parent,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return nameData.size
    }

    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {
        holder.name.text = nameData[position]
        holder.count.text = countData[position]
    }


    inner class ViewHolder(view : View) : RecyclerView.ViewHolder(view){

        val name : TextView = view.findViewById(R.id.nutrition_ele_name)
        val count: TextView = view.findViewById(R.id.nutrition_ele_info)

    }
}