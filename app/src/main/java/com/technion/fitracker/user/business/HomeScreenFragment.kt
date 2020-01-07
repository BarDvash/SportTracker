package com.technion.fitracker.user.business


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.technion.fitracker.R
import com.technion.fitracker.adapters.BusinessNotificationsFireStoreAdapter
import com.technion.fitracker.models.BusinessUserViewModel
import com.technion.fitracker.models.NotificationsModel
import com.technion.fitracker.utils.RecyclerCustomItemDecorator


class HomeScreenFragment : Fragment() {

    lateinit var viewModel: BusinessUserViewModel


    private lateinit var navController: NavController
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var notifications_container: LinearLayout
    lateinit var notifications_content_view: MaterialCardView
    private var shortAnimationDuration: Int = 0

    private var current_user_id: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[BusinessUserViewModel::class.java]
        } ?: throw Exception("Invalid Fragment, customers fragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_business_home_screen, container, false)


    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        current_user_id = firebaseAuth.currentUser?.uid

        notifications_container = view.findViewById(R.id.business_user_notifications_container)
        notifications_content_view = view.findViewById(R.id.business_user_notifications_card)

        shortAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)

        val query = firebaseFirestore.collection("business_users").document(current_user_id!!).collection("notifications").orderBy("notification", Query.Direction.DESCENDING).limit(5)

        val options = FirestoreRecyclerOptions.Builder<NotificationsModel>().setQuery(query, NotificationsModel::class.java).build()

        viewModel.notifications_adapter = BusinessNotificationsFireStoreAdapter(options,this )
        viewModel.notificaations_rec_view = view.findViewById<RecyclerView>(R.id.business_user_notifications_recycler).apply {
            addItemDecoration(
                RecyclerCustomItemDecorator(context, DividerItemDecoration.VERTICAL)
            )
            layoutManager = LinearLayoutManager(context)
            adapter = viewModel.notifications_adapter
        }
        notifications_content_view.visibility = View.GONE
        crossfade()
    }


    private fun crossfade() {
        notifications_container.apply {
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
    }






    override fun onStart() {
        super.onStart()
        viewModel.notifications_adapter?.startListening()
    }

}





