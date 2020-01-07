package com.technion.fitracker.user.personal.measurements

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    val translationTable =
        hashMapOf(
            "Biceps" to "biceps",
            "Body fat" to "body_fat",
            "Chest" to "chest",
            "Hips" to "hips",
            "Waist" to "waist",
            "Weight" to "weight"
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.measurements_graph_activity)
        setSupportActionBar(findViewById(R.id.measurements_toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val params = intent.extras
        val name = params?.get("name") as String
        val dates = ArrayList<String>()


        val chart = findViewById<LineChart>(R.id.chart)
        db.collection("regular_users").document(auth.currentUser!!.uid).collection("measurements").orderBy("data").get().addOnSuccessListener {
            val entries = ArrayList<Entry>()
            var i = 0
            for (element in it) {
                if (element.getString(translationTable[name]!!) == null) {
                    continue
                }
                val doc = element.toObject(MeasurementsHistoryModel::class.java)
                val date = dateFormat.parse(doc.data!!)
                dates.add(getDate(newDateFormat.format(date!!)))
                entries.add(Entry(i++.toFloat(), element.getString(translationTable[name]!!)!!.toFloat()))
            }
            val dataset = LineDataSet(entries, name)
            dataset.valueTextSize = 12F
            val lineData = LineData(dataset)
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
            }
            chart.axisRight.apply {
                isEnabled = false
            }
            chart.axisLeft.apply {
                granularity = 1f
                textSize = 12F
            }
            chart.legend.apply {
                textSize = 12F
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
            chart.description.isEnabled = false
            chart.invalidate()
        }

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
        return date.substring(0, index)
    }

}