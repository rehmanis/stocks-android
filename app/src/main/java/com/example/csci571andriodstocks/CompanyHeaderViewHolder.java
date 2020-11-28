package com.example.csci571andriodstocks;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CompanyHeaderViewHolder extends RecyclerView.ViewHolder {

    final TextView tvTitle;
    final TextView tvNetWorth;

    CompanyHeaderViewHolder(@NonNull View view) {
        super(view);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvNetWorth = view.findViewById(R.id.tv_net_worth);
    }
}
