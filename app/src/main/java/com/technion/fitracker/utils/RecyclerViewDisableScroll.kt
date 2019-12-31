package com.technion.fitracker.utils

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewDisableScroll : RecyclerView.OnItemTouchListener {
    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
    }


    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        return true
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }
}