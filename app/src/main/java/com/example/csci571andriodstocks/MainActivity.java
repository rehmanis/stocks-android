package com.example.csci571andriodstocks;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity {

    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private static final long AUTO_REFRESH_PERIOD_MSEC = 15000;

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
//    private ProgressBar spinner;
    private long counter;
    private RecyclerView recyclerView;
    private NestedScrollView homeViewContainer;
    private TextView date;
    private Context ctx;
    private View spinnerContainer;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private int numApiCalls;
    private boolean isApiFailed;
    private Timer timer;
    private boolean isSelectedFromList;
    CoordinatorLayout coordinatorLayout;
    final Handler myHandler = new Handler();
    private Runnable myRunnable;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        handleIntent(getIntent());
        setContentView(R.layout.activity_main);
        isSelectedFromList = false;
        ctx = this;
        sharedPreferences = getSharedPreferences(LocalStorage.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        storage = new LocalStorage(sharedPreferences, editor);
        date  = (TextView) findViewById(R.id.date_view_id);
        spinnerContainer = findViewById(R.id.progressbar_container);
//        spinner = (ProgressBar)findViewById(R.id.progressbar);
        homeViewContainer = findViewById(R.id.container_home);
        coordinatorLayout = findViewById(R.id.main_coordinator_layout);
        TextView tvTingo = findViewById(R.id.tv_tingo);

        tvTingo.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://www.tiingo.com/"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
//        myToolbar.bringToFront();
        setSupportActionBar(myToolbar);

        init();
        recyclerView.addItemDecoration(new CustomDividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        enableSwipeToDeleteAndUndo();
        enableItemDragFavorite();
        enableItemDragPortfolio();

        myRunnable = new Runnable() {
            public void run() {
                fromStorageFavorite = LocalStorage.getFromStorage(LocalStorage.FAVOURITES);
                fromStoragePortfolio = LocalStorage.getFromStorage(LocalStorage.PORTFOLIO);


                String favTickers = String.join(",", fromStorageFavorite.keySet());
                String portTickers = String.join(",", fromStoragePortfolio.keySet());

                counter++;
                Log.e("AUTO-REFRESESH", "-----------Making API CALL #" + counter);
                makeApiCallPrice(LocalStorage.FAVOURITES, favTickers);
                makeApiCallPrice(LocalStorage.PORTFOLIO, portTickers);
            }
        };
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void UpdateGUI() {
//        fromStorageFavorite = LocalStorage.getFromStorage(LocalStorage.FAVOURITES);
//        fromStoragePortfolio = LocalStorage.getFromStorage(LocalStorage.PORTFOLIO);
//
//
//        String favTickers = String.join(",", fromStorageFavorite.keySet());
//        String portTickers = String.join(",", fromStoragePortfolio.keySet());
//
//        counter++;
//        Log.e("AUTO-REFRESESH", "-----------Making API CALL #" + counter);
//        makeApiCallPrice(LocalStorage.FAVOURITES, favTickers);
//        makeApiCallPrice(LocalStorage.PORTFOLIO, portTickers);
        myHandler.post(myRunnable);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRestart() {
        super.onRestart();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
//        isSelectedFromList = true;
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

//        portFolioSection = new PortfolioSection("PORTFOLIO", this.portfolioList, netWorth, new PortfolioSection.ClickListener() {
//            @Override
//            public void onItemRootViewClicked(Company company, int itemAdapterPosition) {
//
//            }
//        });

        portFolioSection = new PortfolioSection("PORTFOLIO", this.portfolioList, netWorth, ctx,
                (company, itemAdapterPosition) -> redirectToDetails(company.ticker));
                favoriteSection = new FavoriteSection("FAVORITES", this.favouriteList, ctx,
                        (company, itemAdapterPosition) -> redirectToDetails(company.ticker));


        sectionedAdapter.addSection(portFolioSection);
        sectionedAdapter.addSection(favoriteSection);
        recyclerView = (RecyclerView) findViewById(R.id.rvHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(sectionedAdapter);




        spinnerContainer.setVisibility(View.VISIBLE);
//        recyclerView.setVisibility(View.GONE);
//        date.setVisibility(View.GONE);
        homeViewContainer.setVisibility(View.GONE);

        fromStorageFavorite = LocalStorage.getFromStorage(LocalStorage.FAVOURITES);
        fromStoragePortfolio = LocalStorage.getFromStorage(LocalStorage.PORTFOLIO);


        String favoriteTickers = String.join(",", fromStorageFavorite.keySet());
        String portfolioTickers = String.join(",", fromStoragePortfolio.keySet());

//        Log.i("FAV LEN", "..................." + favoriteTickers);

        makeApiCallPrice(LocalStorage.FAVOURITES, favoriteTickers);
        makeApiCallPrice(LocalStorage.PORTFOLIO, portfolioTickers);






        timer = new Timer();
        counter = 0;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // do your task here
                UpdateGUI();

//                fromStorageFavorite = LocalStorage.getFromStorage(LocalStorage.FAVOURITES);
//                fromStoragePortfolio = LocalStorage.getFromStorage(LocalStorage.PORTFOLIO);
//
//
//                String favTickers = String.join(",", fromStorageFavorite.keySet());
//                String portTickers = String.join(",", fromStoragePortfolio.keySet());
//
//                counter++;
//                Log.e("AUTO-REFRESESH", "-----------Making API CALL #" + counter);
//                makeApiCallPrice(LocalStorage.FAVOURITES, favTickers);
//                makeApiCallPrice(LocalStorage.PORTFOLIO, portTickers);
            }
        }, 0, AUTO_REFRESH_PERIOD_MSEC);
    }

    private void enableItemDragPortfolio() {

        ItemTouchHelper.Callback callback = new ItemMoveCallback(portFolioSection) {

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;

                if (viewHolder instanceof CompanyHeaderViewHolder ||
                        sectionedAdapter.getSectionForPosition(viewHolder.getAdapterPosition()) instanceof FavoriteSection){

                    dragFlags = 0;
                }

                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {

                if (target instanceof CompanyHeaderViewHolder) {
                    return  false;
                }

                if (target.getItemViewType() != viewHolder.getItemViewType()){
                    return false;
                }

                final CompanyItemViewHolder itemHolder = (CompanyItemViewHolder) viewHolder;

                int fromPosition = sectionedAdapter.getPositionInSection(itemHolder.getAdapterPosition());
                int targetPosition = sectionedAdapter.getPositionInSection(target.getAdapterPosition());

                portFolioSection.onRowMoved(fromPosition, targetPosition);
                sectionedAdapter.getAdapterForSection(portFolioSection).notifyItemMoved(fromPosition, targetPosition);


                Map<String, String> newMap = new LinkedHashMap<>();

                for (Company company: portFolioSection.getData()){
                    newMap.put(company.ticker, company.shares);
                }

                LocalStorage.setMap(LocalStorage.PORTFOLIO, newMap);

                return true;
            }
        };

        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
    }


    private void enableItemDragFavorite() {

        ItemTouchHelper.Callback callback = new ItemMoveCallback(favoriteSection) {

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;


                if (viewHolder instanceof CompanyHeaderViewHolder ||
                        sectionedAdapter.getSectionForPosition(viewHolder.getAdapterPosition()) instanceof PortfolioSection){
                    dragFlags = 0;
                }
//                Log.e("FLAGS FAV", "------" + dragFlags);
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {

//                Log.e("FAV OUTSIDE", "OUTSIDE___________");

                if (target instanceof CompanyHeaderViewHolder) {
                    return  false;
                }

                if (target.getItemViewType() != viewHolder.getItemViewType()){
                    return false;
                }

                final CompanyItemViewHolder itemHolder = (CompanyItemViewHolder) viewHolder;
                int fromPosition = sectionedAdapter.getPositionInSection(itemHolder.getAdapterPosition());
                int targetPosition = sectionedAdapter.getPositionInSection(target.getAdapterPosition());

                favoriteSection.onRowMoved(fromPosition, targetPosition);
                sectionedAdapter.getAdapterForSection(favoriteSection).notifyItemMoved(fromPosition, targetPosition);

                Map<String, String> newMap = new LinkedHashMap<>();

                for (Company company: favoriteSection.getData()){
                    newMap.put(company.ticker, company.name);
                }

                LocalStorage.setMap(LocalStorage.FAVOURITES, newMap);

                return true;
            }
        };

        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
    }


    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {


            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

                int dragFlag = 0;
                int swipeFlag = ItemTouchHelper.LEFT;

                if (viewHolder instanceof CompanyHeaderViewHolder ||
                        sectionedAdapter.getSectionForPosition(viewHolder.getAdapterPosition()) instanceof PortfolioSection){
                    swipeFlag = 0;
                }

                return makeMovementFlags(dragFlag, swipeFlag);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


                final CompanyItemViewHolder itemHolder = (CompanyItemViewHolder) viewHolder;
                final int position = sectionedAdapter.getPositionInSection(itemHolder.getAdapterPosition());


//                Log.e("POSITION", "my position: " + position);
                final Company item = favoriteSection.getData().get(position);
//                Log.e("Company", "my companu: " + item.name);

//                final Company item = favoriteSection.getData().get(sectionedAdapter.getPositionInSection(position));
//                Log.e("SIZE", "my size: " + favoriteSection.getData().size());

                favoriteSection.removeItem(position);
                sectionedAdapter.getAdapterForSection(favoriteSection).notifyItemRemoved(position);
                fromStorageFavorite.remove(item.ticker);
                LocalStorage.setMap(LocalStorage.FAVOURITES, fromStorageFavorite);

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
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

        SearchView.SearchAutoComplete mSearchAutoComplete = searchView.findViewById(R.id.search_src_text);



//        try {
//            Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
//            field.setAccessible(true);
//            field.set(mSearchAutoComplete, R.drawable.my_cursor);
//        } catch (Exception e) {
//            // Ignore exception
//        }

        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);

        mSearchAutoComplete.setAdapter(autoSuggestAdapter);
        mSearchAutoComplete.setDropDownHeight(1300);
//        mSearchAutoComplete.setOnItemClickListener((parent, view, position, id) ->
//                mSearchAutoComplete.setText(autoSuggestAdapter.getObject(position)));

        mSearchAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            //... your stuff

            String query = (String)parent.getItemAtPosition(position);

            mSearchAutoComplete.setText(autoSuggestAdapter.getObject(position));
            isSelectedFromList = true;
//            Log.e("Clicked", "I am here");
        });


        mSearchAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                isSelectedFromList = false;
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
//                Log.i("well", " this worked " + query);
                if (!isSelectedFromList){

                    return false;
                }
                String[] input = query.split("-");
                redirectToDetails(input[0]);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                isSelectedFromList = false;
                return false;
            }
        });

        handler = new Handler( msg -> {
            if (msg.what == TRIGGER_AUTO_COMPLETE) {

                String input = mSearchAutoComplete.getText().toString();

                if (!TextUtils.isEmpty(mSearchAutoComplete.getText()) && input.length() >= 3) {
//                    Log.i("API", "...................API CALL: "+ input );

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
//                    Log.i("length:", "len: " + array.length());
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

//        Log.e("TICKERS", "key: " + key + "tickers: " + tickers);
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
                spinnerContainer.setVisibility(View.GONE);
                homeViewContainer.setVisibility(View.VISIBLE);
//                recyclerView.setVisibility(View.VISIBLE);
//                date.setVisibility(View.VISIBLE);
            }
            return;
        }

        ApiCall.make(this, tickers, PRICE_URL, response -> {
            //parsing logic, please change it as per your requirement
            double stockValue = 0;

//            Log.e("SIZE..", "company list size " + companiesList.size() + "should be: " + tickersArray.length);

            try {
                JSONObject responseObject = new JSONObject(response);
                JSONArray array = responseObject.getJSONArray("results");
//                Log.i("length:", "len: " + array.length());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject row = array.getJSONObject(i);
                    String ticker = row.getString("ticker");
                    String last = (row.getString("last") != "null") ? row.getString("last") : row.getString("tngoLast");
                    String prevClose = row.getString("prevClose");
                    String name = "";
                    String shares = "";
                    String name_or_shares = "";

//                    Log.e("NULL", "this is null " + (row.getString("last")));

                    if (key.equals(LocalStorage.FAVOURITES)){
                        name = fromStorageFavorite.get(ticker);
                        name_or_shares = fromStorageFavorite.get(ticker);

                        if (fromStoragePortfolio.containsKey(ticker)){
                            name_or_shares = fromStoragePortfolio.get(ticker) + " shares";
                        }

                    }else{
                        shares = fromStoragePortfolio.get(ticker);
                        name_or_shares = fromStoragePortfolio.get(ticker);
                        stockValue += Double.parseDouble(last) * Double.parseDouble(shares);
                    }

//                    Log.e("COMPANY", ticker + "--" + last + "--" + prevClose + "--" + name +"--" + shares);

                    DecimalFormat df = new DecimalFormat("####0.00");
                    Company newCompany = new Company(name, ticker, shares, last, prevClose, name_or_shares, ctx);

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
//                Log.e("ERROR_DBUG", "size: " + favouriteList.size() + "company: " + favouriteList.get(0).name);
            }else{
                portfolioList.clear();
                portfolioList.addAll(companiesList);
                DecimalFormat df = new DecimalFormat("####0.00");
                netWorth = df.format(cash + stockValue);
                portFolioSection.setNetWorth(netWorth);
//                Log.e("NETWORTH", netWorth);
                sectionedAdapter.getAdapterForSection(portFolioSection).notifyAllItemsChanged();
            }

            numApiCalls++;

            if (isApiFailed || numApiCalls == TOT_API_CALLS){
                numApiCalls = 0;
                isApiFailed = false;
                spinnerContainer.setVisibility(View.GONE);
                homeViewContainer.setVisibility(View.VISIBLE);
//                recyclerView.setVisibility(View.VISIBLE);
//                date.setVisibility(View.VISIBLE);
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



//    private void handleIntent(Intent intent) {
//
//        Log.i("INTENT", "use entered the search "  + intent.ACTION_SEARCH + " " + intent.getAction());
//
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//            String query = intent.getStringExtra(SearchManager.QUERY);
//            //use the query to search your data somehow
//            Log.i("SEARCH", "use entered the search " + query);
//            Toast toast = Toast.makeText(this, "search done", Toast.LENGTH_LONG);
//            toast.show();
//        }
//    }

//    @Override
//    public void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        setIntent(intent);
//        handleIntent(intent);
//    }

    public void redirectToDetails(String ticker) {

        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(EXTRA_TICKER, ticker);
        startActivity(intent);
        timer.cancel();
    }



}