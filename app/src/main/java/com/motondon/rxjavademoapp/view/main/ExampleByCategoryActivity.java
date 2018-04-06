package com.motondon.rxjavademoapp.view.main;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.motondon.rxjavademoapp.R;
import com.motondon.rxjavademoapp.view.adapter.CategoryItemsAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExampleByCategoryActivity extends AppCompatActivity {

    @BindView(R.id.example_by_category_list) RecyclerView examplesByCategoryList;

    private  String title;
    private ArrayList<CategoryItem> categoryItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_by_category);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            if (savedInstanceState != null) {
                title = savedInstanceState.getString("TITLE");
                categoryItems = (ArrayList<CategoryItem>) savedInstanceState.getSerializable("CATEGORY_ITEMS");
            } else {
                title = getIntent().getExtras().getString("TITLE");

                // Get the examples lists from the intent and set it to the adapter. They will be available to users and will allow them to
                // click over an option and be redirected to an activity which implements that example.
                categoryItems = (ArrayList<CategoryItem>) getIntent().getExtras().getSerializable("CATEGORY_ITEMS");
            }
            actionBar.setTitle(title);
        }

        examplesByCategoryList.setHasFixedSize(true);
        examplesByCategoryList.setLayoutManager(new LinearLayoutManager(this));
        examplesByCategoryList.setAdapter(new CategoryItemsAdapter(this, categoryItems));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("TITLE", title);
        outState.putSerializable("CATEGORY_ITEMS", categoryItems);
    }
}
