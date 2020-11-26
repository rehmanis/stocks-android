package com.example.csci571andriodstocks;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;

import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity implements WatchlistSection.ClickListener {

    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    public static final String SHARED_PREFS_FILE = "mypref";
    public static final String EXTRA_TICKER = "com.example.csci571andriodstocks.MESSAGE";
    public static final String SEARCH_URL = "https://csci571-trading-platform.wl.r.appspot.com/api/search/";
    public static final String PRICE_URL = "https://csci571-trading-platform.wl.r.appspot.com/api/price/";

    private SectionedRecyclerViewAdapter sectionedAdapter;
    private WatchlistSection watchListSection;
    private AutoSuggestAdapter autoSuggestAdapter;
    private Handler handler;
    private Handler handler2 = new Handler();
    private LocalStorage storage;
    private List<Company> favouriteList;
    private Map<String, String> fromStorageFav;
    private String tickers;
    private ProgressBar spinner;
    private RecyclerView recyclerView;
    private TextView date;
    private Context ctx;



    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

//    private final Runnable watchListUpdatesRunnable = new Runnable() {
//        @Override
//        public void run() {
//            makeApiCallPrice();
////            handler.post(watchListUpdatesRunnable);
//            handler2.postDelayed(watchListUpdatesRunnable, TimeUnit.SECONDS.toMillis(1));
//        }
//    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        setContentView(R.layout.activity_main);
        ctx = this;

        // load tasks from preference
        sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        spinner = (ProgressBar)findViewById(R.id.progressbar);

        storage = new LocalStorage(sharedPreferences, editor);
        fromStorageFav = LocalStorage.getFromStorage(LocalStorage.FAVOURITES);
        this.tickers = String.join(",", fromStorageFav.keySet());

        Log.i("TICKERS", "..................." + tickers);

        this.favouriteList = new ArrayList<>(fromStorageFav.keySet().size());
        Log.i("FAV LEN", "..................." + this.favouriteList.size());

        //create a date string.
        String date_n = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
        //get hold of textview.
        date  = (TextView) findViewById(R.id.date_view_id);
        //set it as current date.
        date.setText(date_n);

        sectionedAdapter = new SectionedRecyclerViewAdapter();
        watchListSection = new WatchlistSection("FAVORITES", this.favouriteList , this);

        // Add your Sections
//        final List<Company> companies = new LoadCompany(this, sectionedAdapter).getCompanies(LocalStorage.FAVOURITES);

//        sectionedAdapter.addSection(new WatchlistSection("Portfolio", companies, this));
        makeApiCallPrice(LocalStorage.FAVOURITES);
        sectionedAdapter.addSection(watchListSection);

        recyclerView = (RecyclerView) findViewById(R.id.rvHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(sectionedAdapter);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        AutoCompleteTextView mSearchAutoComplete = (AutoCompleteTextView) searchView.findViewById(R.id.search_src_text);

//        SearchView.SearchAutoComplete mSearchAutoComplete = searchView.findViewById(R.id.search_src_text);

//        ImageView searchIcon = (ImageView)searchView.findViewById(R.id.search_mag_icon);
//        searchIcon.setAdjustViewBounds(true);
//        searchIcon.setImageResource(R.drawable.ic_search_black_24dp);

//        ImageView searchIcon = (ImageView)searchView.findViewById(R.id.search_mag_icon);
//        searchIcon.setImageDrawable(R.drawable.ic_search_black_24dp);

//        searchView.setQueryHint("");

        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, restaurants);
        mSearchAutoComplete.setAdapter(autoSuggestAdapter);

        mSearchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mSearchAutoComplete.setText(autoSuggestAdapter.getObject(position).toString());
            }
        });

        mSearchAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i("well", " this worked " + query);
                String[] input = query.split("-");
                redirectToDetails(input[0]);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {

                    String input = mSearchAutoComplete.getText().toString();

                    if (!TextUtils.isEmpty(mSearchAutoComplete.getText()) && input.length() >= 3) {
                        Log.i("API", "...................API CALL: "+ input );

                        makeApiCall(input);
                    }
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public void onItemRootViewClicked(@NonNull final WatchlistSection section, final int itemAdapterPosition) {

        Log.i("clicked event", "got a click at position " + itemAdapterPosition);
    }

    private void makeApiCall(String text) {
        ApiCall.make(this, text, SEARCH_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //parsing logic, please change it as per your requirement
                List<String> stringList = new ArrayList<>();
                try {
                    JSONObject responseObject = new JSONObject(response);
                    JSONArray array = responseObject.getJSONArray("results");
                    Log.i("length:", "len: " + array.length());
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject row = array.getJSONObject(i);

                        stringList.add(row.getString("ticker") + "-" + row.getString("name"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //IMPORTANT: set data here and notify
                autoSuggestAdapter.setData(stringList);
                autoSuggestAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("error", "error in search http " + error);
            }
        });
    }


    private void makeApiCallPrice(String key) {

        ApiCall.make(this, tickers, PRICE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //parsing logic, please change it as per your requirement
                List<Company> companiesList = new ArrayList<>();

                try {
                    JSONObject responseObject = new JSONObject(response);
                    JSONArray array = responseObject.getJSONArray("results");
                    Log.i("length:", "len: " + array.length());
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject row = array.getJSONObject(i);
                        String ticker = row.getString("ticker");
                        String last = row.getString("last");
                        String prevClose = row.getString("prevClose");
                        String name = "";

                        if (key.equals(LocalStorage.FAVOURITES)){
                            name = fromStorageFav.get(ticker);
                        }

                        Company newCompany = new Company(name, ticker,"", last, prevClose, "", ctx);
                        companiesList.add(newCompany);
//                        watchListSection.updateItemPrice(i, newCompany.ticker, newCompany.name, newCompany.last,
//                                newCompany.change, newCompany.changeColor, newCompany.arrow);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                favouriteList.clear();
                favouriteList.addAll(companiesList);
//                //IMPORTANT: set data here and notify
//                autoSuggestAdapter.setData(stringList);
                sectionedAdapter.getAdapterForSection(watchListSection).notifyAllItemsChanged(
                        new WatchlistSection.ItemPriceUpdate());
                spinner.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                date.setVisibility(View.VISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("error", "error in search http " + error);
            }
        });
    }

//    private void getWatchListUpdates() {
//
//        for (int i = 0; i < favouriteList.size(); i++) {
//            final Company item = favouriteList.get(i);
//            watchListSection.updateItemPrice(i, item.last, item.change, item.changeColor, item.arrow);
//        }
//
//        sectionedAdapter.getAdapterForSection(watchListSection).notifyAllItemsChanged(
//                new WatchlistSection.ItemPriceUpdate());
//    }



    private void handleIntent(Intent intent) {

        Log.i("INTENT", "use entered the search "  + intent.ACTION_SEARCH + " " + intent.getAction());

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            Log.i("SEARCH", "use entered the search " + query);
            Toast toast = Toast.makeText(this, "search done", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    public void redirectToDetails(String ticker) {

        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(EXTRA_TICKER, ticker);
        startActivity(intent);

    }



}