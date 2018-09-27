package com.motondon.rxjavademoapp.view.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.main.CategoryItem
import kotlinx.android.synthetic.main.item_category_list.view.*

class CategoryItemsAdapter(private val mContext: Context, private val mCategoryItemList: List<CategoryItem>) : RecyclerView.Adapter<CategoryItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(mContext).inflate(R.layout.item_category_list, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return mCategoryItemList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int) {
            itemView.mCategoryItemName.text = mCategoryItemList[position].mExampleName
            itemView.mCategoryItemDetails.text = mCategoryItemList[position].mExampleDetails

            // When user clicks on an example, extract a class that implements that example and call it by using an intent.
            itemView.setOnClickListener { _ ->
                val exampleIntent = Intent(mContext, mCategoryItemList[position].mExampleActivityClass)
                exampleIntent.putExtra("TITLE", mCategoryItemList[position].mExampleName)

                mContext.startActivity(exampleIntent)
            }
        }
    }
}
