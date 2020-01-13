package com.technion.fitracker.user.personal.workout

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.fitracker.R
import com.technion.fitracker.adapters.WorkoutHistoryLogAdapter
import com.technion.fitracker.databinding.ActivityWorkoutHistoryElementDetailsBinding
import com.technion.fitracker.models.exercise.ExerciseLogModel
import com.technion.fitracker.models.workouts.WorkoutHistoryModel
import java.io.File
import java.io.FileOutputStream
import java.util.*

class WorkoutHistoryElementDetails : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth
    lateinit var mFirestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: WorkoutHistoryLogAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    lateinit var viewModel: WorkoutHistoryModel
    lateinit var ratingImage: ImageView
    lateinit var commentHolder: LinearLayout
    var uid: String? = null
    var isTrainer: Boolean = false
    var screenshotPath: String? = null
    var granted = false


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_history_element_details)
        viewModel = ViewModelProviders.of(this)[WorkoutHistoryModel::class.java]
        val binding =
            DataBindingUtil.setContentView<ActivityWorkoutHistoryElementDetailsBinding>(this, R.layout.activity_workout_history_element_details)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setSupportActionBar(findViewById(R.id.workout_history_toolbar))
        val params = intent.extras
        mAuth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        ratingImage = findViewById(R.id.workout_rating)
        commentHolder = findViewById(R.id.workout_comment_container)
        if (params?.get("userID") as String? != null) {
            isTrainer = true
        }
        uid = params?.get("userID") as String? ?: mAuth.currentUser!!.uid
        viewModel.timeElapsed.value = params?.get("time_elapsed") as String?
        viewModel.workoutID = params?.get("id") as String?
        viewModel.workoutName.value = params?.get("workout_name") as String?
        viewModel.workoutComment.value = params?.get("comment") as String?
        viewModel.workoutDate.value = params?.get("date_time") as String?
        viewModel.workoutRating.value = params?.get("rating") as Long?
        if (viewModel.workoutComment.value == null || viewModel.workoutComment.value?.length == 0) {
            commentHolder.visibility = View.GONE
        }
        val exercisesHashMap = (params?.get("exercises") as ArrayList<HashMap<String, String?>>?)
        if (exercisesHashMap != null) {
            for (exercise in exercisesHashMap) {
                viewModel.workoutExercises.value?.add(ExerciseLogModel(exercise["name"], exercise["time_done"]))
            }
        }
        when (viewModel.workoutRating.value?.toInt()) {
            WorkoutSummaryScreen.ExerciseRatings.SAD.ordinal -> {
                ratingImage.setImageResource(R.drawable.ic_sad)
            }
            WorkoutSummaryScreen.ExerciseRatings.NEUTRAL.ordinal -> {
                ratingImage.setImageResource(R.drawable.ic_neutral)
            }
            WorkoutSummaryScreen.ExerciseRatings.HAPPY.ordinal -> {
                ratingImage.setImageResource(R.drawable.ic_happiness)
            }
            WorkoutSummaryScreen.ExerciseRatings.COOL.ordinal -> {
                ratingImage.setImageResource(R.drawable.ic_cool)
            }
            else -> {
                ratingImage.setImageResource(R.drawable.ic_happiness)
            }
        }

        supportActionBar?.title = viewModel.workoutName.value
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewAdapter = WorkoutHistoryLogAdapter(viewModel.workoutExercises.value!!)
        viewManager = LinearLayoutManager(this)
        recyclerView = findViewById<RecyclerView>(R.id.workout_history_recycler)?.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }!!
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.workout_history_menu, menu)
        if (isTrainer == false) {
            menu?.add(0, 1, Menu.NONE, "Delete")?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menu?.add(0, 2, Menu.NONE, "Share")?.apply {
                setIcon(R.drawable.ic_share)
                setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }
        }
        return true

    }

    private fun takeScreenshot(): File? {
        val now = Date()
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)

        try {
            // image naming and path  to include sd card  appending name you choose for file

            // create bitmap screen capture
            val v1 = window.decorView.rootView
            v1.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(v1.drawingCache)
            v1.isDrawingCacheEnabled = false
            val imagePath = File(this.filesDir, "/")
            val imageFile = File(imagePath, now.toString() + ".jpg")

            val outputStream = FileOutputStream(imageFile)
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()

            //setting screenshot in imageview
            val filePath = imageFile.path
            val ssbitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            screenshotPath = filePath
            return imageFile

        } catch (e: Throwable) {
            // Several error may come out with file handling or DOM
            e.printStackTrace()
            return null
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val STORAGE_REQUEST = 1
        when (requestCode) {
            STORAGE_REQUEST -> {
                granted =
                    (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                if (granted) {
                    takeScreenshot()?.let {
                        share(it)
                    }
                }
                return
            }
        }
    }

    private fun share(sharePath: File) {
        var contentUri = getUriForFile(this, "com.technion.fitracker.fileprovider", sharePath)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, contentUri)
        startActivity(intent)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val STORAGE_REQUEST = 1
        val delete_action = 1
        val share_action = 2
        when (item.itemId) {
            share_action -> {
                if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                            this,
                            arrayOf(
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                            ),
                            STORAGE_REQUEST
                    )
                } else {
                    takeScreenshot()?.let {
                        share(it)
                    }
                }
            }
            delete_action -> {
                MaterialAlertDialogBuilder(this).setTitle("Warning").setMessage("Delete workout activity?")
                        .setPositiveButton(
                                "Yes"
                        ) { _, _ ->
                            mFirestore.collection("regular_users").document(uid!!).collection("workouts_history").document(viewModel.workoutID!!)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d(FragmentActivity.VIBRATOR_SERVICE, "DocumentSnapshot deleted with ID: " + viewModel.workoutID)
                                    }
                                    .addOnFailureListener { e -> Log.w(FragmentActivity.VIBRATOR_SERVICE, "Error deleting document", e) }
                            finish()
                        }
                        .setNegativeButton(
                                "No"
                        ) { _, _ ->
                        }.show()

            }
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }
}

//val bundle = bundleOf("dishes" to viewModel.data[pos], "pos" to pos)
//summary["workout_name"] = viewModel.workoutName.value
//summary["time_elapsed"] = viewModel.timeElapsed.value
//val exercisesLog: ArrayList<ExerciseLogModel> = arrayListOf()
//viewModel.workoutExercises.value?.let {
//    for (exercise in it) {
//        exercisesLog.add(exercise.extractLogModel())
//    }
//}
//summary["exercises"] = exercisesLog
//summary["date_time"] = SimpleDateFormat("yyyy-MM-dd 'at' HH:mm").format(Calendar.getInstance().time)
//summary["comment"] = viewModel.comment.value
//summary["rating"] = viewModel.workoutRate