package android.technion.fitracker.user.personal.workout


import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.ExerciseCompactAdapter
import android.technion.fitracker.databinding.FragmentWorkoutInProgressBinding
import android.technion.fitracker.models.workouts.WorkoutStartViewModel
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class WorkoutInProgress : Fragment(), View.OnClickListener {
    private lateinit var viewModel: WorkoutStartViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var navController: NavController
    private lateinit var viewAdapter: ExerciseCompactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[WorkoutStartViewModel::class.java]
        } ?: throw Exception("Invalid  WorkoutInProgress  fragment ")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            DataBindingUtil.inflate<FragmentWorkoutInProgressBinding>(inflater, R.layout.fragment_workout_in_progress, container, false)
        view.viewModel = viewModel
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        val appCompatActivity = (activity as AppCompatActivity)
        appCompatActivity.setSupportActionBar(view.findViewById(R.id.workout_in_progress_toolbar))
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        appCompatActivity.supportActionBar?.title = viewModel.workoutName.value
        viewManager = LinearLayoutManager(context)
        val onItemClickListener = View.OnClickListener { v ->
            val viewHolder = v.tag as RecyclerView.ViewHolder
            val model = viewModel.workoutExercises.value?.get(viewHolder.adapterPosition)
            model?.let {
                it.done = !it.done
                if (it.done) {
                    if (it.type == "Weight") {
                        (viewHolder as ExerciseCompactAdapter.WeightViewHolder).doneImage.visibility = View.VISIBLE
                        viewHolder.weightBodyLayout.visibility = View.GONE
                    } else {
                        (viewHolder as ExerciseCompactAdapter.AerobicViewHolder).doneImage.visibility = View.VISIBLE
                        viewHolder.aerobicBodyLayout.visibility = View.GONE
                    }

                } else {
                    if (it.type == "Weight") {
                        (viewHolder as ExerciseCompactAdapter.WeightViewHolder).doneImage.visibility = View.GONE
                        viewHolder.weightBodyLayout.visibility = View.VISIBLE
                    } else {
                        (viewHolder as ExerciseCompactAdapter.AerobicViewHolder).doneImage.visibility = View.GONE
                        viewHolder.aerobicBodyLayout.visibility = View.VISIBLE
                    }
                }
            }

        }
        val exercises = viewModel.workoutExercises.value
        viewAdapter = ExerciseCompactAdapter(exercises!!).apply {
            mOnItemClickListener = onItemClickListener
        }
        recyclerView = activity?.findViewById<RecyclerView>(R.id.workout_in_progress_recycle).apply {
            this?.setHasFixedSize(true)
            this?.layoutManager = viewManager
            this?.adapter = viewAdapter

        }!!
        fab = activity?.findViewById(R.id.finish_workout_fab)!!
        fab.setOnClickListener(this)
        viewAdapter.notifyDataSetChanged()
    }

    private fun startFragmentAndPop(id: Int) {
        navController.popBackStack()
        navController.navigate(id)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                startFragmentAndPop(R.id.workoutStartScreen)
            }
        }
        return true
    }


    override fun onClick(v: View?) {
        //TODO: to result, stop workout, stop timer,
    }
}
