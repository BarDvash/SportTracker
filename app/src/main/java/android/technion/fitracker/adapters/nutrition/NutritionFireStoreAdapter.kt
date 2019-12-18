package android.technion.fitracker.adapters.nutrition

import android.technion.fitracker.R
import android.technion.fitracker.adapters.nutrition.NutritionFireStoreAdapter.ViewHolder
import android.technion.fitracker.models.nutrition.NutritionFireStoreModel
import android.technion.fitracker.user.Meal
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.StringBuilder

class NutritionFireStoreAdapter(options: FirestoreRecyclerOptions<NutritionFireStoreModel>) :
        FirestoreRecyclerAdapter<NutritionFireStoreModel, ViewHolder>(options) {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.nutrition_ele, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: NutritionFireStoreModel) {
        holder.name.text = item.name
        firestore.collection("users").document(auth.currentUser!!.uid).collection("meals")
                .whereEqualTo("name", item.name!!).get().addOnSuccessListener { documents ->
                    val meal = documents.first().toObject(Meal::class.java)
                    val names = ArrayList<String>()
                    val counts = ArrayList<String>()
                    for (dish in meal.meals!!) {
                        val sbNames = StringBuilder()
                        val sbCount = StringBuilder()
                        for (pair in dish) {
                            sbNames.appendln(pair.key)
                            sbCount.appendln(pair.value)
                        }
                        names.add(sbNames.toString().substringBeforeLast('\n'))
                        counts.add(sbCount.toString().substringBeforeLast('\n'))
                    }
                    holder.recView.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(holder.recView.context)
                        adapter = NutritionNestedAdapter(names, counts)
                        setRecycledViewPool(viewPool)
                    }
                }
}

inner class ViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
    var name: TextView = view.findViewById(R.id.nutritionName)
    var recView: RecyclerView = view.findViewById(R.id.nutrition_dishes_rec_view)
}

}