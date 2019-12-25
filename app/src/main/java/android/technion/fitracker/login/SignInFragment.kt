package android.technion.fitracker.login

import android.content.Intent
import android.os.Bundle
import android.technion.fitracker.R
import android.technion.fitracker.user.User
import android.technion.fitracker.user.business.BusinessUserActivity
import android.technion.fitracker.user.personal.UserActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import com.google.firebase.firestore.FirebaseFirestore



class SignInFragment : Fragment(), View.OnClickListener {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var signInButton: Button
    private lateinit var signInGoogleButton: SignInButton

    //Fields
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    //Auth id
    private val RC_SIGN_IN = 9001

    //Google login token
    private val idToken = "227928727350-8scqikjnk6ta5lj5runh2o0dbd9p0nil.apps.googleusercontent.com"

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
        signInButton = view.findViewById(R.id.login_fragment_sign_in_button)
        signInButton.setOnClickListener(this)
        firestore = FirebaseFirestore.getInstance()
        view.findViewById<Button>(R.id.login_fragment_sign_up_button).setOnClickListener(this)
        signInGoogleButton = view.findViewById(R.id.login_fragment_sign_in_with_google)
        signInGoogleButton.setOnClickListener(this)

        emailEditText = view.findViewById(R.id.login_fragment_email)
        passwordEditText = view.findViewById(R.id.login_fragment_pass)
    }


    override fun onStart() {
        super.onStart()

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        if (auth.currentUser != null) {
            var regular_users_doc = firestore.collection("regular_users").document(auth.currentUser!!.uid)
            var business_users_doc = firestore.collection("business_users").document(auth.currentUser!!.uid)

            regular_users_doc.get().addOnSuccessListener {
                if (it.exists()){
                    startUserActivity()
                }
                else
                {
                    business_users_doc.get().addOnSuccessListener {
                        startBusinessUserActivity()
                    }.addOnFailureListener {
                        Toast.makeText(context, getString(R.string.database_read_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun signIn() {
        signInGoogleButton.isEnabled = false
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onClick(v: View?) {
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
            R.id.login_fragment_sign_in_button -> {
                signInButton.isEnabled = false
                signInButton.text = getString(R.string.sign_in_progress)
                firebaseAuthWithEmail()

            }
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
            signInGoogleButton.isEnabled = true
            Toast.makeText(context, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show()
        }

    }


    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        val photoUrl = FirebaseAuth.getInstance().currentUser?.photoUrl
                        val user =
                            User(name = auth.currentUser?.displayName, photoURL = photoUrl.toString())
                        firestore.collection("regular_users").document(uid!!).get().addOnCompleteListener {
                            val doc = it.result
                            if (doc?.exists()!!) {
                                signInGoogleButton.isEnabled = true
                                startUserActivity()
                            } else {
                                createNewUserEntryInDB(uid, user)

                            }
                        }.addOnFailureListener {
                            createNewUserEntryInDB(uid, user)
                        }
                    } else {
                        signInGoogleButton.isEnabled = true
                        Toast.makeText(
                            context,
                            getString(R.string.authentication_failed_internet),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
    }

    private fun createNewUserEntryInDB(uid: String, user: User) {
        firestore.collection("regular_users").document(uid).set(user).addOnSuccessListener {
            signInGoogleButton.isEnabled = true
            startUserActivity()
        }.addOnFailureListener {
            signInGoogleButton.isEnabled = true
            Toast.makeText(context, getString(R.string.database_write_error), Toast.LENGTH_SHORT)
                    .show()
        }
    }

    private fun startUserActivity() {
        val userHome = Intent(context!!, UserActivity::class.java)
        startActivity(userHome)
        activity?.finish()
    }

    private fun startBusinessUserActivity() {
        val userHome = Intent(context!!, BusinessUserActivity::class.java)
        startActivity(userHome)
        activity?.finish()
    }

    private fun firebaseAuthWithEmail() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            enableButtonAndSetText()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity!!) { task ->
                    if (task.isSuccessful) {
                        if (auth.currentUser != null) {
                            val regular_users_doc_ref = firestore.collection("regular_users").document(auth.currentUser!!.uid)
                            val business_users_doc_ref = firestore.collection("business_users").document(auth.currentUser!!.uid)

                            regular_users_doc_ref.get().addOnSuccessListener {
                                if(it.exists()){
                                    enableButtonAndSetText()
                                    startUserActivity()
                                }else{
                                    business_users_doc_ref.get().addOnSuccessListener {
                                        enableButtonAndSetText()
                                        startBusinessUserActivity()
                                    }.addOnFailureListener {
                                        Toast.makeText(context, getString(R.string.database_read_error), Toast.LENGTH_SHORT)
                                                .show()
                                        enableButtonAndSetText()
                                    }
                                }

                            }
                        }

                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(context!!, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show()
                        enableButtonAndSetText()
                    }
                }
    }


    private fun enableButtonAndSetText() {
        signInButton.isEnabled = true
        signInButton.text = getString(R.string.sign_in_button_text)
    }

}
