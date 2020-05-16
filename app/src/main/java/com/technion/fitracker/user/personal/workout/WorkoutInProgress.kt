package com.technion.fitracker.user.personal.workout


import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Chronometer
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.technion.fitracker.R
import com.technion.fitracker.adapters.ExerciseCompactAdapter
import com.technion.fitracker.databinding.FragmentWorkoutInProgressBinding
import com.technion.fitracker.models.exercise.WeightExerciseModel
import com.technion.fitracker.models.workouts.WorkoutStartViewModel

class WorkoutInProgress : Fragment(), View.OnClickListener {
    private lateinit var viewModel: WorkoutStartViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var finish_fab: FloatingActionButton
    private lateinit var play_stop_fab: FloatingActionButton
    private lateinit var navController: NavController
    private lateinit var viewAdapter: ExerciseCompactAdapter
    private lateinit var chrono: Chronometer

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
                        (viewHolder as ExerciseCompactAdapter.WeightViewHolder).doneImage.setImageResource(R.drawable.ic_tick_inside_circle)
                        viewHolder.doneImage.setOnClickListener { null }
                        viewHolder.doneImage.visibility = View.VISIBLE
                        viewHolder.doneImage.animation = AnimationUtils.loadAnimation(context!!, R.anim.reveal)
                        viewHolder.weightBodyLayout.animation = AnimationUtils.loadAnimation(context!!, R.anim.hide) //
                        viewHolder.weightBodyLayout.visibility = View.GONE
                    } else {
                        (viewHolder as ExerciseCompactAdapter.AerobicViewHolder).doneImage.visibility = View.VISIBLE
                        viewHolder.doneImage.animation = AnimationUtils.loadAnimation(context!!, R.anim.reveal)
                        viewHolder.aerobicBodyLayout.animation = AnimationUtils.loadAnimation(context!!, R.anim.hide)
                        viewHolder.aerobicBodyLayout.visibility = View.GONE
                    }
                    model.time_done = chrono.text.toString()
                } else {
                    if (it.type == "Weight") {
                        val weightExerciseModel = it as WeightExerciseModel
                        if(!weightExerciseModel.gif_url.isNullOrEmpty()) {
                            (viewHolder as ExerciseCompactAdapter.WeightViewHolder).doneImage.setImageResource(R.drawable.ic_brand)
                            viewHolder.doneImage.setOnClickListener {
                                val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                                val dialog: AlertDialog = builder.create()
                                val inflater = LayoutInflater.from(requireContext())
                                val dialogLayout: View = inflater.inflate(R.layout.gif_layout, null)
                                dialog.setView(dialogLayout)
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                dialog.setOnShowListener {
                                    val image = dialog.findViewById<ImageView>(R.id.gif_view) as ImageView
                                    Glide.with(requireContext()).load(weightExerciseModel.gif_url)
                                            .into(image)
                                }
                                dialog.show()
                            }
                            viewHolder.doneImage.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.reveal)
                            viewHolder.weightBodyLayout.visibility = View.VISIBLE
                            viewHolder.weightBodyLayout.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_in_card)
                        }else{
                            (viewHolder as ExerciseCompactAdapter.WeightViewHolder).doneImage.visibility = View.GONE
                            viewHolder.doneImage.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.hide)
                            viewHolder.doneImage.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.hide)
                            viewHolder.weightBodyLayout.visibility = View.VISIBLE
                            viewHolder.weightBodyLayout.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_in_card)
                        }

                    } else {
                        (viewHolder as ExerciseCompactAdapter.AerobicViewHolder).doneImage.visibility = View.GONE
                        viewHolder.doneImage.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.hide)
                        viewHolder.aerobicBodyLayout.visibility = View.VISIBLE
                        viewHolder.aerobicBodyLayout.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_in_card)
                    }
                    model.time_done = null
                }
            }

        }
        val exercises = viewModel.workoutExercises.value
        viewAdapter = ExerciseCompactAdapter(exercises!!, requireContext()).apply {
            mOnItemClickListener = onItemClickListener
        }
        recyclerView = activity?.findViewById<RecyclerView>(R.id.workout_in_progress_recycle)?.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        }!!
        activity?.let {
            chrono = it.findViewById(R.id.workout_stopwatch)
            finish_fab = it.findViewById(R.id.finish_fab)
            play_stop_fab = it.findViewById(R.id.play_stop_fab)
        }
        finish_fab.setOnClickListener(this)
        play_stop_fab.setOnClickListener(this)

        viewAdapter.notifyDataSetChanged()
        viewModel.stopwatch = 0
        chrono.base = SystemClock.elapsedRealtime()
        chrono.start()
        viewModel.started = true
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


    private fun finishedAllExercises(): Boolean {
        viewModel.workoutExercises.value?.let {
            for (exercise in it) {
                if (!exercise.done) {
                    return false
                }
            }
        }
        return true
    }

    private fun stopChrono() {
        viewModel.stopwatch = SystemClock.elapsedRealtime() - chrono.base
        chrono.stop()
    }

    private fun startChrono() {
        chrono.base = SystemClock.elapsedRealtime() - viewModel.stopwatch
        chrono.start()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.play_stop_fab -> {
                if (viewModel.started) {
                    play_stop_fab.setImageResource(R.drawable.ic_play_button_arrowhead)
                    stopChrono()
                } else {
                    play_stop_fab.setImageResource(R.drawable.ic_pause_button)
                    startChrono()
                }
                viewModel.started = !viewModel.started
            }
            R.id.finish_fab -> {
                if (finishedAllExercises()) {
                    stopChrono()
                    viewModel.timeElapsed.value = chrono.text.toString()
                    startFragmentAndPop(R.id.workoutSummaryScreen)
                } else {
                    MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Warning")
                            .setMessage("You haven't finished all exercises, finish the workout anyway?")
                            .setPositiveButton(
                                    "Yes"
                            ) { _, _ ->
                                viewModel.started = false
                                stopChrono()
                                viewModel.timeElapsed.value = chrono.text.toString()
                                startFragmentAndPop(R.id.workoutSummaryScreen)
                            }
                            .setNegativeButton(
                                    "No"
                            ) { _, _ ->
                            }.show()
                }
            }
        }
    }
}
