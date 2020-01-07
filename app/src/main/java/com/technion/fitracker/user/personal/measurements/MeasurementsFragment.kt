package com.technion.fitracker.user.personal.measurements


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.view.ViewCompat.animate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.technion.fitracker.R
import com.technion.fitracker.adapters.measurements.MeasurementsRecyclerViewAdapter
import com.technion.fitracker.databinding.FragmentMeasurementsBinding
import com.technion.fitracker.models.UserViewModel
import com.technion.fitracker.models.measurements.MeasurementsHistoryModel
import com.technion.fitracker.user.personal.UserActivity
import com.technion.fitracker.utils.RecyclerViewDisableScroll
import java.text.SimpleDateFormat


class MeasurementsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var viewModel: UserViewModel
    lateinit var view: FragmentMeasurementsBinding
    val names: ArrayList<String> = ArrayList()
    val values: ArrayList<String> = ArrayList()
    lateinit var placeHolder: TextView
    lateinit var measurementsContainer: MaterialCardView
    lateinit var fab: ExtendedFloatingActionButton
    val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
    val newDateFormat = SimpleDateFormat("dd-MMMM-yyyy HH:mm")
    private var shortAnimationDuration: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[UserViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view =
            DataBindingUtil.inflate(inflater, R.layout.fragment_measurements, container, false)
        view.viewmodel = viewModel
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        names.clear()
        values.clear()
        shortAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
        db.collection("regular_users").document(auth.currentUser!!.uid).collection("measurements")
                .orderBy("data", Query.Direction.DESCENDING).get(Source.CACHE).addOnSuccessListener { it_1 ->
                    if (!it_1.isEmpty) {
                        initFieldsWithLatestMeasurement(it_1)
                    }
                    getLatestFieldsFromDB()
                }.addOnFailureListener {
                    getLatestFieldsFromDB()
                }

        (activity as UserActivity).historyAction.setOnMenuItemClickListener {
            val activity = Intent(context, MeasurementsHistoryActivity::class.java)
            startActivity(activity)
            true
        }
        fab = view.findViewById<ExtendedFloatingActionButton>(R.id.measurements_fab).apply {
            setOnClickListener {
                (activity as UserActivity).userActivityStartFragment(R.id.measurementsAddFragment, false, true, true)
            }
            animation = AnimationUtils.loadAnimation(context!!, R.anim.scale_in_card)
        }
        measurementsContainer = view.findViewById<MaterialCardView>(R.id.last_measure_container)
        measurementsContainer.visibility = View.GONE
        viewModel.measurementRV  = view.findViewById<RecyclerView>(R.id.measurements_rec_view)
        viewModel.measurementRV?.layoutManager = LinearLayoutManager(context)
        viewModel.measurementsRVAdapter = MeasurementsRecyclerViewAdapter(names, values)
        viewModel.measurementRV?.adapter = viewModel.measurementsRVAdapter
        viewModel.measurementRV?.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        viewModel.measurementRV?.addOnItemTouchListener(RecyclerViewDisableScroll())
        viewModel.measurementRV?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    if (fab.isShown) {
                        fab.hide()
                    }
                } else if (dy < 0) {
                    if (!fab.isShown) {
                        fab.show()
                    }
                }
            }
        })
        placeHolder = view.findViewById(R.id.measurements_placeholder)

    }

    private fun getLatestFieldsFromDB() {
        db.collection("regular_users").document(auth.currentUser!!.uid).collection("measurements")
                .orderBy("data", Query.Direction.DESCENDING).get().addOnSuccessListener {
                    if (!it.isEmpty) {
                        initFieldsWithLatestMeasurement(it)
                    } else {
                        viewModel.editTextBiceps.value = ""
                        viewModel.editTextBodyFat.value = ""
                        viewModel.editTextChest.value = ""
                        viewModel.editTextHips.value = ""
                        viewModel.editTextWaist.value = ""
                        viewModel.editTextWeight.value = ""
//                        viewModel.textViewData.value = ""
                        viewModel.textViewDate.set("")
                        names.clear()
                        values.clear()
                        viewModel.measurementsRVAdapter?.notifyDataSetChanged()
                        ifAllEmpty()
                    }
                }
    }

    private fun initFieldsWithLatestMeasurement(it: QuerySnapshot) {
        val lastRes = it.first().toObject(MeasurementsHistoryModel::class.java)
        names.clear()
        values.clear()
        addValue("Biceps", lastRes.biceps)
        addValue("Body fat", lastRes.body_fat)
        addValue("Chest", lastRes.chest)
        addValue("Hips", lastRes.hips)
        addValue("Waist", lastRes.waist)
        addValue("Weight", lastRes.weight)
        viewModel.editTextBiceps.value = lastRes.biceps
        viewModel.editTextBodyFat.value = lastRes.body_fat
        viewModel.editTextChest.value = lastRes.chest
        viewModel.editTextHips.value = lastRes.hips
        viewModel.editTextWaist.value = lastRes.waist
        viewModel.editTextWeight.value = lastRes.weight

        val date = dateFormat.parse(lastRes.data!!)
//        viewModel.textViewData.value = newDateFormat.format(date!!)
        viewModel.textViewDate.set(newDateFormat.format(date!!))

        viewModel.measurementsRVAdapter?.notifyDataSetChanged()
        ifAllEmpty()
    }

    private fun addValue(name: String, value: String?) {
        if (!value.isNullOrEmpty()) {
            names.add(name)
            values.add(value)
        }
    }

    private fun crossfade() {
        measurementsContainer.apply {
            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            alpha = 0f
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            animate()
                    .alpha(1f)
                    .setDuration(shortAnimationDuration.toLong())
                    .setListener(null)
        }
        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
    }

    private fun ifAllEmpty() {
        if (values.isEmpty()) {
            placeHolder.visibility = View.VISIBLE
            measurementsContainer.visibility = View.GONE
        } else {
            placeHolder.visibility = View.GONE
            if(measurementsContainer.visibility == View.GONE){
                crossfade()
            }
        }
    }


}
