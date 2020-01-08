package com.technion.fitracker.user.business.customer


import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.technion.fitracker.R
import com.technion.fitracker.adapters.RecentWorkoutsFireStoreAdapter
import com.technion.fitracker.adapters.measurements.MeasurementsRecyclerViewAdapter
import com.technion.fitracker.databinding.FragmentCustomerMeasurementBinding
import com.technion.fitracker.models.CustomerDataViewModel
import com.technion.fitracker.models.measurements.MeasurementsHistoryModel
import com.technion.fitracker.models.workouts.RecentWorkoutFireStoreModel
import com.technion.fitracker.user.personal.workout.WorkoutHistoryElementDetails
import com.technion.fitracker.utils.RecyclerCustomItemDecorator
import java.text.SimpleDateFormat

/**
 * A simple [Fragment] subclass.
 */
class CustomerMeasurementFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var viewModel: CustomerDataViewModel
    val names: ArrayList<String> = ArrayList()
    val values: ArrayList<String> = ArrayList()
    lateinit var placeHolder: TextView
    lateinit var measurementsContainer: MaterialCardView
    val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
    val newDateFormat = SimpleDateFormat("dd-MMMM-yyyy HH:mm")
    private lateinit var workoutsContentView: MaterialCardView
    lateinit var recentWorkoutsContainer: LinearLayout

    private var shortAnimationDuration: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[CustomerDataViewModel::class.java]
        } ?: throw Exception("Invalid CustomerMeasurementFragment in activity!")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            DataBindingUtil.inflate<FragmentCustomerMeasurementBinding>(inflater, R.layout.fragment_customer_measurement, container, false)
        view.viewmodel = viewModel
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        names.clear()
        values.clear()
        recentWorkoutsContainer = view.findViewById(R.id.recent_workouts_container)

        shortAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
        db.collection("regular_users").document(viewModel.customerID!!).collection("measurements")
                .orderBy("data", Query.Direction.DESCENDING).get(Source.CACHE).addOnSuccessListener { innerIt ->
                    if (!innerIt.isEmpty) {
                        initFieldsWithLatestMeasurement(innerIt)
                    }
                    getLatestFieldsFromDB()
                }.addOnFailureListener {
                    getLatestFieldsFromDB()
                }
        measurementsContainer = view.findViewById<MaterialCardView>(R.id.customer_last_measure_container)
        measurementsContainer.visibility = View.GONE
        viewModel.measurementRV  = view.findViewById<RecyclerView>(R.id.customer_measurements_rec_view)
        viewModel.measurementRV?.layoutManager = LinearLayoutManager(context)
        viewModel.measurementsRVAdapter = MeasurementsRecyclerViewAdapter(names, values,viewModel.customerID)
        viewModel.measurementRV?.adapter = viewModel.measurementsRVAdapter
        viewModel.measurementRV?.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        placeHolder = view.findViewById(R.id.customer_measurements_placeholder)

        workoutsContentView = view.findViewById(R.id.last_workout_container)
        val query = db
                .collection("regular_users")
                .document(viewModel.customerID!!)
                .collection("workouts_history")
                .orderBy("date_time", Query.Direction.DESCENDING)
                .limit(5)
        val options = FirestoreRecyclerOptions.Builder<RecentWorkoutFireStoreModel>()
                .setQuery(query, RecentWorkoutFireStoreModel::class.java)
                .build()
        viewModel.homeRecentWorkoutsAdapter = RecentWorkoutsFireStoreAdapter(options, this).apply {
            mOnItemClickListener = View.OnClickListener { v ->
                val rvh = v.tag as RecentWorkoutsFireStoreAdapter.ViewHolder
                val snapshot = viewModel.homeRecentWorkoutsAdapter?.snapshots?.getSnapshot(rvh.adapterPosition)
                val comment: String? = snapshot?.get("comment") as String?
                val date_time: String? = snapshot?.get("date_time") as String?
                val exercisesHashMap: ArrayList<HashMap<String, String?>>? = snapshot?.get("exercises") as ArrayList<HashMap<String,String?>>?
                val rating: Long? = snapshot?.get("rating") as Long?
                val time_elapsed: String? = snapshot?.get("time_elapsed") as String?
                val workout_name: String? = snapshot?.get("workout_name") as String?
                val customerView = Intent(context!!, WorkoutHistoryElementDetails::class.java)
                val bundle = bundleOf("userID" to viewModel.customerID, "id" to snapshot?.id,"comment" to comment, "date_time" to date_time, "exercises" to exercisesHashMap, "rating" to rating, "time_elapsed" to time_elapsed, "workout_name" to workout_name)
                customerView.putExtras(bundle)
                startActivity(customerView)
            }
        }
        viewModel.homeRecentWorkoutRV = view.findViewById<RecyclerView>(R.id.last_workouts_recycler).apply {
            addItemDecoration(
                RecyclerCustomItemDecorator(context, DividerItemDecoration.VERTICAL)
            )
            layoutManager = LinearLayoutManager(context)
            adapter = viewModel.homeRecentWorkoutsAdapter
        }



        workoutsContentView.visibility = View.GONE
        crossfade()

    }

    override fun onStart() {
        super.onStart()
        viewModel.homeRecentWorkoutsAdapter?.startListening()
    }

    private fun getLatestFieldsFromDB() {
        db.collection("regular_users").document(viewModel.customerID!!).collection("measurements")
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
        workoutsContentView.apply {
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
