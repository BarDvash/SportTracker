package android.technion.fitracker.adapters

import android.technion.fitracker.R
import android.technion.fitracker.models.workouts.RecentWorkoutFireStoreModel
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class RecentWorkoutsFireStoreAdapter(options: FirestoreRecyclerOptions<RecentWorkoutFireStoreModel>) :
        FirestoreRecyclerAdapter<RecentWorkoutFireStoreModel, RecentWorkoutsFireStoreAdapter.ViewHolder>(options) {

    inner class ViewHolder(view: View) :
            RecyclerView.ViewHolder(view){
        var workout_name: TextView = view.findViewById(R.id.recent_workout_name)
        var date_time: TextView = view.findViewById(R.id.recent_workout_date_time)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.element_home_last_workouts,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int, p2: RecentWorkoutFireStoreModel) {
        Log.w(FragmentActivity.VIBRATOR_SERVICE, "Added " + p2.workout_name)
        p0.workout_name.text = p2.workout_name
        p0.date_time.text = p2.date_time
    }


}