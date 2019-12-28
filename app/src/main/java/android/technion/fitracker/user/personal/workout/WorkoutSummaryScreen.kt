package android.technion.fitracker.user.personal.workout


import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.ExerciseSummaryAdapter
import android.technion.fitracker.databinding.FragmentWorkoutSummaryScreenBinding
import android.technion.fitracker.models.workouts.WorkoutStartViewModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_workout_in_progress.*


class WorkoutSummaryScreen : Fragment(), View.OnClickListener {
    private lateinit var viewModel: WorkoutStartViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ExerciseSummaryAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var saveButton: MaterialButton
    private lateinit var discardButton: MaterialButton
    private lateinit var navController: NavController
    private lateinit var commentField: TextInputEditText
    private lateinit var timeElapsed: TextView
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[WorkoutStartViewModel::class.java]
        } ?: throw Exception("Invalid WorkoutSummaryScreen fragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = DataBindingUtil.inflate<FragmentWorkoutSummaryScreenBinding>(inflater, R.layout.fragment_workout_summary_screen, container, false)
        view.viewModel = viewModel
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        val appCompatActivity = (activity as AppCompatActivity)
        appCompatActivity.setSupportActionBar(view.findViewById(R.id.workout_summary_toolbar))
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewAdapter = ExerciseSummaryAdapter(viewModel.workoutExercises.value!!)
        viewManager = LinearLayoutManager(context)
        //TODO: handle click on ratings
        recyclerView = activity?.findViewById<RecyclerView>(R.id.workout_summary_recycler)?.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }!!
        activity?.let {
            saveButton = it.findViewById(R.id.workout_summary_save)
            discardButton = it.findViewById(R.id.workout_summary_discard)
            commentField = it.findViewById(R.id.workout_summary_comment)
            timeElapsed = it.findViewById(R.id.time_elapsed)
        }
        saveButton.setOnClickListener(this)
        discardButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.workout_summary_save -> {
                //TODO:
            }
            R.id.workout_summary_discard -> {
                MaterialAlertDialogBuilder(activity).setTitle("Warning").setMessage("Discard workout result?")
                        .setPositiveButton(
                            "Yes"
                        ) { _, _ ->
                            activity?.finish()

                        }
                        .setNegativeButton(
                            "No"
                        ) { _, _ ->
                        }.show()
            }

        }
    }
}
