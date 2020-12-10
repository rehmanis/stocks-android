package com.example.csci571andriodstocks;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Collections;
import java.util.List;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class FavoriteSection extends Section implements ItemMoveCallback.ItemTouchHelperContract {

    private final String title;
    private final List<Company> list;
    private final ClickListener clickListener;
    private final Context ctx;

    public FavoriteSection(@NonNull final String title, @NonNull final List<Company> list,
                           Context ctx,
                           @NonNull final ClickListener clickListener) {
        // call constructor with layout resources for this Section header and items
        super(SectionParameters.builder()
                .itemResourceId(R.layout.item_company)
                .headerResourceId(R.layout.favorite_sec_header)
                .build());

        this.title = title;
        this.list = list;
        this.clickListener = clickListener;
        this.ctx = ctx;
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


        itemHolder.rootView.setOnClickListener(v ->
                clickListener.onItemRootViewClicked(company, itemHolder.getAdapterPosition())
        );

        itemHolder.btnGoTo.setOnClickListener(v ->
                clickListener.onItemRootViewClicked(company, itemHolder.getAdapterPosition())
        );



    }


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

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(list, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(list, i, i - 1);
            }
        }
    }

    @Override
    public void onRowSelected(CompanyItemViewHolder myViewHolder) {
        myViewHolder.rootView.setBackgroundColor(Color.GRAY);
    }

    @Override
    public void onRowClear(CompanyItemViewHolder myViewHolder) {
        myViewHolder.rootView.setBackgroundColor(ResourcesCompat.getColor(ctx.getResources(), R.color.grey, null));
    }

    interface ClickListener {

        void onItemRootViewClicked(Company company, final int itemAdapterPosition);
    }



}
