package com.technion.fitracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.fitracker.R
import com.technion.fitracker.models.workouts.RecentWorkoutFireStoreModel
import com.technion.fitracker.user.business.customer.CustomerMeasurementFragment
import com.technion.fitracker.user.personal.HomeScreenFragment

class RecentWorkoutsFireStoreAdapter(
    options: FirestoreRecyclerOptions<RecentWorkoutFireStoreModel>,
    private val fragment: Fragment
) :
        FirestoreRecyclerAdapter<RecentWorkoutFireStoreModel, RecentWorkoutsFireStoreAdapter.ViewHolder>(options) {

    var mOnItemClickListener: View.OnClickListener? = null

    inner class ViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
        var workoutName: TextView = view.findViewById(R.id.recent_workout_name)
        var dateTime: TextView = view.findViewById(R.id.recent_workout_date_time)

        init {
            view.tag = this
            view.setOnClickListener(mOnItemClickListener)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.element_home_last_workouts, parent, false)
        return ViewHolder(view)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        when (fragment) {
            is HomeScreenFragment -> {
                if (itemCount == 0) {
                    fragment.recentWorkoutsContainer.visibility = View.GONE
                } else {
                    fragment.recentWorkoutsContainer.visibility = View.VISIBLE
                }
                fragment.setPlaceholder()
            }
            is CustomerMeasurementFragment -> {
                if (itemCount == 0) {
                    fragment.recentWorkoutsContainer.visibility = View.GONE
                } else {
                    fragment.recentWorkoutsContainer.visibility = View.VISIBLE
                }
            }
        }

    }


    override fun onBindViewHolder(p0: ViewHolder, p1: Int, p2: RecentWorkoutFireStoreModel) {
        p0.workoutName.text = p2.workout_name
        p0.dateTime.text = p2.date_time
    }


}