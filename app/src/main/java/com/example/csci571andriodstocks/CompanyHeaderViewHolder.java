package com.example.csci571andriodstocks;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CompanyHeaderViewHolder extends RecyclerView.ViewHolder {

    final TextView tvTitle;

    CompanyHeaderViewHolder(@NonNull View view) {
        super(view);
        tvTitle = view.findViewById(R.id.tvTitle);
    }
}
