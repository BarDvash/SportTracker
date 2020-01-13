package com.technion.fitracker.user.personal.measurements

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.fitracker.R
import com.technion.fitracker.databinding.FragmentMeasurementAddBinding
import com.technion.fitracker.models.UserViewModel
import com.technion.fitracker.models.measurements.MeasurementsHistoryModel
import com.technion.fitracker.user.personal.UserActivity
import java.text.SimpleDateFormat
import java.util.*

class MeasurementsAddFragment : Fragment() {
    private lateinit var navController: NavController

    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var viewModel: UserViewModel
    lateinit var view: FragmentMeasurementAddBinding

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
            DataBindingUtil.inflate(inflater, R.layout.fragment_measurement_add, container, false)
        view.viewmodel = viewModel
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        navController = Navigation.findNavController(view)
        (activity as UserActivity).historyAction.setOnMenuItemClickListener {
            val activity = Intent(context, MeasurementsGraphActivity::class.java)
            startActivity(activity)
            true
        }
        (activity as UserActivity).addAction.setOnMenuItemClickListener {
            if (allEmpty()) {
                true
            }
            val s = SimpleDateFormat("yyyyMMddHHmmss")
            val timeStamp: String = s.format(Date())
            val data = MeasurementsHistoryModel(
                    viewModel.editTextBiceps.value, viewModel.editTextChest.value, timeStamp, viewModel.editTextBodyFat.value
                    , viewModel.editTextHips.value, viewModel.editTextWaist.value, viewModel.editTextWeight.value
            )
            db.collection("regular_users").document(auth.currentUser!!.uid).collection("measurements").add(data)
                    .addOnSuccessListener {
                        //                        Toast.makeText(context, getString(R.string.added_success), Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        //                        Toast.makeText(context, getString(R.string.add_failure), Toast.LENGTH_SHORT).show()
                    }
            (activity as UserActivity).userActivityPopBackStack(false, false, true)
            true
        }
    }


    private fun allEmpty(): Boolean {
        return viewModel.editTextBiceps.value.isNullOrEmpty() && viewModel.editTextChest.value.isNullOrEmpty() &&
                viewModel.editTextBodyFat.value.isNullOrEmpty() && viewModel.editTextHips.value.isNullOrEmpty() &&
                viewModel.editTextWaist.value.isNullOrEmpty() && viewModel.editTextWeight.value.isNullOrEmpty()
    }
}