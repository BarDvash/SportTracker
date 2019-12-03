package android.technion.fitracker.login


import android.os.Bundle
import android.technion.fitracker.R
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.NavController
import androidx.navigation.Navigation

/**
 * A simple [Fragment] subclass.
 */
class SignUpFragment : Fragment(), View.OnClickListener {

    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        view.findViewById<Button>(R.id.sign_up_fragment_sign_up_button).setOnClickListener(this)
        view.findViewById<Button>(R.id.sign_up_fragment_trainer_sign_up_button).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.sign_up_fragment_sign_up_button -> navController.navigate(
                R.id.action_signUpFragment_to_signInFragment
            );
            R.id.sign_up_fragment_trainer_sign_up_button -> navController.navigate(
                R.id.action_signUpFragment_to_signUpBusinessFragment
            );
        }
    }


}
