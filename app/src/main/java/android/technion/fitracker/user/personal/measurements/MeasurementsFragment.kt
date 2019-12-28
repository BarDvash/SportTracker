package android.technion.fitracker.user.personal.measurements


import android.content.Intent
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.measurements.MeasurementsRecyclerViewAdapter
import android.technion.fitracker.databinding.FragmentMeasurementsBinding
import android.technion.fitracker.models.UserViewModel
import android.technion.fitracker.models.measurements.MeasurementsHistoryModel
import android.technion.fitracker.user.personal.UserActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import kotlin.collections.ArrayList


class MeasurementsFragment : Fragment() {

    private lateinit var navController: NavController

    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var viewModel: UserViewModel
    lateinit var view: FragmentMeasurementsBinding
    val names: ArrayList<String> = ArrayList()
    val values: ArrayList<String> = ArrayList()
    lateinit var adapter: MeasurementsRecyclerViewAdapter
    lateinit var placeHolder: TextView

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

        db.collection("regular_users").document(auth.currentUser!!.uid).collection("measurements")
                .orderBy("data", Query.Direction.DESCENDING).get(Source.CACHE).addOnSuccessListener { it_1 ->
                    if (!it_1.isEmpty) {
                        initFieldsWithLatestMeasurement(it_1)
                    }
                    getLatestFieldsFromDB()
                }.addOnFailureListener {
                    getLatestFieldsFromDB()
                }

        navController = Navigation.findNavController(view)
        (activity as UserActivity).historyAction.setOnMenuItemClickListener {
            val activity = Intent(context, MeasurementsHistoryActivity::class.java)
            startActivity(activity)
            true
        }
        view.findViewById<FloatingActionButton>(R.id.measurements_fab).setOnClickListener {
            (activity as UserActivity).userActivityStartFragment(R.id.measurementsAddFragment,false,true,true)
        }
        //TODO check for placeholder visibility
        val rec_view = view.findViewById<RecyclerView>(R.id.measurements_rec_view)
        rec_view.setHasFixedSize(true)
        rec_view.layoutManager = LinearLayoutManager(context)
        adapter = MeasurementsRecyclerViewAdapter(names,values)
        rec_view.adapter = adapter
        rec_view.addItemDecoration(DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL))
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
                        names.clear()
                        values.clear()
                        adapter.notifyDataSetChanged()
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
        adapter.notifyDataSetChanged()
        ifAllEmpty()
    }

    private fun addValue(name: String, value: String?) {
        if (!value.isNullOrEmpty()) {
            names.add(name)
            values.add(value)
        }
    }

    private fun ifAllEmpty() {
        if (values.isEmpty()){
            placeHolder.visibility = View.VISIBLE
        }
        else{
            placeHolder.visibility = View.GONE
        }
    }
}
