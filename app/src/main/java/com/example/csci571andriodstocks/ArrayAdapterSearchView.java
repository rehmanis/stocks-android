package com.example.csci571andriodstocks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.CursorAdapter;

public class ArrayAdapterSearchView extends SearchView {

    private SearchView.SearchAutoComplete mSearchAutoComplete;

    public ArrayAdapterSearchView(Context context) {
        super(context);
        initialize();
    }

    public ArrayAdapterSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    @SuppressLint("RestrictedApi")
    public void initialize() {
        mSearchAutoComplete = findViewById(R.id.search_src_text);
        mSearchAutoComplete.setDropDownBackgroundResource(R.color.white);
        mSearchAutoComplete.setThreshold(0);
        this.setAdapter(null);
        this.setOnItemClickListener(null);
    }

    @Override
    public void setSuggestionsAdapter(CursorAdapter adapter) {
        // don't let anyone touch this
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mSearchAutoComplete.setOnItemClickListener(listener);
    }

    public void setAdapter(ArrayAdapter<?> adapter) {
        mSearchAutoComplete.setAdapter(adapter);
    }

    public void setText(String text) {
        mSearchAutoComplete.setText(text);
    }

}