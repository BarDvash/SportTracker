package com.technion.fitracker.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.technion.fitracker.R
import com.technion.fitracker.models.exercise.AerobicExerciseModel
import com.technion.fitracker.models.exercise.ExerciseBaseModel


class ExerciseAdapter(private val myDataset: ArrayList<ExerciseBaseModel>, val mContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class HolderPosition {
        AEROBIC, WEIGHT
    }

    var mOnItemClickListener: View.OnClickListener? = null

    inner class WeightViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
        var container: MaterialCardView = view.findViewById(R.id.weight_ele_container)
        var name: TextView = view.findViewById(R.id.weight_element_name)
        var weight: TextView = view.findViewById(R.id.weight_element_weight)
        var gifPlay: ImageView = view.findViewById(R.id.play_gif_button)
        var sets: TextView = view.findViewById(R.id.weight_element_sets)
        var repetitions: TextView = view.findViewById(R.id.weight_element_repetitions)
        var muscleCategory: TextView = view.findViewById(R.id.weight_element_muscle_category)
        var rest: TextView = view.findViewById(R.id.weight_element_rest)
        var notes: TextView = view.findViewById(R.id.weight_element_notes)

        init {
            view.tag = this
            view.setOnClickListener(mOnItemClickListener)
        }
    }

    inner class AerobicViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
        var container: MaterialCardView = view.findViewById(R.id.aerobic_ele_container)
        var name: TextView = view.findViewById(R.id.aerobic_element_name)
        var duration: TextView = view.findViewById(R.id.aerobic_element_duration)
        var speed: TextView = view.findViewById(R.id.aerobic_element_speed)
        var intensity: TextView = view.findViewById(R.id.aerobic_element_intensity)
        var notes: TextView = view.findViewById(R.id.aerobic_element_notes)

        init {
            view.tag = this
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
                val weightView = LayoutInflater.from(parent.context).inflate(R.layout.element_weight_workout, parent, false)
                WeightViewHolder(weightView)
            }
            HolderPosition.AEROBIC.ordinal -> {
                val aerobicView =
                    LayoutInflater.from(parent.context).inflate(R.layout.element_aerobic_workout, parent, false)
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
            setViewHolderElement(aerobicHolder.notes, aerobicElement?.notes)
            aerobicHolder.container.animation = AnimationUtils.loadAnimation(mContext, R.anim.scale_in_card)
        } else {
            val weightElement = myDataset[position].downcastToWeight()
            val weightHolder = (holder as WeightViewHolder)
            setViewHolderElement(weightHolder.name, weightElement?.name)
            setViewHolderElement(weightHolder.weight, weightElement?.weight)
            setViewHolderElement(weightHolder.sets, weightElement?.sets)
            setViewHolderElement(weightHolder.repetitions, weightElement?.repetitions)
            setViewHolderElement(weightHolder.rest, weightElement?.rest)
            setViewHolderElement(weightHolder.muscleCategory, weightElement?.muscle_category)
            setViewHolderElement(weightHolder.notes, weightElement?.notes)
            if (!weightElement?.gif_url.isNullOrEmpty()) {
                weightHolder.gifPlay.visibility = View.VISIBLE
                weightHolder.gifPlay.setOnClickListener {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
                    val dialog: AlertDialog = builder.create()
                    val inflater = LayoutInflater.from(mContext)
                    val dialogLayout: View = inflater.inflate(R.layout.gif_layout, null)
                    dialog.setView(dialogLayout)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setOnShowListener {
                        val image = dialog.findViewById<ImageView>(R.id.gif_view) as ImageView
                        Glide.with(mContext).load(weightElement?.gif_url)
                                .into(image)
                    }
                    dialog.show()
                }
            } else {
                weightHolder.gifPlay.visibility = View.GONE
                weightHolder.gifPlay.setOnClickListener { }
            }
            weightHolder.container.animation = AnimationUtils.loadAnimation(mContext, R.anim.scale_in_card)
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
            textView.text = field
        }
    }

    override fun getItemCount() = myDataset.size

}