package android.technion.fitracker


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation

/**
 * A simple [Fragment] subclass.
 */
class SignInFragment : Fragment(), View.OnClickListener {

    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        view.findViewById<Button>(R.id.login_fragment_sign_in_button).setOnClickListener(this)
        view.findViewById<Button>(R.id.login_fragment_sign_in_with_google).setOnClickListener(this)
        view.findViewById<Button>(R.id.login_fragment_sign_up_button).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        // TODO("We should manage here the logic for the trainer/user login")
        when (v!!.id) {
            R.id.login_fragment_sign_in_button -> navController.navigate(R.id.action_signInFragment_self);
            R.id.login_fragment_sign_in_with_google -> navController.navigate(R.id.action_signInFragment_self);
            R.id.login_fragment_sign_up_button -> navController.navigate(R.id.action_signInFragment_to_signUpFragment);

        }
    }


}
