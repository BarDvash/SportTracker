package android.technion.fitracker.user.personal.nutrition


import android.content.Intent
import android.database.DataSetObserver
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.adapters.nutrition.NutritionFireStoreAdapter
import android.technion.fitracker.models.nutrition.NutritionFireStoreModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.databinding.ObservableArrayList
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * A simple [Fragment] subclass.
 */
class NutritionFragment : Fragment(), View.OnClickListener {
    lateinit var mAuth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    lateinit var nutrition_recyclerView: RecyclerView
    lateinit var adapter: FirestoreRecyclerAdapter<NutritionFireStoreModel, NutritionFireStoreAdapter.ViewHolder>
    lateinit var fab: ExtendedFloatingActionButton
    public lateinit var placeholder: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentView = inflater.inflate(R.layout.fragment_nutrition, container, false)
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fab = view.findViewById(R.id.nutrition_fab)
        fab.setOnClickListener(this)
        nutrition_recyclerView = view.findViewById(R.id.nutrition_rec_view)
        nutrition_recyclerView.setHasFixedSize(true)
        nutrition_recyclerView.layoutManager = LinearLayoutManager(context)
        placeholder = view.findViewById(R.id.nutrition_placeholder)
        val uid = mAuth.currentUser?.uid
        val query =
            firestore.collection("regular_users").document(uid!!).collection("meals")
                    .orderBy("name", Query.Direction.ASCENDING)
        val options =
            FirestoreRecyclerOptions.Builder<NutritionFireStoreModel>()
                    .setQuery(query, NutritionFireStoreModel::class.java).build()
        val onClickListener = View.OnClickListener {
            val element = it.tag as NutritionFireStoreAdapter.ViewHolder
            val name = element.name.text


            firestore.collection("regular_users").document(uid).collection("meals").whereEqualTo("name", name).get()
                    .addOnSuccessListener {
                        val doc = it.first().toObject(NutritionFireStoreModel::class.java)
                        val bundle = bundleOf("list" to doc.meals, "name" to name, "docId" to it.first().id)
                        val userHome = Intent(context, NutritionAddMealActivity::class.java)
                        userHome.putExtras(bundle)
                        startActivity(userHome)
                    }.addOnFailureListener {

            }

        }
        adapter = NutritionFireStoreAdapter(options, onClickListener,this)
        nutrition_recyclerView.adapter = adapter
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
