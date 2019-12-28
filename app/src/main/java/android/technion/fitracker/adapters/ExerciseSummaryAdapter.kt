package android.technion.fitracker.adapters

import android.technion.fitracker.R
import android.technion.fitracker.models.exercise.AerobicExerciseModel
import android.technion.fitracker.models.exercise.ExerciseBaseModel
import android.technion.fitracker.models.exercise.WeightExerciseModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ExerciseSummaryAdapter(private val myDataset: ArrayList<ExerciseBaseModel>) : RecyclerView.Adapter<ExerciseSummaryAdapter.ExerciseViewHolder>() {

    enum class ExerciseRatings{
        SAD,NEUTRAL,HAPPY,COOL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val v =  LayoutInflater.from(parent.context)
                .inflate(R.layout.element_exercise_summary,parent,false)
        return ExerciseViewHolder(v)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        myDataset[position].let{
            if(it.type.equals("Aerobic")){
                holder.workout_name.text = (it as AerobicExerciseModel).name
            }else{
                holder.workout_name.text = (it as WeightExerciseModel).name
            }
            holder.exercise_time_done.text = it.time_done ?: "---"
            holder.exercise_rating_neutral.isClickable = true
            holder.exercise_rating_cool.isClickable = true
            holder.exercise_rating_sad.isClickable = true
            holder.exercise_rating_happy.isClickable = true
            holder.exercise_rating_cool.setOnClickListener { run { setBaseColor(holder); setSelectedImage(holder, ExerciseRatings.COOL.ordinal) } }
            holder.exercise_rating_sad.setOnClickListener { run {setBaseColor(holder); setSelectedImage(holder, ExerciseRatings.SAD.ordinal) }}
            holder.exercise_rating_happy.setOnClickListener { run {setBaseColor(holder); setSelectedImage(holder, ExerciseRatings.HAPPY.ordinal) }}
            holder.exercise_rating_neutral.setOnClickListener { run {setBaseColor(holder); setSelectedImage(holder, ExerciseRatings.NEUTRAL.ordinal) }}
        }
    }

    private fun setBaseColor(holder: ExerciseViewHolder){
        holder.exercise_rating_cool.setColorFilter(0x00FF0000)
        holder.exercise_rating_sad.setColorFilter(0x00FF0000)
        holder.exercise_rating_happy.setColorFilter(0x00FF0000)
        holder.exercise_rating_neutral.setColorFilter(0x00FF0000)
    }

    private fun setSelectedImage(holder: ExerciseViewHolder,index: Int){
        when(index){
            ExerciseRatings.SAD.ordinal -> {
                holder.exercise_rating_sad.setColorFilter(0xFF0000)
            }
            ExerciseRatings.NEUTRAL.ordinal -> {
                holder.exercise_rating_neutral.setColorFilter(0xFF0000)

            }
            ExerciseRatings.HAPPY.ordinal -> {
                holder.exercise_rating_happy.setColorFilter(0xFF0000)

            }
            ExerciseRatings.COOL.ordinal -> {
                holder.exercise_rating_cool.setColorFilter(0xFF0000)

            }
        }
    }

    override fun getItemCount() = myDataset.size

    inner class ExerciseViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
        var workout_name: TextView = view.findViewById(R.id.workout_name)
        var exercise_time_done: TextView = view.findViewById(R.id.exercise_time_done)
        var exercise_rating_sad: ImageView = view.findViewById(R.id.exercise_rating_sad)
        var exercise_rating_neutral: ImageView = view.findViewById(R.id.exercise_rating_neutral)
        var exercise_rating_happy: ImageView = view.findViewById(R.id.exercise_rating_happy)
        var exercise_rating_cool: ImageView = view.findViewById(R.id.exercise_rating_cool)
    }

}
