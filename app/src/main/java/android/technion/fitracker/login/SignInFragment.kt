package android.technion.fitracker.login


import android.os.Bundle
import android.technion.fitracker.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


/**
 * A simple [Fragment] subclass.
 */
class SignInFragment : Fragment(), View.OnClickListener {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var navController: NavController

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
//        view.findViewById<Button>(R.id.login_fragment_sign_in_with_google).setOnClickListener(this)
        view.findViewById<Button>(R.id.login_fragment_sign_up_button).setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(context!!)

//        updateUI(account)
    }

    override fun onClick(v: View?) {
        // TODO("We should manage here the logic for the trainer/user login")\

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(context!!, gso)

        when (v!!.id) {
            R.id.login_fragment_sign_in_button -> navController.navigate(
                R.id.action_signInFragment_to_user_navigation_graph
            );
            R.id.login_fragment_sign_in_with_google -> navController.navigate(
                R.id.action_signInFragment_self
            );
            R.id.login_fragment_sign_up_button -> navController.navigate(
                R.id.action_signInFragment_to_signUpFragment
            );

        }
    }


}
