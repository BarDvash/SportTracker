package com.technion.fitracker.user.personal.workout.edit


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.technion.fitracker.R
import com.technion.fitracker.databinding.FragmentWeightExerciseBinding
import com.technion.fitracker.models.workouts.CreateNewExerciseViewModel


class WeightExerciseFragment : Fragment(), View.OnClickListener {
    lateinit var addExercise: FloatingActionButton
    lateinit var viewModel: CreateNewExerciseViewModel
    lateinit var nameEditText: AutoCompleteTextView
    lateinit var gifViewButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[CreateNewExerciseViewModel::class.java]
        } ?: throw Exception("Invalid Activity CreateNewExerciseViewModel aerobic fragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            DataBindingUtil.inflate<FragmentWeightExerciseBinding>(inflater, R.layout.fragment_weight_exercise, container, false)
        view.myViewModel = viewModel
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addExercise = view.findViewById(R.id.weight_done_fab)
        addExercise.setOnClickListener(this)
        nameEditText = view.findViewById(R.id.weight_name_input)
        gifViewButton = view.findViewById(R.id.show_gif_button)
        var a: List<String> = viewModel.exerciseDB.values.map { it.map { it.name } }.flatten()
        val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_dropdown_item_1line, a)
        nameEditText.setAdapter(adapter)
        nameEditText.threshold = 1
        nameEditText.doAfterTextChanged {
            if (viewModel.findExercise(viewModel.weight_name.value!!).type.isNullOrEmpty()) {
                gifViewButton.visibility = View.GONE
                viewModel.weight_gif_url = null
            }
        }
        nameEditText.setOnItemClickListener { parent, view, position, id ->
            adapter.getItem(position)?.let {
                var exercise = viewModel.findExercise(it)
                viewModel.weight_muscle_category.set(exercise.type)
                gifViewButton.visibility = View.VISIBLE
                viewModel.weight_gif_url = exercise.gif_url
                gifViewButton.setOnClickListener {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(activity!!)
                    val dialog: AlertDialog = builder.create()
                    val inflater = layoutInflater
                    val dialogLayout: View = inflater.inflate(R.layout.gif_layout, null)
                    dialog.setView(dialogLayout)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setOnShowListener {
                        val image = dialog.findViewById<ImageView>(R.id.gif_view) as ImageView
                        Glide.with(activity!!).load(exercise.gif_url).into(image)
                    }
                    dialog.show()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.weight_done_fab -> {
                if (viewModel.weight_name.value.isNullOrEmpty()) {
                    //TODO : refactor me to underline the field
                    Toast.makeText(v.context, "You must fill at least Name field!", Toast.LENGTH_LONG).show()
                } else {
                    val intent = createIntentWithData()
                    activity?.setResult(CreateNewWorkoutActivity.ResultCodes.WEIGHT.ordinal, intent)
                    activity?.finish()
                }
            }
        }
    }

    private fun createIntentWithData(): Intent {
        return Intent().apply {
            putExtra("name", viewModel.weight_name.value)
            putExtra("muscle_category", viewModel.weight_muscle_category.get())
            putExtra("gif_url", viewModel.weight_gif_url)
            putExtra("weight", viewModel.weight_weight.value)
            putExtra("sets", viewModel.weight_sets.value)
            putExtra("repetitions", viewModel.weight_repetitions.value)
            putExtra("rest", viewModel.weight_rest.value)
            putExtra("notes", viewModel.weight_notes.value)
        }
    }
}
