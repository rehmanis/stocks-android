package com.example.csci571andriodstocks;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CompanyItemViewHolder extends RecyclerView.ViewHolder {

    final View rootView;
    final ImageView imgItem;
    final TextView ticker;
    final TextView shares_or_name;
    final TextView change;
    final TextView price;

    CompanyItemViewHolder(@NonNull View view) {
        super(view);

        rootView = view;
        imgItem = view.findViewById(R.id.img_change_arrow);
        ticker = view.findViewById(R.id.company_ticker);
        shares_or_name = view.findViewById(R.id.company_name_or_shares);
        change = view.findViewById(R.id.company_price_change);
        price = view.findViewById(R.id.company_price);
    }
}
