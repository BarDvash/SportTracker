package com.technion.fitracker.login


import android.content.Intent
import android.os.Bundle
import com.technion.fitracker.R
import com.technion.fitracker.user.User
import com.technion.fitracker.user.business.BusinessUserActivity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SignUpBusinessFragment : Fragment(), View.OnClickListener {

    lateinit var navController: NavController
    lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    //Button
    private lateinit var signUpButton: Button

    //Fields
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var mobilePhone: EditText
    private lateinit var userName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }


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
        firestore = FirebaseFirestore.getInstance()
        signUpButton = view.findViewById(R.id.sign_up_business_sign_up_button)
        signUpButton.setOnClickListener(this)
        view.findViewById<Button>(R.id.sign_up_business_regular_user_button).setOnClickListener(this)

        emailEditText = view.findViewById(R.id.sign_up_business_email)
        passwordEditText = view.findViewById(R.id.sign_up_business_pass)
        mobilePhone = view.findViewById(R.id.sign_up_business_phone)
        userName = view.findViewById(R.id.sign_up_business_name)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.sign_up_business_sign_up_button -> {
                signUpButton.isEnabled = false
                handleEmailSignUp()
            }
            R.id.sign_up_business_regular_user_button -> navController.navigate(
                R.id.action_signUpBusinessFragment_to_signUpFragment
            )
        }
    }

    private fun handleEmailSignUp() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val phone = mobilePhone.text.toString()
        val userProvidedName = userName.text.toString()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    val user = User(name = userProvidedName, phone_number = phone, photoURL = null, uid = uid, type = "business", search_field = userProvidedName.toLowerCase())
                    firestore.collection("business_users").document(uid!!).set(user).addOnSuccessListener {
                        startBusinessUserActivity()
                        signUpButton.isEnabled = true
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


    private fun startBusinessUserActivity() {
        val userHome = Intent(context!!, BusinessUserActivity::class.java)
        startActivity(userHome)
        activity?.finish()
    }

}
