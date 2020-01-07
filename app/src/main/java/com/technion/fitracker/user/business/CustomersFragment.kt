package com.technion.fitracker.user.business



import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.technion.fitracker.R
import com.technion.fitracker.adapters.CustomersFireStoreAdapter
import com.technion.fitracker.models.BusinessUserViewModel
import com.technion.fitracker.models.CustomersFireStoreModel
import com.technion.fitracker.user.business.customer.ViewCustomerData
import com.technion.fitracker.user.personal.workout.edit.CreateNewWorkoutActivity





class CustomersFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    private lateinit var viewModel: BusinessUserViewModel
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var recyclerView: RecyclerView
    lateinit var adapter: FirestoreRecyclerAdapter<CustomersFireStoreModel, CustomersFireStoreAdapter.ViewHolder>
    lateinit var placeholder: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_business_customers, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[BusinessUserViewModel::class.java]
        } ?: throw Exception("Invalid Fragment, customers fragment")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fab = view.findViewById(R.id.customers_fab)
        recyclerView = view.findViewById(R.id.customers_rec_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    if (fab.isShown) {
                        fab.hide()
                    }
                } else if (dy < 0) {
                    if (!fab.isShown) {
                        fab.show()
                    }
                }
            }
        })
        val uid = mAuth.currentUser?.uid
        placeholder = view.findViewById(R.id.no_customers_placeholder)
        val query = firestore.collection("business_users").document(uid!!).collection("customers").orderBy("customer_name", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions.Builder<CustomersFireStoreModel>().setQuery(query, CustomersFireStoreModel::class.java).build()
        adapter = CustomersFireStoreAdapter(options, this).apply{
            mOnItemClickListener = View.OnClickListener { v ->
                val rvh = v.tag as CustomersFireStoreAdapter.ViewHolder
                val customerID: String? = adapter.snapshots.getSnapshot(rvh.adapterPosition).get("customer_id") as String?
                val customerName: String? = adapter.snapshots.getSnapshot(rvh.adapterPosition).get("customer_name") as String?
                val customerView = Intent(context!!, ViewCustomerData::class.java)
                customerView.putExtra("customerID", customerID)
                customerView.putExtra("customerName", customerName)
                startActivity(customerView)
            }
        }
        recyclerView.adapter = adapter
        //TODO: initialize fab ?
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

}
