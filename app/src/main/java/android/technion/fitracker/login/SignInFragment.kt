package android.technion.fitracker.login

import android.content.Intent
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.user.UserActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


/**
 * A simple [Fragment] subclass.
 */
class SignInFragment : Fragment(), View.OnClickListener {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth

    //Auth id
    private val RC_SIGN_IN = 9001

    //Google login token
    private val idToken = "227928727350-okbq0bdbqj44m608vlpcr8t6thf8fikv.apps.googleusercontent.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

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
        view.findViewById<Button>(R.id.login_fragment_sign_up_button).setOnClickListener(this)
        view.findViewById<SignInButton>(R.id.login_fragment_sign_in_with_google).setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        if (auth.currentUser != null){
            val userHome = Intent(context!!,UserActivity::class.java)
            startActivity(userHome)
            activity?.finish()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent,RC_SIGN_IN)
    }

    override fun onClick(v: View?) {
        // TODO("We should manage here the logic for the trainer/user login")\

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(idToken)
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(context!!, gso)

        when (v!!.id) {
            R.id.login_fragment_sign_in_with_google -> signIn()
            R.id.login_fragment_sign_in_button -> navController.navigate(
                R.id.action_signInFragment_to_user_navigation_graph
            )
            R.id.login_fragment_sign_up_button -> navController.navigate(
                R.id.action_signInFragment_to_signUpFragment
            )

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account!!)
        } catch (e: ApiException) {
            Toast.makeText(context,"Authentication failed",Toast.LENGTH_SHORT).show()
        }

    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navController.navigate(R.id.action_signInFragment_to_user_navigation_graph)
//                    val userHome = Intent(context!!, UserActivity::class.java)
//                    startActivity(userHome)
//                    activity?.finish()
                } else {
                    Toast.makeText(context,"Authentication failed, please check your internet connection and try again.",Toast.LENGTH_SHORT).show()
                }
            }
    }

}
