package android.technion.fitracker.adapters

import android.technion.fitracker.R
import android.technion.fitracker.models.exercise.AerobicExerciseModel
import android.technion.fitracker.models.exercise.ExerciseBaseModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView





class ExerciseCompactAdapter(private val myDataset: ArrayList<ExerciseBaseModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class HolderPosition {
        AEROBIC, WEIGHT
    }

    var mOnItemClickListener: View.OnClickListener? = null

    inner class WeightViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
        var doneImage: ImageView = view.findViewById(R.id.exerciseDoneImage)
        var weightBodyLayout: LinearLayout = view.findViewById(R.id.weight_workout_body)
        var name: TextView = view.findViewById(R.id.weight_element_name)
        var weight: TextView = view.findViewById(R.id.weight_element_weight)
        var sets: TextView = view.findViewById(R.id.weight_element_sets)
        var repetitions: TextView = view.findViewById(R.id.weight_element_repetitions)
        var rest: TextView = view.findViewById(R.id.weight_element_rest)
        init {
            view.setTag(this)
            view.setOnClickListener(mOnItemClickListener)
        }
    }

    inner class AerobicViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
        var doneImage: ImageView = view.findViewById(R.id.exerciseDoneImage)
        var aerobicBodyLayout: LinearLayout = view.findViewById(R.id.aerobic_workout_body)
        var name: TextView = view.findViewById(R.id.aerobic_element_name)
        var duration: TextView = view.findViewById(R.id.aerobic_element_duration)
        var speed: TextView = view.findViewById(R.id.aerobic_element_speed)
        var intensity: TextView = view.findViewById(R.id.aerobic_element_intensity)
        init {
            view.setTag(this)
            view.setOnClickListener(mOnItemClickListener)
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (myDataset[position].type.equals("Aerobic")) {
            HolderPosition.AEROBIC.ordinal
        } else {
            HolderPosition.WEIGHT.ordinal
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder?
        viewHolder = when (viewType) {
            HolderPosition.WEIGHT.ordinal -> {
                val weightView = LayoutInflater.from(parent.context).inflate(R.layout.element_weight_workout_compact, parent, false)
                WeightViewHolder(weightView)
            }
            HolderPosition.AEROBIC.ordinal -> {
                val aerobicView =
                    LayoutInflater.from(parent.context).inflate(R.layout.element_aerobic_workout_compact, parent, false)
                AerobicViewHolder(aerobicView)
            }
            else -> null
        }
        return viewHolder!!

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (myDataset[position].type.equals("Aerobic")) {
            val aerobicElement: AerobicExerciseModel? = myDataset[position].downcastToAerobic()
            val aerobicHolder = (holder as AerobicViewHolder)
            setViewHolderElement(aerobicHolder.name, aerobicElement?.name)
            setViewHolderElement(aerobicHolder.duration, aerobicElement?.duration)
            setViewHolderElement(aerobicHolder.speed, aerobicElement?.speed)
            setViewHolderElement(aerobicHolder.intensity, aerobicElement?.intensity)
            aerobicElement?.let {
                if(it.done){
                    aerobicHolder.doneImage.visibility = View.VISIBLE
                    aerobicHolder.aerobicBodyLayout.visibility = View.GONE
                }else{
                    aerobicHolder.doneImage.visibility = View.GONE
                    aerobicHolder.aerobicBodyLayout.visibility = View.VISIBLE
                }
            }
        } else {
            val weightElement = myDataset[position].downcastToWeight()
            val weightHolder = (holder as WeightViewHolder)
            setViewHolderElement(weightHolder.name, weightElement?.name)
            setViewHolderElement(weightHolder.weight, weightElement?.weight)
            setViewHolderElement(weightHolder.sets, weightElement?.sets)
            setViewHolderElement(weightHolder.repetitions, weightElement?.repetitions)
            setViewHolderElement(weightHolder.rest, weightElement?.rest)
        }

    }

    private fun setViewHolderElement(
        textView: TextView?,
        field: String?
    ) {
        if (field.isNullOrEmpty()) {
            (textView?.parent as LinearLayout).visibility = View.GONE

        } else {
            (textView?.parent as LinearLayout).visibility = View.VISIBLE
            textView?.text = field
        }
    }

    override fun getItemCount() = myDataset.size

}