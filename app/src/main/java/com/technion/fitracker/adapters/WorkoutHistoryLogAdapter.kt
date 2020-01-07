package com.technion.fitracker.adapters



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.technion.fitracker.R
import com.technion.fitracker.models.exercise.ExerciseLogModel

class WorkoutHistoryLogAdapter(private val myDataset: ArrayList<ExerciseLogModel>) : RecyclerView.Adapter<WorkoutHistoryLogAdapter.ExerciseViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.element_exercise_summary, parent, false)
        return ExerciseViewHolder(v)
    }

    inner class ExerciseViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
        var workout_name: TextView = view.findViewById(R.id.workout_name)
        var exercise_time_done: TextView = view.findViewById(R.id.exercise_time_done)
    }



    override fun getItemCount() = myDataset.size

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.workout_name.text = myDataset[position].name
        holder.exercise_time_done.text = myDataset[position].time_done ?: "--:--  "
    }

}