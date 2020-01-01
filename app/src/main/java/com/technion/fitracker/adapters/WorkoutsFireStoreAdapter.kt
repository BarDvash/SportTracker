package com.technion.fitracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.fitracker.R
import com.technion.fitracker.adapters.WorkoutsFireStoreAdapter.ViewHolder
import com.technion.fitracker.models.WorkoutFireStoreModel
import com.technion.fitracker.user.personal.workout.WorkoutsFragment

class WorkoutsFireStoreAdapter(options: FirestoreRecyclerOptions<WorkoutFireStoreModel>, val workoutsFragment: WorkoutsFragment) :
        FirestoreRecyclerAdapter<WorkoutFireStoreModel, ViewHolder>(options) {

    var mOnItemClickListener: View.OnClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.element_workout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        item: WorkoutFireStoreModel
    ) {
        holder.name.text = item.name
        holder.desc.text = item.desc
    }

    override fun onDataChanged() {
        super.onDataChanged()
        if (itemCount <= 0) {
            workoutsFragment.placeholder.visibility = View.VISIBLE
        } else {
            workoutsFragment.placeholder.visibility = View.GONE
        }
    }


    inner class ViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
        var name: TextView = view.findViewById(R.id.workout_name)
        var desc: TextView = view.findViewById(R.id.workout_info)

        init {
            view.tag = this
            view.setOnClickListener(mOnItemClickListener)
        }

    }
}