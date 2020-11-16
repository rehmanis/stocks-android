package com.example.csci571andriodstocks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity implements WatchlistSection.ClickListener {

    private SectionedRecyclerViewAdapter sectionedAdapter;
    String restaurants[] = {
            "KFC",
            "Dominos",
            "Pizza Hut",
            "Burger King",
            "Subway",
            "Dunkin' Donuts",
            "Starbucks",
            "Cafe Coffee Day"
    };


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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ArrayAdapterSearchView searchView = (ArrayAdapterSearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, restaurants);
        searchView.setAdapter(adapter);

        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                searchView.setText(adapter.getItem(position).toString());

            }
        });

        return true;
    }

    @Override
    public void onItemRootViewClicked(@NonNull final WatchlistSection section, final int itemAdapterPosition) {

        Log.i("clicked event", "got a click at position " + itemAdapterPosition);
    }
}