package com.motondon.rxjavademoapp.view.adapter;

import android.content.Intent;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.motondon.rxjavademoapp.R;
import com.motondon.rxjavademoapp.view.main.ExampleByCategoryActivity;
import com.motondon.rxjavademoapp.view.main.CategoryItem;
import com.motondon.rxjavademoapp.view.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.ViewHolder> {

    private List<Pair<List<CategoryItem>, Pair<String, String>>> mExampleCategoriessList = new ArrayList<>();
    private MainActivity mMainActivity;

    public MainActivityAdapter(MainActivity mainActivity, List<Pair<List<CategoryItem>, Pair<String, String>>> mExampleCategoriessList) {
        this.mExampleCategoriessList = mExampleCategoriessList;
        this.mMainActivity = mainActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview_options, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        // Not too much to say here. We just get the item and set a listener for it. When user clicks on it, he will be redirected
        // to the activity that implements the selected example.
        Pair<List<CategoryItem>, Pair<String, String>> categoriesDetailsList = mExampleCategoriessList.get(position);

        final List<CategoryItem> categoryItemList = categoriesDetailsList.first;
        Pair<String, String> item = categoriesDetailsList.second;

        holder.categoryName.setText(item.first);
        holder.categoryDetails.setText(item.second);

        holder.itemView.setOnClickListener((v) -> {
            Intent exampleByCategoryIntent = new Intent(mMainActivity.getApplicationContext(), ExampleByCategoryActivity.class);

            exampleByCategoryIntent.putExtra("CATEGORY_ITEMS",(ArrayList) categoryItemList);
            exampleByCategoryIntent.putExtra("TITLE", holder.categoryName.getText().toString());

            mMainActivity.startActivity(exampleByCategoryIntent);
        });
    }

    @Override
    public int getItemCount() {
        return mExampleCategoriessList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.category_name) TextView categoryName;
        @BindView(R.id.category_details) TextView categoryDetails;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
