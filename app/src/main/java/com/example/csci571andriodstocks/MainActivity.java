package com.example.csci571andriodstocks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity implements WatchlistSection.ClickListener {

    private SectionedRecyclerViewAdapter sectionedAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sectionedAdapter = new SectionedRecyclerViewAdapter();

        // Add your Sections
        final List<Company> companies = new LoadCompany().getContactsWithLetter();
        sectionedAdapter.addSection(new WatchlistSection("Portfolio", companies, this));
        sectionedAdapter.addSection(new WatchlistSection("Watchlist", companies, this));

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rvHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(sectionedAdapter);

    }

    @Override
    public void onItemRootViewClicked(@NonNull final WatchlistSection section, final int itemAdapterPosition) {

        Log.i("clicked event", "got a click at position " + itemAdapterPosition);
    }
}