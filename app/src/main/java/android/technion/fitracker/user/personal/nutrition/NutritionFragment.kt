package android.technion.fitracker.user.personal.nutrition


import android.content.Intent
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.nutrition.NutritionFireStoreAdapter
import android.technion.fitracker.models.nutrition.NutritionFireStoreModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

var fragmentView: View? = null
var nutrition_recyclerView: RecyclerView? = null

/**
 * A simple [Fragment] subclass.
 */
class NutritionFragment : Fragment(), View.OnClickListener {
    lateinit var mAuth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
//    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FirestoreRecyclerAdapter<NutritionFireStoreModel, NutritionFireStoreAdapter.ViewHolder>
    lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (fragmentView == null){
            fragmentView = inflater.inflate(R.layout.fragment_nutrition, container, false)
        }
        // Inflate the layout for this fragment
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fab = view.findViewById(R.id.nutrition_fab)
        fab.setOnClickListener(this)
        if (nutrition_recyclerView == null) {
            nutrition_recyclerView = view.findViewById(R.id.nutrition_rec_view)
            nutrition_recyclerView!!.setHasFixedSize(true)
            nutrition_recyclerView!!.layoutManager = LinearLayoutManager(context)
            val uid = mAuth.currentUser?.uid
            val query = firestore.collection("regular_users").document(uid!!).collection("meals").orderBy("Name", Query.Direction.ASCENDING)
            val options = FirestoreRecyclerOptions.Builder<NutritionFireStoreModel>().setQuery(query, NutritionFireStoreModel::class.java).build()
            adapter = NutritionFireStoreAdapter(options)
            nutrition_recyclerView!!.adapter = adapter
        }
        adapter = nutrition_recyclerView!!.adapter as FirestoreRecyclerAdapter<NutritionFireStoreModel, NutritionFireStoreAdapter.ViewHolder>
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()

    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.nutrition_fab -> switchToAddActivity()
        }
    }

    private fun switchToAddActivity() {
        val userHome = Intent(context, NutritionAddMealActivity::class.java)
        startActivity(userHome)
    }
}
