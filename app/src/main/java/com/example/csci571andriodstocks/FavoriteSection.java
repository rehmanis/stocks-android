package com.example.csci571andriodstocks;

import android.graphics.Color;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class FavoriteSection extends Section {

    private final String title;
    private final List<Company> list;
    private final ClickListener clickListener;


    public FavoriteSection(@NonNull final String title, @NonNull final List<Company> list,
                           @NonNull final ClickListener clickListener) {
        // call constructor with layout resources for this Section header and items
        super(SectionParameters.builder()
                .itemResourceId(R.layout.item_company)
                .headerResourceId(R.layout.favorite_sec_header)
                .build());

        this.title = title;
        this.list = list;
        this.clickListener = clickListener;
    }



    @Override
    public int getContentItemsTotal() {
        return list.size(); // number of items of this section
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        // return a custom instance of ViewHolder for the items of this section
        return new CompanyItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        final CompanyItemViewHolder itemHolder = (CompanyItemViewHolder) holder;

        final Company company = list.get(position);

        itemHolder.ticker.setText(company.ticker);
        itemHolder.imgItem.setImageResource(company.arrow);
        itemHolder.change.setText(String.valueOf(company.change));
        itemHolder.change.setTextColor(company.changeColor);
        itemHolder.shares_or_name.setText(company.name_or_shares);
        itemHolder.last.setText(company.last);

        if (position == 0){
            itemHolder.dividerLine.setVisibility(View.GONE);
        }

        Log.i("VIEWCOLOR", "...........color" + company.changeColor);


        itemHolder.rootView.setOnClickListener(v ->
                clickListener.onItemRootViewClicked(company, itemHolder.getAdapterPosition())
        );



    }

//
//    private void enableSwipeToDeleteAndUndo() {
//        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
//
//                final int position = viewHolder.getAdapterPosition();
//                Log.e("POSITION", "my position: " + position);
//                final Company item = favoriteSection.getData().get(position);
//                Log.e("SIZE", "my position: " + favoriteSection.getData().size());
//
//                favoriteSection.removeItem(position);
//                sectionedAdapter.getAdapterForSection(favoriteSection).notifyItemRemoved(position);
//
//                Snackbar snackbar = Snackbar
//                        .make(coordinatorLayout, "Item was removed from the list.", Snackbar.LENGTH_LONG);
//                snackbar.setAction("UNDO", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        favoriteSection.restoreItem(item, position);
//                        recyclerView.scrollToPosition(position);
//                    }
//                });
//
//                snackbar.setActionTextColor(Color.YELLOW);
//                snackbar.show();
//
//            }
//        };
//
//        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
//        itemTouchhelper.attachToRecyclerView(recyclerView);
//    }

//    @Override
//    public void onBindItemViewHolder(final RecyclerView.ViewHolder holder, final int position,
//                                     final List<Object> payloads) {
//
//        final CompanyItemViewHolder itemHolder = (CompanyItemViewHolder) holder;
//
//        Company company = list.get(position);
//
//        for (Object obj : payloads) {
//            if (obj instanceof ItemPriceUpdate) {
//                itemHolder.ticker.setText(company.ticker);
//                itemHolder.shares_or_name.setText(company.name);
//                itemHolder.imgItem.setImageResource(company.arrow);
//                itemHolder.change.setText(String.valueOf(company.change));
//                itemHolder.change.setTextColor(company.changeColor);
//                itemHolder.last.setText(company.last);
//            }
//        }
//
//        itemHolder.btnGoTo.setOnClickListener(v ->
//                clickListener.onItemRootViewClicked(company, itemHolder.getAdapterPosition())
//        );
//    }



//    void updateItemPrice(final int index, final String ticker, final String name_shares, final String last,
//                         final double change, final @ColorInt int changeColor, final @DrawableRes int arrow) {
//        Company watchItem = list.get(index);
//
//        watchItem.ticker = ticker;
//        watchItem.name_or_shares = name_shares;
//        watchItem.last = last;
//        watchItem.change = change;
//        watchItem.changeColor = changeColor;
//        watchItem.arrow = arrow;
//
//    }
//
//    static class ItemPriceUpdate {
//    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        // return an empty instance of ViewHolder for the headers of this section
        return new CompanyHeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(final RecyclerView.ViewHolder holder) {
        final CompanyHeaderViewHolder headerHolder = (CompanyHeaderViewHolder) holder;

        headerHolder.tvTitle.setText(title);
    }

    public void removeItem(int position) {
        list.remove(position);


    }

    public void restoreItem(Company item, int position) {
        list.add(position, item);
    }

    public List<Company> getData() {
        return list;
    }

    interface ClickListener {

        void onItemRootViewClicked(Company company, final int itemAdapterPosition);
    }

    interface swipeToDeleteCallback {

        void onItemSwiped(Company company, final int itemAdapterPosition);
    }

}
