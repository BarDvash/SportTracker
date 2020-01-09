package com.technion.fitracker.user.personal


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
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
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.technion.fitracker.R
import com.technion.fitracker.adapters.MyTrainerFireStoreAdapter
import com.technion.fitracker.adapters.RecentWorkoutsFireStoreAdapter
import com.technion.fitracker.adapters.UserNotificationsFireStoreAdapter
import com.technion.fitracker.databinding.FragmentHomeScreenBinding
import com.technion.fitracker.models.NotificationsModel
import com.technion.fitracker.models.PersonalTrainer
import com.technion.fitracker.models.UserViewModel
import com.technion.fitracker.models.workouts.RecentWorkoutFireStoreModel
import com.technion.fitracker.user.personal.workout.WorkoutHistoryElementDetails
import com.technion.fitracker.utils.RecyclerCustomItemDecorator


class HomeScreenFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var viewModel: UserViewModel
    lateinit var recentWorkoutsContainer: LinearLayout
    private lateinit var workoutsContentView: MaterialCardView
    private lateinit var personalTrainerContentView: MaterialCardView
    private var shortAnimationDuration: Int = 0

    lateinit var personalTrainerContainer: LinearLayout

    lateinit var notifications_container: LinearLayout
    lateinit var notifications_content_view: MaterialCardView
    lateinit var placeholder: TextView
    private var current_user_id: String? = null


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
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        current_user_id = firebaseAuth.currentUser?.uid
        personalTrainerContainer = view.findViewById(R.id.personal_trainer_container)
        recentWorkoutsContainer = view.findViewById(R.id.recent_workouts_container)
        notifications_content_view = view.findViewById(R.id.user_notifications_card)
        notifications_container = view.findViewById(R.id.user_notifications_container)
        placeholder = view.findViewById(R.id.user_home_placeholder)
        fetchPersonalTrainerUID()
        val notifications_query =
            firebaseFirestore.collection("regular_users").document(current_user_id!!).collection("notifications")
                    .orderBy("notification", Query.Direction.DESCENDING).limit(4)

        val notifications_options =
            FirestoreRecyclerOptions.Builder<NotificationsModel>().setQuery(notifications_query, NotificationsModel::class.java).build()

        viewModel.notifications_adapter = UserNotificationsFireStoreAdapter(notifications_options, this)
        viewModel.notificaations_rec_view = view.findViewById<RecyclerView>(R.id.user_notifications_recycler).apply {
            addItemDecoration(
                RecyclerCustomItemDecorator(context, DividerItemDecoration.VERTICAL)
            )
            layoutManager = LinearLayoutManager(context)
            adapter = viewModel.notifications_adapter
        }

        personalTrainerContentView = view.findViewById(R.id.personal_trainer_card)
        shortAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
        workoutsContentView = view.findViewById(R.id.last_workout_container)
        val query = firebaseFirestore
                .collection("regular_users")
                .document(current_user_id!!)
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
                val exercisesHashMap: ArrayList<HashMap<String, String?>>? = snapshot?.get("exercises") as ArrayList<HashMap<String, String?>>?

                val rating: Long? = snapshot?.get("rating") as Long?
                val time_elapsed: String? = snapshot?.get("time_elapsed") as String?
                val workout_name: String? = snapshot?.get("workout_name") as String?
                val customerView = Intent(context!!, WorkoutHistoryElementDetails::class.java)
                val bundle =
                    bundleOf("id" to snapshot?.id, "comment" to comment, "date_time" to date_time, "exercises" to exercisesHashMap, "rating" to rating, "time_elapsed" to time_elapsed, "workout_name" to workout_name)
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
        personalTrainerContentView.visibility = View.GONE
        notifications_container.visibility = View.GONE
        crossfade()
    }


    fun fetchPersonalTrainerUID() {
        if (viewModel.personalTrainerUID == null) {
            val user_doc = firebaseFirestore.collection("regular_users").document(current_user_id!!)
            user_doc.get().addOnSuccessListener {
                if (it.exists()) {
                    viewModel.personalTrainerUID = it.get("personal_trainer_uid") as String?
                    viewModel.personalTrainerUID?.let {
                        val queryPersonalTrainer = firebaseFirestore
                                .collection("business_users")
                                .whereEqualTo(FieldPath.documentId(), viewModel.personalTrainerUID)
                                .limit(1)
                        val optionsPersonalTrainer = FirestoreRecyclerOptions.Builder<PersonalTrainer>()
                                .setQuery(queryPersonalTrainer, PersonalTrainer::class.java)
                                .build()
                        viewModel.personalTrainerAdapter = MyTrainerFireStoreAdapter(optionsPersonalTrainer, this)
                        viewModel.personalTrainerRV = view?.findViewById<RecyclerView>(R.id.personal_trainer_rv)?.apply {
                            layoutManager = LinearLayoutManager(context)
                            adapter = viewModel.personalTrainerAdapter
                        }
                    }
                    viewModel.personalTrainerAdapter?.startListening()
                }
            }.addOnFailureListener { Toast.makeText(activity, "Lost internet connection", Toast.LENGTH_LONG).show() }//lost internet connection TODO:!
        } else {
            viewModel.personalTrainerUID?.let {
                val queryPersonalTrainer = firebaseFirestore
                        .collection("business_users")
                        .whereEqualTo(FieldPath.documentId(), viewModel.personalTrainerUID)
                        .limit(1)
                val optionsPersonalTrainer = FirestoreRecyclerOptions.Builder<PersonalTrainer>()
                        .setQuery(queryPersonalTrainer, PersonalTrainer::class.java)
                        .build()
                viewModel.personalTrainerAdapter = MyTrainerFireStoreAdapter(optionsPersonalTrainer, this)
                viewModel.personalTrainerRV = view?.findViewById<RecyclerView>(R.id.personal_trainer_rv)?.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = viewModel.personalTrainerAdapter
                }
                viewModel.personalTrainerAdapter?.startListening()
            }
            val user_doc = firebaseFirestore.collection("regular_users").document(current_user_id!!)
            user_doc.get().addOnSuccessListener {
                if (it.exists()) {
                    val trainer_UID = it.get("personal_trainer_uid") as String?
                    if (viewModel.personalTrainerUID != trainer_UID) {
                        viewModel.personalTrainerUID = trainer_UID
                        if(viewModel.personalTrainerUID != null && viewModel.personalTrainerUID != "") {
                            val queryPersonalTrainer = firebaseFirestore
                                    .collection("business_users")
                                    .whereEqualTo(FieldPath.documentId(), viewModel.personalTrainerUID)
                                    .limit(1)
                            val optionsPersonalTrainer = FirestoreRecyclerOptions.Builder<PersonalTrainer>()
                                    .setQuery(queryPersonalTrainer, PersonalTrainer::class.java)
                                    .build()
                            viewModel.personalTrainerAdapter = MyTrainerFireStoreAdapter(optionsPersonalTrainer, this)
                            viewModel.personalTrainerRV = view?.findViewById<RecyclerView>(R.id.personal_trainer_rv)?.apply {
                                layoutManager = LinearLayoutManager(context)
                                adapter = viewModel.personalTrainerAdapter
                            }
                        }
                        viewModel.personalTrainerAdapter?.startListening()
                    }
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        viewModel.homeRecentWorkoutsAdapter?.startListening()
        viewModel.notifications_adapter?.startListening()
    }

    fun setPlaceholder(){
        if(notifications_content_view.isVisible || personalTrainerContentView.isVisible || workoutsContentView.isVisible){
            placeholder.visibility = View.GONE
        }else{
            placeholder.visibility = View.VISIBLE
        }
    }

    private fun crossfade() {
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
        personalTrainerContentView.apply {
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
        // participate in layout passes, etc.)
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
        notifications_content_view.apply {
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
        // participate in layout passes, etc.)
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
}



