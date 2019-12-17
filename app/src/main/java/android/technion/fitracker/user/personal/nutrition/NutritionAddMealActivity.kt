package android.technion.fitracker.user.personal.nutrition


import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.databinding.ActivityAddMealBinding
import android.technion.fitracker.models.nutrition.AddMealViewModel
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation


class NutritionAddMealActivity: AppCompatActivity() {
    lateinit var navController: NavController
    lateinit var viewModel: AddMealViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this)[AddMealViewModel::class.java]
        val binding =
            DataBindingUtil.setContentView<ActivityAddMealBinding>(this, R.layout.activity_add_meal)

        binding.lifecycleOwner = this  // use Fragment.viewLifecycleOwner for fragments

        binding.viewmodel = viewModel
        navController = Navigation.findNavController(findViewById(R.id.add_meal_fragment_navigation))
    }


    override fun onSupportNavigateUp(): Boolean { //This method is called when the up button is pressed. Just the pop back stack.
        val test2 =   navController.currentDestination?.label
        if (test2 == "fragment_add_meal")
            finish()
        else
            navController.popBackStack()
        return true
    }
}