package android.technion.fitracker.user.personal.workout


import android.content.Intent
import android.os.Bundle
import android.technion.fitracker.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton


class WeightExerciseFragment : Fragment(), View.OnClickListener {
    lateinit var addExercise: FloatingActionButton
    lateinit var name: EditText
    lateinit var weight: EditText
    lateinit var sets: EditText
    lateinit var repetitions: EditText
    lateinit var rest: EditText
    lateinit var notes: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_weight_exercise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        name = view.findViewById(R.id.weight_name_input)
        weight = view.findViewById(R.id.weight_weight_input)
        sets = view.findViewById(R.id.weight_sets_input)
        repetitions = view.findViewById(R.id.weight_repetitions_input)
        rest = view.findViewById(R.id.weight_set_rest_input)
        notes = view.findViewById(R.id.weight_notes_input)
        addExercise = view.findViewById(R.id.weight_done_fab)
        addExercise.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.weight_done_fab -> {
                if (name.text.toString() == "") {
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
        val intent = Intent()
        intent.putExtra("name", name.text.toString())
        intent.putExtra("weight", weight.text.toString())
        intent.putExtra("sets", sets.text.toString())
        intent.putExtra("repetitions", repetitions.text.toString())
        intent.putExtra("rest", rest.text.toString())
        intent.putExtra("notes", notes.text.toString())
        return intent
    }


}
