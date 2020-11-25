package com.example.csci571andriodstocks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class DetailsActivity extends AppCompatActivity {

    public static final String PRICE_URL = "https://csci571-trading-platform.wl.r.appspot.com/api/price/";
    public static final String DETAIL_URL = "https://csci571-trading-platform.wl.r.appspot.com/api/detail/";
    public static final String CHART_URL = "https://csci571-trading-platform.wl.r.appspot.com/api/chart/historical/";
    public static final int TOT_API_CALLS = 2;

    private Map<String, String> myFavourites;
    private Map<String, Integer> myPortfolio;
    private boolean isFavourites;
    private String ticker;
    private String name;
    private Context ctx;
    private WebView wv;
    private GridView statGrid;
    private String[] stats;
    private ProgressBar spinner;
    private int numApiCalls;
    private boolean isApiFailed;
    CustomGridAdapter customGridAdapter;
    private NestedScrollView nestedScrollView;
//    private String descBtnTxt = "Show more";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        isFavourites = false;
        Intent intent = getIntent();
        ticker = intent.getStringExtra(MainActivity.EXTRA_TICKER);
        ctx = this;
        numApiCalls = 0;
        isApiFailed = false;
        wv = (WebView) findViewById(R.id.webView_chart);
        wv.loadUrl("file:///android_asset/charts.html");
        wv.getSettings().setJavaScriptEnabled(true);

        statGrid = (GridView) findViewById(R.id.grid_view); // init GridView
        spinner = (ProgressBar)findViewById(R.id.progressbar);
        stats = new String[7];
        // Create an object of CustomAdapter and set Adapter to GirdView
        customGridAdapter = new CustomGridAdapter(this, stats);
        statGrid.setAdapter(customGridAdapter);

        nestedScrollView = (NestedScrollView) findViewById(R.id.details_screen);

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.equals("file:///android_asset/charts.html")) {
                    Log.i("CHART", "chart................................");
                    makeApiCallChart(ticker);
                }
            }
        });


        makeApiCallPrice(ticker);
        makeApiCallSummary(ticker);

//        myPortfolio = LocalStorage.getFromStorage(LocalStorage.PORTFOLIO);
//        tvShares = findViewById(R.id.tvDetails_shares);
//        TextView tvMarketValue = findViewById(R.id.tvMarket_value);
//        tvShares.setText("You have 0 shares of " + ticker);
//
//        if (myPortfolio.containsKey(ticker)){
//
//            int shares = myPortfolio.get(ticker);
//
//            tvShares.setText("Shares owned: " + shares);
//            tvMarketValue.setText("Market Value: "  );
//        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.details_menu, menu);
        myFavourites = LocalStorage.getFromStorage(LocalStorage.FAVOURITES);
        Log.i("fav", "favouroites" + myFavourites);


        MenuItem item = menu.findItem(R.id.toggle_star);
        Log.i("oncreatemeu", "...............item: " + ticker);

        if (myFavourites.containsKey(ticker)){
            item.setIcon(R.drawable.ic_baseline_star_24);
            isFavourites = true;
        } else{
            item.setIcon(R.drawable.ic_baseline_star_border_24);
            isFavourites = false;
        }

        return true;
    }

     // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.toggle_star) {

            myFavourites = LocalStorage.getFromStorage(LocalStorage.FAVOURITES);

            // do something here
            if (isFavourites){
                isFavourites = false;
                item.setIcon(R.drawable.ic_baseline_star_border_24);
                myFavourites.remove(ticker);

            } else{
                isFavourites = true;
                item.setIcon(R.drawable.ic_baseline_star_24);
                myFavourites.put(ticker, name);
            }
            Log.i("added-removed", "favouroites......" + myFavourites);
            LocalStorage.<String, String>setMap(LocalStorage.FAVOURITES, myFavourites);

        }
        return true;
    }


    private void makeApiCallPrice(String ticker) {

        ApiCall.make(this, ticker, PRICE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject responseObject = new JSONObject(response);
                    JSONArray array = responseObject.getJSONArray("results");
                    Log.i("length:", "len: " + array.length());
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject row = array.getJSONObject(i);
                        String last = row.getString("last");
                        String prevClose = row.getString("prevClose");
                        String low = row.getString("low");
                        String bidPrice = row.getString("bidPrice");
                        String openPrice = row.getString("open");
                        String mid = row.getString("mid");
                        String high = row.getString("high");
                        String volume = row.getString("volume");

                        TextView tvTicker = (TextView) findViewById(R.id.detail_ticker);
                        TextView tvLast = (TextView) findViewById(R.id.detail_last);
                        TextView tvChange = (TextView) findViewById(R.id.detail_change);
                        TextView tvMarketValue = findViewById(R.id.tvMarket_value);
                        TextView tvShares = findViewById(R.id.tvDetails_shares);

                        Locale locale = new Locale("en", "US");
                        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                        Company company = new Company(name, ticker,"", last, prevClose, "", ctx);

                        company.last = fmt.format(Double.parseDouble(company.last));

                        tvTicker.setText(company.ticker);
                        tvChange.setText(fmt.format(company.change));
                        tvChange.setTextColor(company.changeColor);
                        tvLast.setText(company.last);
                        tvShares.setText("You have 0 shares of " + ticker);

                        myPortfolio = LocalStorage.getFromStorage(LocalStorage.PORTFOLIO);

                        if (myPortfolio.containsKey(ticker)){

                            int shares = myPortfolio.get(ticker);
                            double val = shares * Double.parseDouble(last);
                            tvShares.setText("Shares owned: " + shares);
                            tvMarketValue.setText("Market Value: " + fmt.format(val));
                        }


                        String[] newSats = {
                                "Current Price: " + last,
                                "Low: " + low,
                                "Bid Price: " + bidPrice,
                                "Open Price: " + openPrice,
                                "Mid: " + mid,
                                "High: " + high,
                                "Volume: " + volume
                        };

                        customGridAdapter.setData(newSats);
                        customGridAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    isApiFailed = true;
                }

                numApiCalls++;

                if (isApiFailed){
                    numApiCalls = 0;
                    // display an error message or do some error handling

                } else if (numApiCalls == TOT_API_CALLS){
                    numApiCalls = 0;
                    spinner.setVisibility(View.GONE);
                    nestedScrollView.setVisibility(View.VISIBLE);
                    isEllipses();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isApiFailed = true;
                Log.i("error", "error in search http " + error);
            }
        });
    }

    private void makeApiCallSummary(String ticker) {

        ApiCall.make(this, ticker, DETAIL_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject responseObject = new JSONObject(response);
                    JSONArray array = responseObject.getJSONArray("results");
                    Log.i("length:", "len: " + array.length());
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject row = array.getJSONObject(i);
                        String description = row.getString("description");
                        name = row.getString("name");;

                        TextView tvCompanyName = (TextView) findViewById(R.id.detail_company_name);
                        TextView tvDescription = (TextView) findViewById(R.id.tvDetails_desc);
                        tvCompanyName.setText(name);
                        tvDescription.setText(description);




                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                numApiCalls++;

                if (isApiFailed){
                    numApiCalls = 0;
                    // display an error message or do some error handling

                } else if (numApiCalls == TOT_API_CALLS){
                    numApiCalls = 0;
                    spinner.setVisibility(View.GONE);
                    nestedScrollView.setVisibility(View.VISIBLE);
                    isEllipses();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isApiFailed = true;
                Log.i("error", "error in search http " + error);
            }
        });
    }

    public void toggleDescription(View view){

        TextView tvDescription = (TextView) findViewById(R.id.tvDetails_desc);
        Button toggleDescBtn = (Button) findViewById(R.id.btn_desc);

        CharSequence btnDescTxt = toggleDescBtn.getText();

        Log.d("button", ".........button pressed text: " + btnDescTxt);

        if (btnDescTxt.equals("Show more...")){
            toggleDescBtn.setText("Show less");
            tvDescription.setMaxLines(Integer.MAX_VALUE);
            tvDescription.setEllipsize(null);

        }else{
            toggleDescBtn.setText("Show more...");
            tvDescription.setMaxLines(2);
            tvDescription.setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    public void isEllipses(){

        TextView tvDescription = (TextView) findViewById(R.id.tvDetails_desc);
        Button toggleDescBtn = (Button) findViewById(R.id.btn_desc);

        tvDescription.post(new Runnable() {
            @Override
            public void run() {
                int lines = tvDescription.getLineCount();
                if (lines > 2) {
                    //do something
                    tvDescription.setMaxLines(2);
                    tvDescription.setEllipsize(TextUtils.TruncateAt.END);
                    toggleDescBtn.setVisibility(View.VISIBLE);
                    Log.d("LINES", "..........There are " + lines);
                }
            }
        });

    }


    private void makeApiCallChart(String ticker) {

        ApiCall.make(this, ticker, CHART_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject responseObject = new JSONObject(response);
                    JSONArray array = responseObject.getJSONArray("results");
                    Log.i("length:", "len: " + array.length());
                    wv.loadUrl("javascript:createChart('"+ticker+"', '"+array+"');");

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("error", "error in search http " + error);
            }
        });
    }

}