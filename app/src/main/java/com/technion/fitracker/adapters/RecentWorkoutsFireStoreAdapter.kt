package com.technion.fitracker.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.fitracker.R
import com.technion.fitracker.models.workouts.RecentWorkoutFireStoreModel
import com.technion.fitracker.user.personal.HomeScreenFragment

class RecentWorkoutsFireStoreAdapter(
    options: FirestoreRecyclerOptions<RecentWorkoutFireStoreModel>,
    private val homeScreenFragment: HomeScreenFragment
) :
        FirestoreRecyclerAdapter<RecentWorkoutFireStoreModel, RecentWorkoutsFireStoreAdapter.ViewHolder>(options) {

    inner class ViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
        var workoutName: TextView = view.findViewById(R.id.recent_workout_name)
        var dateTime: TextView = view.findViewById(R.id.recent_workout_date_time)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.element_home_last_workouts, parent, false)
        return ViewHolder(view)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        if (itemCount == 0) {
            homeScreenFragment.recentWorkoutsContainer.visibility = View.GONE
        } else {
            homeScreenFragment.recentWorkoutsContainer.visibility = View.VISIBLE
        }
    }


    override fun onBindViewHolder(p0: ViewHolder, p1: Int, p2: RecentWorkoutFireStoreModel) {
        Log.w(FragmentActivity.VIBRATOR_SERVICE, "Added " + p2.workout_name)
        p0.workoutName.text = p2.workout_name
        p0.dateTime.text = p2.date_time
    }


}