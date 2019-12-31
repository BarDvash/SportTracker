package com.technion.fitracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.technion.fitracker.R
import com.technion.fitracker.models.exercise.AerobicExerciseModel
import com.technion.fitracker.models.exercise.ExerciseBaseModel
import com.technion.fitracker.models.exercise.WeightExerciseModel

class ExerciseSummaryAdapter(private val myDataset: ArrayList<ExerciseBaseModel>) : RecyclerView.Adapter<ExerciseSummaryAdapter.ExerciseViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.element_exercise_summary, parent, false)
        return ExerciseViewHolder(v)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        myDataset[position].let {
            if (it.type.equals("Aerobic")) {
                holder.workout_name.text = (it as AerobicExerciseModel).name
            } else {
                holder.workout_name.text = (it as WeightExerciseModel).name
            }
            holder.exercise_time_done.text = it.time_done ?: "---"
        }
    }


    override fun getItemCount() = myDataset.size

    inner class ExerciseViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
        var workout_name: TextView = view.findViewById(R.id.workout_name)
        var exercise_time_done: TextView = view.findViewById(R.id.exercise_time_done)
    }

}
