package com.technion.fitracker

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


private const val TITLE_TAG = "settingsActivityTitle"

class SettingsActivity : AppCompatActivity(),
                         PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setSupportActionBar(findViewById(R.id.settings_toolbar))
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, HeaderFragment())
                    .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title_activity_settings)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                pref.fragment
        ).apply {
            arguments = args
            setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit()
        title = pref.title

        return true
    }

    class HeaderFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey)

        }

        override fun onResume() {
            super.onResume()
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        }

        override fun onPause() {
            preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
            super.onPause()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val auth = FirebaseFirestore.getInstance()
            when (key) {
                "phone_number" -> {
                    var pref = findPreference<EditTextPreference>("phone_number")
                    auth.collection("regular_users").document(uid)
                            .update("phone_number", "972" + pref?.text).addOnSuccessListener {
                                auth.collection("regular_users").document(uid).get().addOnSuccessListener { innerIt ->
                                    val trainerUID = innerIt.getString("personal_trainer_uid")
                                    if (trainerUID != null) {
                                        auth.collection("business_users").document(trainerUID).collection("customers").document(uid)
                                                .update("customer_phone_number", "972" + pref?.text)
                                    }
                                }
                            }.addOnFailureListener { e ->
                                run {
                                    auth.collection("business_users")
                                            .document(uid)
                                            .update("phone_number", "972" + pref?.text)
                                }
                            }
                }
                "edit_landing_page" -> {
                    var pref = findPreference<EditTextPreference>("edit_landing_page")
                    auth.collection("regular_users").document(uid)
                            .update("landing_info", pref?.text).addOnFailureListener { e ->
                                run {
                                    auth.collection("business_users")
                                            .document(uid)
                                            .update("landing_info", pref?.text)

                                }
                            }
                }
                "show_phone" -> {
                    var pref = findPreference<SwitchPreferenceCompat>("show_phone")
                    auth.collection("regular_users").document(uid)
                            .update("show_phone", pref?.isChecked).addOnFailureListener { e ->
                                run {
                                    auth.collection("business_users")
                                            .document(uid)
                                            .update("show_phone", pref?.isChecked)

                                }
                            }
                }
            }
        }
    }

    class AboutFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.about_preferences, rootKey)
        }
    }

    class AccountFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.account_preferences, rootKey)
        }
    }


}
