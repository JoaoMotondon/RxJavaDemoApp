package com.motondon.rxjavademoapp.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.motondon.rxjavademoapp.R

import java.util.ArrayList
import kotlinx.android.synthetic.main.item_list_single.view.*

/**
 * Adapter used to map a String to a text view.
 */
class SimpleStringAdapter(private val mContext: Context) : RecyclerView.Adapter<SimpleStringAdapter.ViewHolder>() {
    private val mStrings = ArrayList<String>()

    fun setStrings(newStrings: List<String>) {
        mStrings.clear()
        mStrings.addAll(newStrings)
        notifyDataSetChanged()
    }

    fun addString(newString: String) {
        mStrings.add(newString)
        notifyDataSetChanged()
    }

    fun clear() {
        mStrings.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_single, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return mStrings.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(position: Int) {
            itemView.mItemName.text = mStrings[position]
            itemView.setOnClickListener { Toast.makeText(mContext, mStrings[position], Toast.LENGTH_SHORT).show() }
        }

    }
}
