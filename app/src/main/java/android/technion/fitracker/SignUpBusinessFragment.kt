package android.technion.fitracker


import android.os.Bundle
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
class SignUpBusinessFragment : Fragment(), View.OnClickListener {

    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up_business, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        view.findViewById<Button>(R.id.sign_up_buisiness_sign_up_button).setOnClickListener(this)
        view.findViewById<Button>(R.id.sign_up_buisiness_regular_user_button).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.sign_up_buisiness_sign_up_button -> navController.navigate(R.id.action_signUpBusinessFragment_to_signInFragment);
            R.id.sign_up_buisiness_regular_user_button -> navController.navigate(R.id.action_signUpBusinessFragment_to_signUpFragment);
        }
    }


}
