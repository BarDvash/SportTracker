package com.technion.fitracker.login


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.fitracker.R
import com.technion.fitracker.user.User
import com.technion.fitracker.user.personal.UserActivity


class SignUpFragment : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth

    //Button
    private lateinit var signUpButton: Button

    //Fields
    private lateinit var emailEditText: EditText
    private lateinit var nameEditText: EditText
    lateinit var firestore: FirebaseFirestore
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        if (auth.currentUser != null) {
            val docRef = firestore.collection("regular_users").document(auth.currentUser!!.uid)
            docRef.get().addOnSuccessListener { document ->

                if (document.exists()) {
                    startUserActivity()
                }

            }.addOnFailureListener {
                Toast.makeText(context, getString(R.string.database_read_error), Toast.LENGTH_SHORT).show()
            }


        }
    }

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
        firestore = FirebaseFirestore.getInstance()
        view.findViewById<Button>(R.id.sign_up_fragment_trainer_sign_up_button).setOnClickListener(this)
        signUpButton = view.findViewById(R.id.sign_up_fragment_sign_up_button)
        signUpButton.setOnClickListener(this)
        emailEditText = view.findViewById(R.id.sign_up_fragment_email)
        passwordEditText = view.findViewById(R.id.sign_up_fragment_pass)
        nameEditText = view.findViewById(R.id.sign_up_fragment_name)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.sign_up_fragment_sign_up_button -> {
                signUpButton.isEnabled = false
                handleEmailSignUp()
            }
            R.id.sign_up_fragment_trainer_sign_up_button -> navController.navigate(
                R.id.action_signUpFragment_to_signUpBusinessFragment
            )
        }
    }

    private fun handleEmailSignUp() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val userName = nameEditText.text.toString()
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity!!) { task ->
                    if (task.isSuccessful) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        val photoUrl = FirebaseAuth.getInstance().currentUser?.photoUrl
                        val user = User(name = userName, photoURL = photoUrl.toString())
                        firestore.collection("regular_users").document(uid!!).set(user).addOnSuccessListener {
                            signUpButton.isEnabled = true
                            startUserActivity()
                        }.addOnFailureListener {
                            signUpButton.isEnabled = true
                            Toast.makeText(context, getString(R.string.database_write_error), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        signUpButton.isEnabled = true
                        Toast.makeText(context, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show()
                    }
                }
    }


    private fun startUserActivity() {

        val userHome = Intent(context!!, UserActivity::class.java)
        startActivity(userHome)
        activity?.finish()

    }
}
