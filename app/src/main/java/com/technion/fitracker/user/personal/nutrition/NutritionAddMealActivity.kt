package com.technion.fitracker.user.personal.nutrition


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.fitracker.R
import com.technion.fitracker.databinding.ActivityAddMealBinding
import com.technion.fitracker.models.nutrition.AddMealViewModel
import com.technion.fitracker.user.Meal


class NutritionAddMealActivity : AppCompatActivity() {
    lateinit var navController: NavController
    lateinit var viewModel: AddMealViewModel
    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    var updateData: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this)[AddMealViewModel::class.java]
        val binding =
            DataBindingUtil.setContentView<ActivityAddMealBinding>(this, R.layout.activity_add_meal)

        binding.lifecycleOwner = this  // use Fragment.viewLifecycleOwner for fragments

        binding.viewmodel = viewModel
        val params = intent.extras
        val list = params?.get("list")
        if (list != null) {
            updateData = true
            val menu = params.get("list") as ArrayList<Map<String, String>>
            val name = params.getString("name")
            viewModel.editTextMealName.value = name
            for (elem in menu) {
                viewModel.data.add(elem)
            }
            viewModel.docId = params.getString("docId")
        }
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        navController = Navigation.findNavController(findViewById(R.id.add_meal_fragment_navigation))


    }


    override fun onSupportNavigateUp(): Boolean { //This method is called when the up button is pressed. Just the pop back stack.
        val currentFragmentName = navController.currentDestination?.label
        if (currentFragmentName == "fragment_add_dish" && viewModel.dishes.isNotEmpty()) {
            showWarning()
            return false
        }
        if (currentFragmentName == "fragment_add_meal" && viewModel.data.isNotEmpty()) {
            showWarning()
            return false
        }
        goBack()
        return true
    }

    override fun onBackPressed() {
        onSupportNavigateUp()
    }

    private fun goBack(): Boolean {

        val currentFragmentName = navController.currentDestination?.label
        if (currentFragmentName == "fragment_add_meal") {
//            viewModel.writeToDB(updateData,db,auth)
            finish()
        } else
            navController.popBackStack()
        return true
    }


    private fun showWarning() {
        MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.usaved_data_will_be_lost))
                .setPositiveButton(
                    getString(R.string.yes)
                ) { _, _ ->
                    goBack()
                }
                .setNegativeButton(
                    getString(R.string.no)
                ) { _, _ ->

                }.show()
    }

    fun writeToDB(updateData: Boolean) {
        val data = Meal(viewModel.editTextMealName.value, viewModel.data)
        if (!updateData) {
            db.collection("regular_users").document(auth.currentUser!!.uid).collection("meals").add(data).addOnSuccessListener {
                //                    Toast.makeText(context,"done",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                //                    Toast.makeText(context,"nope",Toast.LENGTH_SHORT).show()
            }
        } else {
            db.collection("regular_users").document(auth.currentUser!!.uid).collection("meals").document(viewModel.docId!!).set(data)
                    .addOnSuccessListener {
                        //                    Toast.makeText(context,"done",Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        //                    Toast.makeText(context,"nope",Toast.LENGTH_SHORT).show()
                    }
        }
    }

    fun deleteFromDB() {
        db.collection("regular_users").document(auth.currentUser!!.uid).collection("meals").document(viewModel.docId!!).delete()
                .addOnSuccessListener {
                    //                    Toast.makeText(context,"done",Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    //                    Toast.makeText(context,"nope",Toast.LENGTH_SHORT).show()
                }
    }
}