package com.technion.fitracker.user.business


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.technion.fitracker.R
import com.technion.fitracker.models.BusinessUserViewModel


class ScheduleFragment : Fragment() {

    private lateinit var viewModel: BusinessUserViewModel

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


}
