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


class AerobicExerciseFragment : Fragment(), View.OnClickListener {
    lateinit var addExercise: FloatingActionButton
    lateinit var name: EditText
    lateinit var duration: EditText
    lateinit var speed: EditText
    lateinit var intensity: EditText
    lateinit var notes: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_aerobic_exercise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        name = view.findViewById(R.id.aerobic_name_input)
        duration = view.findViewById(R.id.aerobic_duration_input)
        speed = view.findViewById(R.id.aerobic_speed_input)
        intensity = view.findViewById(R.id.aerobic_intensity_input)
        notes = view.findViewById(R.id.aerobic_notes_input)
        addExercise = view.findViewById(R.id.aerobic_done_fab)
        addExercise.setOnClickListener(this)

    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.aerobic_done_fab -> {
                if (name.text.toString() == "") {
                    //TODO : refactor me to underline the field
                    Toast.makeText(v.context, "You must fill at least Name field!", Toast.LENGTH_LONG).show()
                } else {
                    val intent = createIntentWithData()
                    activity?.setResult(CreateNewWorkoutActivity.ResultCodes.AEROBIC.ordinal, intent)
                    activity?.finish()
                }
            }
        }
    }

    private fun createIntentWithData(): Intent {
        val intent = Intent()
        intent.putExtra("name", name.text.toString())
        intent.putExtra("duration", duration.text.toString())
        intent.putExtra("speed", speed.text.toString())
        intent.putExtra("intensity", intensity.text.toString())
        intent.putExtra("notes", notes.text.toString())
        return intent
    }


}
