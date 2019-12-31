package com.technion.fitracker.user.personal.workout.edit


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.technion.fitracker.R
import com.technion.fitracker.databinding.FragmentWeightExerciseBinding
import com.technion.fitracker.models.workouts.CreateNewExerciseViewModel


class WeightExerciseFragment : Fragment(), View.OnClickListener {
    lateinit var addExercise: FloatingActionButton
    lateinit var viewModel: CreateNewExerciseViewModel

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
            putExtra("weight", viewModel.weight_weight.value)
            putExtra("sets", viewModel.weight_sets.value)
            putExtra("repetitions", viewModel.weight_repetitions.value)
            putExtra("rest", viewModel.weight_rest.value)
            putExtra("notes", viewModel.weight_notes.value)
        }
    }
}
