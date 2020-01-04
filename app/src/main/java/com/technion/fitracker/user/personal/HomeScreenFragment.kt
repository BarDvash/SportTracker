package com.technion.fitracker.user.personal


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.technion.fitracker.R
import com.technion.fitracker.adapters.RecentWorkoutsFireStoreAdapter
import com.technion.fitracker.models.workouts.RecentWorkoutFireStoreModel
import com.technion.fitracker.utils.RecyclerCustomItemDecorator


class HomeScreenFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    lateinit var recentWorkoutsContainer: LinearLayout
    lateinit var personalTrainerContainer: LinearLayout
    lateinit var personalTrainerImageView: ImageView
    lateinit var personalTrainerNameView: TextView

    lateinit var adapter: FirestoreRecyclerAdapter<RecentWorkoutFireStoreModel, RecentWorkoutsFireStoreAdapter.ViewHolder>
    private var current_user_id: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        super.onViewCreated(view, savedInstanceState)
        current_user_id = FirebaseAuth.getInstance().currentUser?.uid
        recentWorkoutsContainer = view.findViewById(R.id.recent_workouts_container)

        personalTrainerContainer = view.findViewById(R.id.personal_trainer_container)
        personalTrainerImageView = view.findViewById(R.id.hone_screen_personal_trainer_image_view)
        personalTrainerNameView = view.findViewById(R.id.home_screen_personal_trainer_name)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        recyclerView = view.findViewById(R.id.last_workouts_recycler)
        recyclerView.addItemDecoration(
            RecyclerCustomItemDecorator(context, DividerItemDecoration.VERTICAL)
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = LinearLayoutManager(context)
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
        adapter = RecentWorkoutsFireStoreAdapter(options, this)
        recyclerView.adapter = adapter
        setPersonalTrainerCard()
    }

    override fun onStart() {
        super.onStart()
        setPersonalTrainerCard()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }


    fun setPersonalTrainerCard() {
        val user_doc = firebaseFirestore.collection("regular_users").document(current_user_id!!)
        user_doc.get().addOnSuccessListener {
            if (it.exists()) {
                var personal_trainer_uid = it.get("personal_trainer_uid") as String?
                if (personal_trainer_uid != null) {
                    val personal_trainer_doc = firebaseFirestore.collection("business_users").document(personal_trainer_uid)
                    personal_trainer_doc.get().addOnSuccessListener {
                        if (it.exists()) {
                            var personal_trainer_name = it.get("name") as String?
                            var personal_trainer_photo = it.get("photoURL") as String?

                            personalTrainerNameView.text = personal_trainer_name
                            if (personal_trainer_photo.isNullOrEmpty()) {
                                Glide.with(activity!!) //1
                                        .load(personal_trainer_photo)
                                        .placeholder(R.drawable.user_avatar)
                                        .error(R.drawable.user_avatar)
                                        .skipMemoryCache(true) //2
                                        .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                                        .transform(CircleCrop()) //4
                                        .into(personalTrainerImageView)
                            }

                            personalTrainerContainer.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }.addOnFailureListener { Toast.makeText(activity, "Lost internet connection", Toast.LENGTH_LONG).show() }//lost internet connection TODO:!
    }
}
