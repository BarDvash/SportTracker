package com.technion.fitracker.adapters.measurements

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.technion.fitracker.R
import com.technion.fitracker.user.personal.measurements.MeasurementsGraphActivity

class MeasurementsRecyclerViewAdapter(private val names: ArrayList<String>, private val values: ArrayList<String>) :
        RecyclerView.Adapter<MeasurementsRecyclerViewAdapter.ViewHolder>() {

    private val nameToUnits: HashMap<String,String> = hashMapOf("Biceps" to "(cm)",
                                                                "Body fat" to "(%)",
                                                                "Chest" to "(cm)",
                                                                "Hips" to "(cm)",
                                                                "Waist" to "(cm)",
                                                                "Weight" to "(kg)")

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
        holder.imageButton.setOnClickListener {
            val bundle = bundleOf("name" to names[position])
            val activity = Intent(holder.context, MeasurementsGraphActivity::class.java)
            activity.putExtras(bundle)
            startActivity(holder.context,activity,bundle)
        }
        if (nameToUnits.containsKey(names[position])){
            holder.unuts.text = nameToUnits[names[position]]
        }
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.measurements_ele_name)
        val data: TextView = view.findViewById(R.id.measurements_ele_data)
        val imageButton: ImageView = view.findViewById(R.id.measurment_history_image)
        val unuts: TextView = view.findViewById(R.id.measure_units)
        val context = view.context
    }

}