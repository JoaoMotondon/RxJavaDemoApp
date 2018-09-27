package com.motondon.rxjavademoapp.view.adapter

import android.content.Intent
import android.support.v4.util.Pair
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.main.ExampleByCategoryActivity
import com.motondon.rxjavademoapp.view.main.CategoryItem
import com.motondon.rxjavademoapp.view.main.MainActivity

import java.util.ArrayList

import kotlinx.android.synthetic.main.item_cardview_options.view.*

class MainActivityAdapter(private val mMainActivity: MainActivity, mExampleCategoriesList: List<Pair<List<CategoryItem>, Pair<String, String>>>) : RecyclerView.Adapter<MainActivityAdapter.ViewHolder>() {

    private var mExampleCategoriesList = ArrayList<Pair<List<CategoryItem>, Pair<String, String>>>()

    init {
        this.mExampleCategoriesList = mExampleCategoriesList as ArrayList<Pair<List<CategoryItem>, Pair<String, String>>>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cardview_options, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return mExampleCategoriesList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            // Not too much to say here. We just get the item and set a listener for it. When user clicks on it, he will be redirected
            // to the activity that implements the selected example.
            val categoriesDetailsList = mExampleCategoriesList[position]

            val categoryItemList = categoriesDetailsList.first
            val item = categoriesDetailsList.second

            itemView.mCategoryName.text = item.first
            itemView.mCategoryDetails.text = item.second

            itemView.setOnClickListener { _ ->
                val exampleByCategoryIntent = Intent(mMainActivity.applicationContext, ExampleByCategoryActivity::class.java)

                exampleByCategoryIntent.putExtra("CATEGORY_ITEMS", categoryItemList as ArrayList<*>)
                exampleByCategoryIntent.putExtra("TITLE", itemView.mCategoryName.text.toString())

                mMainActivity.startActivity(exampleByCategoryIntent)
            }
        }
    }
}
