package android.technion.fitracker.user.personal.workout


import android.content.Intent
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.databinding.FragmentAerobicExerciseBinding
import android.technion.fitracker.models.workouts.CreateNewExerciseViewModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton


class AerobicExerciseFragment : Fragment(), View.OnClickListener {
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
            DataBindingUtil.inflate<FragmentAerobicExerciseBinding>(inflater, R.layout.fragment_aerobic_exercise, container, false)
        view.myViewModel = viewModel
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addExercise = view.findViewById(R.id.aerobic_done_fab)
        addExercise.setOnClickListener(this)

    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.aerobic_done_fab -> {
                if (viewModel.aerobic_name.value == "") {
                    //TODO : refactor me to underline the field
                    Toast.makeText(v.context, "You must fill at least Name field!", Toast.LENGTH_LONG).show()
                } else {
                    val intent = createIntentWithData()
                    activity?.apply {
                        setResult(CreateNewWorkoutActivity.ResultCodes.AEROBIC.ordinal, intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun createIntentWithData(): Intent {
        return Intent().apply {
            putExtra("name", viewModel.aerobic_name.value)
            putExtra("duration", viewModel.aerobic_duration.value)
            putExtra("speed", viewModel.aerobic_speed.value)
            putExtra("intensity", viewModel.aerobic_intensity.value)
            putExtra("notes", viewModel.aerobic_notes.value)
        }
    }


}
