package com.example.csci571andriodstocks;

import android.view.View;
import android.widget.Button;
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
    final TextView last;
    final Button btnGoTo;
    final View dividerLine;


    CompanyItemViewHolder(@NonNull View view) {
        super(view);

        rootView = view;
        imgItem = view.findViewById(R.id.img_change_arrow);
        ticker = view.findViewById(R.id.company_ticker);
        shares_or_name = view.findViewById(R.id.company_name_or_shares);
        change = view.findViewById(R.id.company_price_change);
        last = view.findViewById(R.id.company_price);
        btnGoTo = view.findViewById(R.id.btn_goTo);
        dividerLine = view.findViewById(R.id.divider_line);
    }
}
