package com.technion.fitracker.user.business


import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.technion.fitracker.R
import com.technion.fitracker.adapters.AppointmentsSpinnerAdapter
import com.technion.fitracker.adapters.schedule.FirebaseScheduleAdapter
import com.technion.fitracker.models.schedule.AppointmentModel
import com.technion.fitracker.models.BusinessUserViewModel
import com.technion.fitracker.models.schedule.AppointmentRescheduleNotificationModel
import com.technion.fitracker.utils.CalendarEventDecorator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


class ScheduleFragment : Fragment() {

    private lateinit var viewModel: BusinessUserViewModel
    lateinit var calendarView: MaterialCalendarView
    lateinit var recView: RecyclerView
    lateinit var fab: FloatingActionButton
    lateinit var spinner: Spinner
    lateinit var scrollView: ScrollView

    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy")
    private val dateCalendarFormat = SimpleDateFormat("yyyy MM dd")
    private val timeFormat = SimpleDateFormat("HH mm")
    private val dateNotificationFormat = SimpleDateFormat("yyyy MM dd HH mm")
    private val dateAndTimeFormat = SimpleDateFormat("dd MMMM yyyy 'at' HH:mm")
    private var adapter: FirebaseScheduleAdapter? = null
    private val calendar = Calendar.getInstance()
    private var chosenDate = 0L
    private val traineesNames = ArrayList<String>()
    private val traineesIds = ArrayList<String>()
    private val traineesPhotos = ArrayList<String>()
    private val eventDates = HashSet<CalendarDay>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[BusinessUserViewModel::class.java]
        } ?: throw Exception("Invalid Fragment, customers fragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_business_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        initSpinnerData()
        calendarView = view.findViewById(R.id.schedule_calendarView)
        scrollView = view.findViewById(R.id.scrollView2)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                if (scrollY - oldScrollY > 0) {
                    if (fab.isShown) {
                        fab.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_go_down))
                        fab.visibility = View.GONE
                    }
                } else if (scrollY - oldScrollY < 0) {
                    if (!fab.isShown) {
                        fab.visibility = View.VISIBLE
                        fab.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_go_up))
                    }
                }
            }
        }

        fab = view.findViewById(R.id.add_meeting_btn)
        chosenDate = calendar.timeInMillis
        setCurrentDateSelected()
        fab.setOnClickListener {
            var selectedPos = -1
            val dial = Dialog(context!!, R.style.WideDialog)
            dial.setContentView(R.layout.dialog_add_appointment)

            spinner = dial.findViewById(R.id.dialog_user_spinner)
            val spinnerAdapter = AppointmentsSpinnerAdapter(context!!, traineesNames, traineesIds, traineesPhotos)
            spinner.adapter = spinnerAdapter
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedPos = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

            dial.setTitle(dateFormat.format(chosenDate))
            val time = dial.findViewById<TimePicker>(R.id.dialog_date_picker)
            time.setIs24HourView(true)
            val notes = dial.findViewById<EditText>(R.id.dialog_notes)

            val addButton = dial.findViewById<Button>(R.id.dialog_add_button)
            addButton.setOnClickListener {
                val timeString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    toProperFormat(time.hour) + " " + toProperFormat(time.minute)
                } else {
                    toProperFormat(time.currentHour) + " " + toProperFormat(time.currentMinute)
                }
                val date = dateCalendarFormat.format(chosenDate)
                val appointment = AppointmentModel(
                        traineesIds[selectedPos],
                        date,
                        timeString,
                        notes.text.toString()
                )
                db.collection("business_users").document(auth.currentUser!!.uid).collection("appointments").add(appointment).addOnSuccessListener {

                }
                prepareNotificationPackage(auth.currentUser!!.uid, traineesIds[selectedPos], "", "", date, timeString)
                initEventDates()
                initRecyclerData()
                dial.hide()
            }
            dial.show()
        }
        calendarView.setOnDateChangedListener { _, _, _ ->
            val day = calendarView.selectedDate!!.day
            val month = calendarView.selectedDate!!.month
            val year = calendarView.selectedDate!!.year
            val res = dateCalendarFormat.parse("$year $month $day")
            chosenDate = res!!.time
            initRecyclerData()
        }
        calendarView.addDecorator(CalendarEventDecorator(eventDates, Color.parseColor("#d32f2f")))
        initEventDates()
        recView = view.findViewById(R.id.schedule_rec_view)
        recView.layoutManager = LinearLayoutManager(context)
        initRecyclerData()

    }

    private fun toProperFormat(time: Int): String {
        if (time < 10) {
            return "0$time"
        } else {
            return "$time"
        }
    }

    private fun initRecyclerData() {
        val query = db.collection("business_users").document(auth.currentUser!!.uid)
                .collection("appointments")
                .whereEqualTo("appointment_date", dateCalendarFormat.format(chosenDate))
                .orderBy("appointment_time", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions.Builder<AppointmentModel>().setQuery(query, AppointmentModel::class.java).build()
        if (adapter != null) {
            adapter?.stopListening()
        }
        val onClickListener = View.OnClickListener {
            val name = it.findViewById<TextView>(R.id.element_schedule_name)
            val date = it.findViewById<TextView>(R.id.element_schedule_date)


            val oldDate = dateCalendarFormat.format(dateAndTimeFormat.parse(date.text.toString())!!)
            val oldTime = timeFormat.format(dateAndTimeFormat.parse(date.text.toString())!!)
            val userID = (it.tag as FirebaseScheduleAdapter.ViewHolder).customerId

            var selectedPos = -1
            val dial = Dialog(context!!, R.style.WideDialog)
            dial.setContentView(R.layout.dialog_edit_appointment)
            val datePicker = dial.findViewById<DatePicker>(R.id.dialog_edit_date_picker)
            val dateToConvert = oldDate.split(" ")
            val notesField = dial.findViewById<EditText>(R.id.dialog_edit_notes)
            notesField.setText((it.tag as FirebaseScheduleAdapter.ViewHolder).notes)

            datePicker.updateDate(dateToConvert[0].toInt(), dateToConvert[1].toInt() - 1, dateToConvert[2].toInt())
            val spinner = dial.findViewById<Spinner>(R.id.dialog_edit_user_spinner)
            val spinnerAdapter = AppointmentsSpinnerAdapter(context!!, traineesNames, traineesIds, traineesPhotos)
            spinner.adapter = spinnerAdapter
            spinner.setSelection(traineesNames.indexOf(name.text))
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedPos = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

            dial.setTitle(date.text)
            val time = dial.findViewById<TimePicker>(R.id.dialog_edit_time_picker)
            time.setIs24HourView(true)
            val hoursAndMinutes = date.text.split(" ").last().split(":")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                time.hour = hoursAndMinutes[0].toInt()
                time.minute = hoursAndMinutes[1].toInt()
            } else {
                time.currentHour = hoursAndMinutes[0].toInt()
                time.currentMinute = hoursAndMinutes[1].toInt()
            }
            val editButton = dial.findViewById<Button>(R.id.dialog_edit_button)
            editButton.setOnClickListener {
                val newTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    "${toProperFormat(time.hour)} ${toProperFormat(time.minute)}"
                } else {
                    "${toProperFormat(time.currentHour)} ${toProperFormat(time.currentMinute)}"
                }
                val newDate =
                    dateCalendarFormat.format(dateCalendarFormat.parse("${datePicker.year} ${(datePicker.month + 1)} ${datePicker.dayOfMonth}")!!)
                val appointment = AppointmentModel(
                        traineesIds[selectedPos],
                        newDate,
                        newTime,
                        notesField.text.toString()
                )
                db.collection("business_users").document(auth.currentUser!!.uid).collection("appointments")
                        .whereEqualTo("appointment_date", oldDate)
                        .whereEqualTo("appointment_time", oldTime)
                        .whereEqualTo("customer_id", userID).get().addOnCompleteListener { it2 ->
                            if (it2.result != null) {
                                val docId = it2.result!!.first().id
                                db.collection("business_users").document(auth.currentUser!!.uid).collection("appointments").document(docId)
                                        .set(appointment).addOnSuccessListener {
                                            if (oldDate != newDate || oldTime != newTime) {
                                                prepareNotificationPackage(auth.currentUser!!.uid, traineesIds[selectedPos],
                                                                           oldDate, oldTime, newDate, newTime)
                                            }
                                            initEventDates()
                                            initRecyclerData()
                                            dial.hide()
                                        }.addOnFailureListener {
                                            dial.hide()
                                        }
                            } else {
                                dial.hide()
                            }
                        }

            }
            val deleteButton = dial.findViewById<Button>(R.id.dialog_delete_button)
            deleteButton.setOnClickListener {
                db.collection("business_users").document(auth.currentUser!!.uid).collection("appointments")
                        .whereEqualTo("appointment_date", oldDate)
                        .whereEqualTo("appointment_time", oldTime)
                        .whereEqualTo("customer_id", userID).get().addOnCompleteListener { it2 ->
                            if (it2.result != null) {
                                val docId = it2.result!!.first().id
                                db.collection("business_users").document(auth.currentUser!!.uid).collection("appointments").document(docId)
                                        .delete().addOnSuccessListener {
                                            prepareNotificationPackage(auth.currentUser!!.uid, traineesIds[selectedPos], oldDate, oldTime, "", "")
                                            initEventDates()
                                            initRecyclerData()
                                            dial.hide()
                                        }.addOnFailureListener {
                                            dial.hide()
                                        }
                            } else {
                                dial.hide()
                            }
                        }
            }
            dial.show()
        }
        adapter = FirebaseScheduleAdapter(options, onClickListener,this)
        recView.adapter = adapter
        adapter?.startListening()
    }

    private fun setCurrentDateSelected() {
        val res = dateCalendarFormat.format(chosenDate).split(" ")
        calendarView.selectRange(
                CalendarDay.from(res[0].toInt(), res[1].toInt(), res[2].toInt()),
                CalendarDay.from(res[0].toInt(), res[1].toInt(), res[2].toInt())
        )
    }

    private fun initSpinnerData() {
        db.collection("business_users").document(auth.currentUser!!.uid).collection("customers")
                .get().addOnSuccessListener {
                    if (!it.isEmpty) {
                        for (element in it) {
                            traineesNames.add(element.getString("customer_name") ?: "")
                            traineesIds.add(element.getString("customer_id") ?: "")
                            traineesPhotos.add(element.getString("customer_photo_url") ?: "")
                        }
                    }
                }
    }

    private fun prepareNotificationPackage(trainerId :String, traineeId: String, oldDate: String, oldTime: String,newDate: String, newTime: String){
        val oldFormattedDate =  if (oldDate.isEmpty()) {
            ""
        }
        else {
            dateAndTimeFormat.format(dateNotificationFormat.parse("$oldDate $oldTime")!!)
        }
        val newFormattedDate = if (newDate.isEmpty()) {
            ""
        }
        else{
            dateAndTimeFormat.format(dateNotificationFormat.parse("$newDate $newTime")!!)
        }
        val notification = AppointmentRescheduleNotificationModel(trainerId, traineeId, oldFormattedDate, newFormattedDate)
        db.collection("regular_users").document(traineeId).collection("appointments_updates").add(notification).addOnSuccessListener {
        }.addOnFailureListener {
        }
    }

    private fun initEventDates() {
        db.collection("business_users").document(auth.currentUser!!.uid).collection("appointments").get(Source.CACHE).addOnSuccessListener {
            fillDatesAndInvalidate(it)
            initDatesFromInternet()

        }.addOnFailureListener {
            initDatesFromInternet()
        }
    }

    private fun initDatesFromInternet() {
        db.collection("business_users").document(auth.currentUser!!.uid).collection("appointments").get().addOnSuccessListener {
            eventDates.clear()
            fillDatesAndInvalidate(it)
        }
    }

    private fun fillDatesAndInvalidate(it: QuerySnapshot) {
        for (elem in it) {
            val date = elem.getString("appointment_date")
            val res = date!!.split(" ")
            eventDates.add(CalendarDay.from(res[0].toInt(), res[1].toInt(), res[2].toInt()))
        }
        calendarView.invalidateDecorators()
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }
}
