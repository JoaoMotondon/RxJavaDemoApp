package com.motondon.rxjavademoapp.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.motondon.rxjavademoapp.R;
import com.motondon.rxjavademoapp.view.main.CategoryItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryItemsAdapter extends RecyclerView.Adapter<CategoryItemsAdapter.ViewHolder> {

    private Context mContext;
    private List<CategoryItem> mCategoryItemList;

    public CategoryItemsAdapter(Context context, List<CategoryItem> categoryItemList) {
        mContext = context;
        mCategoryItemList = categoryItemList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(mContext)
                .inflate(R.layout.item_category_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mCategoryItemName.setText(mCategoryItemList.get(position).mExampleName);
        holder.mCategoryItemDetails.setText(mCategoryItemList.get(position).mExampleDetails);

        // When user clicks on an example, extract a class that implements that example and call it by using an intent.
        holder.itemView.setOnClickListener((v) -> {
            Intent exampleIntent = new Intent(mContext, mCategoryItemList.get(position).mExampleActivityClass);
            exampleIntent.putExtra("TITLE", mCategoryItemList.get(position).mExampleName);

            mContext.startActivity(exampleIntent);
        });
    }

    @Override
    public int getItemCount() {
        return mCategoryItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item) TextView mCategoryItemName;
        @BindView(R.id.item_details) TextView mCategoryItemDetails;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
