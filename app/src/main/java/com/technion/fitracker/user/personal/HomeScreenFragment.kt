package com.technion.fitracker.user.personal


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.technion.fitracker.R
import com.technion.fitracker.adapters.RecentWorkoutsFireStoreAdapter
import com.technion.fitracker.databinding.FragmentHomeScreenBinding
import com.technion.fitracker.models.UserViewModel
import com.technion.fitracker.models.workouts.RecentWorkoutFireStoreModel
import com.technion.fitracker.utils.RecyclerCustomItemDecorator


class HomeScreenFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var viewModel: UserViewModel
    lateinit var recentWorkoutsContainer: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[UserViewModel::class.java]
        } ?: throw Exception("Invalid Fragment, HomeScreenFragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            DataBindingUtil.inflate<FragmentHomeScreenBinding>(inflater, R.layout.fragment_home_screen, container, false)
        view.viewmodel = viewModel
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        super.onViewCreated(view, savedInstanceState)
        recentWorkoutsContainer = view.findViewById(R.id.recent_workouts_container)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.currentUser?.uid

        val query = firebaseFirestore
                .collection("regular_users")
                .document(uid!!)
                .collection("workouts_history")
                .orderBy("date_time", Query.Direction.DESCENDING)
                .limit(5)
        val options = FirestoreRecyclerOptions.Builder<RecentWorkoutFireStoreModel>()
                .setQuery(query, RecentWorkoutFireStoreModel::class.java)
                .build()
        viewModel.homeAdapter = RecentWorkoutsFireStoreAdapter(options, this)
        viewModel.homeRV = view.findViewById<RecyclerView>(R.id.last_workouts_recycler).apply {
            addItemDecoration(
                RecyclerCustomItemDecorator(context, DividerItemDecoration.VERTICAL)
            )
            layoutManager = LinearLayoutManager(context)
            adapter = viewModel.homeAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.homeAdapter?.startListening()
    }


}
