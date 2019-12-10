package android.technion.fitracker.user.personal.nutrition


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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

var fragmentView: View? = null
var recyclerView: RecyclerView? = null

/**
 * A simple [Fragment] subclass.
 */
class NutritionFragment : Fragment() {
    lateinit var mAuth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
//    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FirestoreRecyclerAdapter<NutritionFireStoreModel, NutritionFireStoreAdapter.ViewHolder>

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
        if (recyclerView == null) {
            recyclerView = view.findViewById(R.id.nutrition_rec_view)
            recyclerView!!.setHasFixedSize(true)
            recyclerView!!.layoutManager = LinearLayoutManager(context)
            val uid = mAuth.currentUser?.uid
            val query = firestore.collection("users").document(uid!!).collection("meals").orderBy("Name", Query.Direction.ASCENDING)
            val options = FirestoreRecyclerOptions.Builder<NutritionFireStoreModel>().setQuery(query, NutritionFireStoreModel::class.java).build()
            adapter = NutritionFireStoreAdapter(options)
            recyclerView!!.adapter = adapter
        }
        adapter = recyclerView!!.adapter as FirestoreRecyclerAdapter<NutritionFireStoreModel, NutritionFireStoreAdapter.ViewHolder>
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()

    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()

    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
