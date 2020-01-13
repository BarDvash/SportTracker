package com.technion.fitracker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.technion.fitracker.user.User
import java.text.SimpleDateFormat
import java.util.*


class ChoosePhotoActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mFirestorage: FirebaseStorage
    private lateinit var pickImageButton: Button
    private lateinit var currentPhoto: ImageView
    private lateinit var pathReference: String
    private lateinit var strorageRef: StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_photo)
        setSupportActionBar(findViewById(R.id.toolbar2))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        pickImageButton = findViewById<Button>(R.id.pick_photo)
        currentPhoto = findViewById(R.id.current_photo)
        mAuth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        mFirestorage = FirebaseStorage.getInstance("gs://fitracker-cbd9b.appspot.com")
        val sdf = SimpleDateFormat("yyMMdd_HHmmss", Locale.getDefault())

//You can change "yyyyMMdd_HHmmss as per your requirement

        //You can change "yyyyMMdd_HHmmss as per your requirement
        val currentDateandTime: String = sdf.format(Date())
        pathReference = "profile_photos/" + mAuth.currentUser?.uid + "/profile_picture" + currentDateandTime + ".jpg"
        strorageRef = mFirestorage.getReference(pathReference)
        initUserPhoto()
        pickImageButton.setOnClickListener {
            //check runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    //permission denied
                    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE);
                } else {
                    //permission already granted
                    pickImageFromGallery();
                }
            } else {
                //system OS is < Marshmallow
                pickImageFromGallery();
            }
        }
    }

    private fun clearPreviousPhotos(){
        var deletePathReference = "profile_photos/" + mAuth.currentUser?.uid
        var delRef = mFirestorage.getReference(deletePathReference)
        delRef.listAll()
                .addOnSuccessListener { listResult ->
                    listResult.items.forEach { item ->
                        Log.d("DELETing", item.toString())
                        item.delete()
                    }
                }
                .addOnFailureListener {
                    // Uh-oh, an error occurred!
                }
    }

    private fun updatePhotoURL(photoURL: String) {
        val currentUID = mAuth.currentUser?.uid
        if (currentUID != null) {
            mFirestore.collection("regular_users").document(currentUID).get().addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    mFirestore.collection("regular_users").document(currentUID).update("photoURL", photoURL)
                    initUserPhoto()
                    pickImageButton.text = getString(R.string.pick_photo)
                    pickImageButton.isEnabled = true
                } else {
                    mFirestore.collection("business_users").document(currentUID)
                            .get().addOnSuccessListener { document ->
                                val user = document.toObject(User::class.java)
                                if (user != null) {
                                    mFirestore.collection("business_users").document(currentUID).update("photoURL", photoURL)
                                    initUserPhoto()
                                    pickImageButton.text = getString(R.string.pick_photo)
                                    pickImageButton.isEnabled = true
                                }
                            }
                }
            }
        }
    }

    private fun initUserPhoto() {
        val currentUID = mAuth.currentUser?.uid
        if (currentUID != null) {
            val docRef = mFirestore.collection("regular_users").document(currentUID)
            docRef.get().addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    if (!user.photoURL.isNullOrEmpty()) {
                        Glide.with(this) //1
                                .load(user?.photoURL)
                                .placeholder(R.drawable.user_avatar)
                                .error(R.drawable.user_avatar)
                                .skipMemoryCache(true) //2
                                .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                                .transform(CircleCrop()) //4
                                .into(currentPhoto)

                    }
                } else {
                    val docRef = mFirestore.collection("business_users").document(currentUID)
                    docRef.get().addOnSuccessListener { document ->
                        val user = document.toObject(User::class.java)
                        if (!user?.photoURL.isNullOrEmpty()) {
                            Glide.with(this) //1
                                    .load(user?.photoURL)
                                    .placeholder(R.drawable.user_avatar)
                                    .error(R.drawable.user_avatar)
                                    .skipMemoryCache(true) //2
                                    .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                                    .transform(CircleCrop()) //4
                                    .into(currentPhoto)
                        }
                    }
                }
            }
        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        //image pick code
        private const val IMAGE_PICK_CODE = 1000;
        //Permission code
        private const val PERMISSION_CODE = 1001;
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    //permission from popup granted
                    pickImageFromGallery()
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            var file = data?.data
            if (file != null) {
                pickImageButton.text = "Uploading ..."
                pickImageButton.isEnabled = false
                clearPreviousPhotos()
                var uploadTask = strorageRef.putFile(file)

                val urlTask = uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            pickImageButton.text = getString(R.string.pick_photo)
                            pickImageButton.isEnabled = true
                            throw it
                        }
                    }
                    strorageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        if(downloadUri != null){
                            updatePhotoURL(downloadUri.toString())

                        }
                    } else {
                        pickImageButton.text = getString(R.string.pick_photo)
                        pickImageButton.isEnabled = true
                    }
                }
            }

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
