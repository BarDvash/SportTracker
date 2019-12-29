package com.technion.fitracker.adapters.measurements

import com.technion.fitracker.R
import com.technion.fitracker.models.measurements.MeasurementsHistoryModel
import com.technion.fitracker.adapters.measurements.MeasurementsFireStoreAdapter.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import java.sql.Date
import java.text.SimpleDateFormat

class MeasurementsFireStoreAdapter(options: FirestoreRecyclerOptions<MeasurementsHistoryModel>) : FirestoreRecyclerAdapter<MeasurementsHistoryModel, ViewHolder>(options) {

    val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
    val newDateFormat = SimpleDateFormat("dd-MMMM-yyyy HH:mm")

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.measurements_history_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        item: MeasurementsHistoryModel
    ) {
        val date = dateFormat.parse(item.data!!)


        holder.date.text = newDateFormat.format(date!!)

        //TODO make generic
        holder.name_1.text = "Weight"
        holder.name_2.text = "Body fat"
        holder.name_3.text = "Chest"
        holder.name_4.text = "Waist"
        holder.name_5.text = "Hips"
        holder.name_6.text = "Biceps"

        holder.data_1.text = item.weight
        holder.data_2.text = item.body_fat
        holder.data_3.text = item.chest
        holder.data_4.text = item.waist
        holder.data_5.text = item.hips
        holder.data_6.text = item.biceps
        for (entry in holder.list) {
            if (entry.value.text.isNullOrEmpty()) {
                entry.key.visibility = View.GONE
                entry.value.visibility = View.GONE
            }
        }

    }

    inner class ViewHolder(view: View) :
            RecyclerView.ViewHolder(view){
        var name_1: TextView = view.findViewById(R.id.measurements_name_1)
        var name_2: TextView = view.findViewById(R.id.measurements_name_2)
        var name_3: TextView = view.findViewById(R.id.measurements_name_3)
        var name_4: TextView = view.findViewById(R.id.measurements_name_4)
        var name_5: TextView = view.findViewById(R.id.measurements_name_5)
        var name_6: TextView = view.findViewById(R.id.measurements_name_6)

        var data_1: TextView = view.findViewById(R.id.measurements_data_1)
        var data_2: TextView = view.findViewById(R.id.measurements_data_2)
        var data_3: TextView = view.findViewById(R.id.measurements_data_3)
        var data_4: TextView = view.findViewById(R.id.measurements_data_4)
        var data_5: TextView = view.findViewById(R.id.measurements_data_5)
        var data_6: TextView = view.findViewById(R.id.measurements_data_6)

        var date: TextView = view.findViewById(R.id.measurements_date)
        var list: HashMap<TextView,TextView> = hashMapOf(name_1 to data_1,
                                                         name_2 to data_2,
                                                         name_3 to data_3,
                                                         name_4 to data_4,
                                                         name_5 to data_5,
                                                         name_6 to data_6)
    }
}