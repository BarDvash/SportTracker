package com.technion.fitracker.user.personal.measurements

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.model.Document
import com.technion.fitracker.R
import com.technion.fitracker.adapters.measurements.MeasurementsFireStoreAdapter
import com.technion.fitracker.models.measurements.MeasurementsHistoryModel
import java.text.SimpleDateFormat


class MeasurementsGraphActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    var adapter: MeasurementsFireStoreAdapter? = null
    val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
    val newDateFormat = SimpleDateFormat("dd-MMMM-yyyy HH:mm")
    var uid: String? = null
    val translationTable =
        hashMapOf(
            "Biceps" to "biceps",
            "Body fat" to "body_fat",
            "Chest" to "chest",
            "Hips" to "hips",
            "Waist" to "waist",
            "Weight" to "weight"
        )

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.measurements_graph_activity)
        setSupportActionBar(findViewById(R.id.measurements_toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val params = intent.extras
        val name = params?.get("name") as String
        uid = params.get("userID") as String? ?: auth.currentUser!!.uid
        val units = params.getString("units")
        val dates = ArrayList<String>()
        Log.d(Context.VIBRATOR_SERVICE,(params.get("userID") as String?).toString())

        val chart = findViewById<BarChart>(R.id.chart)
        db.collection("regular_users").document(uid!!).collection("measurements").orderBy("data",Query.Direction.DESCENDING).get().addOnSuccessListener {
            val entries = ArrayList<BarEntry>()
            var i = 0
            val docs = takeLastDocs(5,it)
            for (element in docs) {
                if (element.getString(translationTable[name]!!).isNullOrEmpty()) {
                    continue
                }
                val doc = element.toObject(MeasurementsHistoryModel::class.java)
                val date = dateFormat.parse(doc.data!!)
                dates.add(getDate(newDateFormat.format(date!!)))
                entries.add(BarEntry(i++.toFloat(), element.getString(translationTable[name]!!)!!.toFloat()))
            }
            val dataset = BarDataSet(entries, "$name $units")
            dataset.apply {
                valueTextSize = 12F
                color = ColorTemplate.rgb(getString(R.color.secondaryDarkColor))
            }
            val lineData = BarData(dataset)
            chart.data = lineData
            val xAxis = chart.xAxis
            xAxis.apply {
                textSize = 12F
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = object : ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                        return dates[value.toInt()]
                    }
                }
                setDrawGridLines(false)
            }
            chart.axisRight.apply {
                isEnabled = false
            }
            chart.axisLeft.apply {
                isEnabled = false
//                granularity = 1f
//                textSize = 12F
            }
            chart.legend.apply {
                textSize = 12F
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
            chart.description.isEnabled = false
            chart.setExtraOffsets(10F, 10F, 10F, 10F)
            chart.invalidate()
        }

    }

    private fun takeLastDocs(num: Int, docs: QuerySnapshot): ArrayList<QueryDocumentSnapshot> {
        val res = ArrayList<QueryDocumentSnapshot>()
        var count = 0
        for (element in docs) {
            if (count == num) {
                break
            }
            count++
            res.add(0,element)
        }
        return res
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    private fun getDate(date: String): String {
        val index = date.indexOfLast { x -> x == '-' }
        return date.substring(0, index).replace("-"," ")
    }

}