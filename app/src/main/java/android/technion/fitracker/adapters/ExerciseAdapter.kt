package android.technion.fitracker.adapters

import android.technion.fitracker.R
import android.technion.fitracker.models.exercise.AerobicExerciseModel
import android.technion.fitracker.models.exercise.ExerciseBaseModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ExerciseAdapter(private val myDataset: ArrayList<ExerciseBaseModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class HolderPosition {
        AEROBIC, WEIGHT
    }

    inner class WeightViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
        var name: TextView = view.findViewById(R.id.weight_element_name)
        var weight: TextView = view.findViewById(R.id.weight_element_weight)
        var sets: TextView = view.findViewById(R.id.weight_element_sets)
        var repetitions: TextView = view.findViewById(R.id.weight_element_repetitions)
        var rest: TextView = view.findViewById(R.id.weight_element_rest)
        var notes: TextView = view.findViewById(R.id.weight_element_notes)
    }

    inner class AerobicViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
        var name: TextView = view.findViewById(R.id.aerobic_element_name)
        var duration: TextView = view.findViewById(R.id.aerobic_element_duration)
        var speed: TextView = view.findViewById(R.id.aerobic_element_speed)
        var intensity: TextView = view.findViewById(R.id.aerobic_element_intensity)
        var notes: TextView = view.findViewById(R.id.aerobic_element_notes)
    }

    override fun getItemViewType(position: Int): Int {
        return if (myDataset[position].type.equals("Aerobic")) {
            HolderPosition.AEROBIC.ordinal
        } else {
            HolderPosition.WEIGHT.ordinal
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder?
        viewHolder = when (viewType) {
            HolderPosition.WEIGHT.ordinal -> {
                val weightView = LayoutInflater.from(parent.context).inflate(R.layout.wieght_workout_ele, parent, false)
                WeightViewHolder(weightView)
            }
            HolderPosition.AEROBIC.ordinal -> {
                val aerobicView = LayoutInflater.from(parent.context).inflate(R.layout.aerobic_workout_ele, parent, false)
                AerobicViewHolder(aerobicView)
            }
            else -> null
        }
        return viewHolder!!

    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (myDataset[position].type.equals("Aerobic")) {
            val aerobicElement: AerobicExerciseModel? = myDataset[position].downcastToAerobic()
            val aerobicHolder = (holder as AerobicViewHolder)
            aerobicHolder.name.text = aerobicElement?.name
            aerobicHolder.duration.text = "Duration: " + aerobicElement?.duration
            aerobicHolder.speed.text = "Speed: " + aerobicElement?.speed
            aerobicHolder.intensity.text = "Intensity: " + aerobicElement?.intensity
            aerobicHolder.notes.text = "Notes: " +aerobicElement?.notes
        } else {
            val weightElement = myDataset[position].downcastToWeight()
            val weightHolder = (holder as WeightViewHolder)
            weightHolder.name.text = weightElement?.name
            weightHolder.weight.text = "Weight: " + weightElement?.weight
            weightHolder.sets.text = "Sets: " + weightElement?.sets
            weightHolder.repetitions.text = "Repetitions: " + weightElement?.repetitions
            weightHolder.rest.text = "Rest: " + weightElement?.rest
            weightHolder.notes.text = "Notes: " +weightElement?.notes
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size

}