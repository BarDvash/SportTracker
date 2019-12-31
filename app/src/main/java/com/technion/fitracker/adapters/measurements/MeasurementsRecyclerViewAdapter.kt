package com.technion.fitracker.adapters.measurements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.technion.fitracker.R

class MeasurementsRecyclerViewAdapter(private val names: ArrayList<String>, private val values: ArrayList<String>) :
        RecyclerView.Adapter<MeasurementsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.measurements_card, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return names.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.name.text = names[position]
        holder.data.text = values[position]
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.measurements_ele_name)
        val data: TextView = view.findViewById(R.id.measurements_ele_data)
    }

}