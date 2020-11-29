package com.example.csci571andriodstocks;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity {

    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;

    public static final String EXTRA_TICKER = "com.example.csci571andriodstocks.MESSAGE";
    public static final String SEARCH_URL = "https://csci571-trading-platform.wl.r.appspot.com/api/search/";
    public static final String PRICE_URL = "https://csci571-trading-platform.wl.r.appspot.com/api/price/";
    public static final int TOT_API_CALLS = 2;

    private SectionedRecyclerViewAdapter sectionedAdapter;
    private FavoriteSection favoriteSection;
    private PortfolioSection portFolioSection;
    private AutoSuggestAdapter autoSuggestAdapter;
    private Handler handler;
    private Handler handler2 = new Handler();
    private LocalStorage storage;
    private List<Company> favouriteList;
    private List<Company> portfolioList;
    private String netWorth;
    private double cash;
    private Map<String, String> fromStorageFavorite;
    private Map<String, String> fromStoragePortfolio;
//    private String tickers;
    private ProgressBar spinner;
    private RecyclerView recyclerView;
    private TextView date;
    private Context ctx;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private int numApiCalls;
    private boolean isApiFailed;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        setContentView(R.layout.activity_main);
        ctx = this;
        sharedPreferences = getSharedPreferences(LocalStorage.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        storage = new LocalStorage(sharedPreferences, editor);
        date  = (TextView) findViewById(R.id.date_view_id);
        spinner = (ProgressBar)findViewById(R.id.progressbar);


        init();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRestart() {
        super.onRestart();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
        init();
        Log.e("RESTART","Activity restarted");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void init() {
        numApiCalls = 0;
        isApiFailed = false;

        String date_n = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());

        //set it as current date.
        date.setText(date_n);

        sectionedAdapter = new SectionedRecyclerViewAdapter();
        this.favouriteList = new ArrayList<>();
        this.portfolioList = new ArrayList<>();
        netWorth = sharedPreferences.getString(LocalStorage.CASH_IN_HAND, "20000.00");
        cash = Double.parseDouble(netWorth);
        Log.e("NETWORH", "networth: " + netWorth);

        portFolioSection = new PortfolioSection("PORTFOLIO", this.portfolioList, netWorth,
                (company, itemAdapterPosition) -> redirectToDetails(company.ticker));
        favoriteSection = new FavoriteSection("FAVORITES", this.favouriteList,
                (company, itemAdapterPosition) -> redirectToDetails(company.ticker));


        sectionedAdapter.addSection(portFolioSection);
        sectionedAdapter.addSection(favoriteSection);
        recyclerView = (RecyclerView) findViewById(R.id.rvHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(sectionedAdapter);

        spinner.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        date.setVisibility(View.GONE);

        fromStorageFavorite = LocalStorage.getFromStorage(LocalStorage.FAVOURITES);
        fromStoragePortfolio = LocalStorage.getFromStorage(LocalStorage.PORTFOLIO);

        String favoriteTickers = String.join(",", fromStorageFavorite.keySet());
        String portfolioTickers = String.join(",", fromStoragePortfolio.keySet());

        Log.i("FAV LEN", "..................." + favoriteTickers);

        makeApiCallPrice(LocalStorage.FAVOURITES, favoriteTickers);
        makeApiCallPrice(LocalStorage.PORTFOLIO, portfolioTickers);
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

        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);

        mSearchAutoComplete.setAdapter(autoSuggestAdapter);

        mSearchAutoComplete.setOnItemClickListener((parent, view, position, id) ->
                mSearchAutoComplete.setText(autoSuggestAdapter.getObject(position).toString()));

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

        handler = new Handler( msg -> {
            if (msg.what == TRIGGER_AUTO_COMPLETE) {

                String input = mSearchAutoComplete.getText().toString();

                if (!TextUtils.isEmpty(mSearchAutoComplete.getText()) && input.length() >= 3) {
                    Log.i("API", "...................API CALL: "+ input );

                    makeApiCall(input);
                }
            }
            return false;
        });

        return true;
    }

//    @Override
//    public void onItemRootViewClicked(@NonNull final WatchlistSection section, final int itemAdapterPosition) {
//
//        Log.i("clicked event", "got a click at position " + itemAdapterPosition);
//    }

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


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void makeApiCallPrice(String key, String tickers) {

        Log.e("TICKERS", "key: " + key + "tickers: " + tickers);
        String[] tickersArray = tickers.split(",");
        Map<String, Integer> tickerToPosition = new HashMap<>();
        List<Company> companiesList = new ArrayList<>();

        // need this otherwise to make sure that the orderer of tickers is same as the one they
        // were inserted in.
        for (int i = 0; i < tickersArray.length; i++){
            tickerToPosition.put(tickersArray[i], i);
            companiesList.add(new Company());
        }


        if (tickers.length() == 0){
            numApiCalls++;

            if (isApiFailed || numApiCalls == TOT_API_CALLS){

                numApiCalls = 0;
                isApiFailed = false;
                spinner.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                date.setVisibility(View.VISIBLE);
            }
            return;
        }

        ApiCall.make(this, tickers, PRICE_URL, response -> {
            //parsing logic, please change it as per your requirement
            double stockValue = 0;

            Log.e("SIZE..", "company list size " + companiesList.size() + "should be: " + tickersArray.length);

            try {
                JSONObject responseObject = new JSONObject(response);
                JSONArray array = responseObject.getJSONArray("results");
                Log.i("length:", "len: " + array.length());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject row = array.getJSONObject(i);
                    String ticker = row.getString("ticker");
                    String last = (row.getString("last") != "null") ? row.getString("last") : row.getString("tngoLast");
                    String prevClose = row.getString("prevClose");
                    String name = "";
                    String shares = "";
                    String name_or_shares = "";

                    Log.e("NULL", "this is null " + (row.getString("last")));

                    if (key.equals(LocalStorage.FAVOURITES)){
                        name = fromStorageFavorite.get(ticker);
                        name_or_shares = fromStoragePortfolio.getOrDefault(ticker, fromStorageFavorite.get(ticker));
                    }else{
                        shares = fromStoragePortfolio.get(ticker);
                        name_or_shares = fromStoragePortfolio.get(ticker);
                        stockValue += Double.parseDouble(last) * Double.parseDouble(shares);
                    }

                    Log.e("COMPANY", ticker + "--" + last + "--" + prevClose + "--" + name +"--" + shares);

                    DecimalFormat df = new DecimalFormat("####0.00");
                    Company newCompany = new Company(name, ticker,shares, last, prevClose, name_or_shares, ctx);

                    newCompany.last = df.format(Double.parseDouble(newCompany.last));
                    newCompany.change = Double.parseDouble(df.format(newCompany.change));

                    companiesList.set(tickerToPosition.get(ticker), newCompany);

                }
            } catch (Exception e) {
                isApiFailed = true;
                e.printStackTrace();
            }

            if (key.equals(LocalStorage.FAVOURITES)){

                favouriteList.clear();
                favouriteList.addAll(companiesList);
                sectionedAdapter.getAdapterForSection(favoriteSection).notifyAllItemsChanged();
                Log.e("ERROR_DBUG", "size: " + favouriteList.size() + "company: " + favouriteList.get(0).name);
            }else{
                portfolioList.clear();
                portfolioList.addAll(companiesList);
                DecimalFormat df = new DecimalFormat("####0.00");
                netWorth = df.format(cash + stockValue);
                portFolioSection.setNetWorth(netWorth);
                Log.e("NETWORTH", netWorth);
                sectionedAdapter.getAdapterForSection(portFolioSection).notifyAllItemsChanged();
            }

            numApiCalls++;

            if (isApiFailed || numApiCalls == TOT_API_CALLS){
                numApiCalls = 0;
                isApiFailed = false;
                spinner.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                date.setVisibility(View.VISIBLE);
            }

        }, error -> {
            isApiFailed = true;
            Log.i("error", "error in search http " + error);
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