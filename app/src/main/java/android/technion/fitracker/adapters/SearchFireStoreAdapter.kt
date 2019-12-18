package android.technion.fitracker.adapters

import android.technion.fitracker.R
import android.technion.fitracker.models.SearchFireStoreModel
import android.technion.fitracker.adapters.SearchFireStoreAdapter.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions


class SearchFireStoreAdapter(options: FirestoreRecyclerOptions<SearchFireStoreModel>) : FirestoreRecyclerAdapter<SearchFireStoreModel, ViewHolder>(options) {


    var mOnItemClickListener: View.OnClickListener? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.setTag(this)
            view.setOnClickListener(mOnItemClickListener)
        }
        var name: TextView = view.findViewById(R.id.search_card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_ele, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: SearchFireStoreModel) {
        holder.name.text = item.name
    }

    fun setOnItemClickListener(itemClickListener: View.OnClickListener) {
        mOnItemClickListener = itemClickListener
    }





}




