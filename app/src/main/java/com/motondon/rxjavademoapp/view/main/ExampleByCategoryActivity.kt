package com.motondon.rxjavademoapp.view.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.adapter.CategoryItemsAdapter

import java.util.ArrayList

import kotlinx.android.synthetic.main.activity_example_by_category.*

class ExampleByCategoryActivity : AppCompatActivity() {

    private var title: String? = null
    private var categoryItems = ArrayList<CategoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example_by_category)

        if (supportActionBar != null) {

            if (savedInstanceState != null) {
                title = savedInstanceState.getString("TITLE")
                categoryItems = savedInstanceState.getSerializable("CATEGORY_ITEMS") as ArrayList<CategoryItem>
            } else {
                title = intent.extras?.getString("TITLE")

                // Get the examples lists from the intent and set it to the adapter. They will be available to users and will allow them to
                // click over an option and be redirected to an activity which implements that example.
                categoryItems = intent.extras?.getSerializable("CATEGORY_ITEMS") as ArrayList<CategoryItem>
            }
            supportActionBar?.title = title
        }

        examplesByCategoryList.setHasFixedSize(true)
        examplesByCategoryList.layoutManager = LinearLayoutManager(this)
        examplesByCategoryList.adapter = CategoryItemsAdapter(this, categoryItems)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("TITLE", title)
        outState.putSerializable("CATEGORY_ITEMS", categoryItems)
    }
}
