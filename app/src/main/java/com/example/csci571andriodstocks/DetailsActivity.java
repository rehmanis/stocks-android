package com.example.csci571andriodstocks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailsActivity extends AppCompatActivity {

    public static final String PRICE_URL = "https://csci571-trading-platform.wl.r.appspot.com/api/price/";
    public static final String DETAIL_URL = "https://csci571-trading-platform.wl.r.appspot.com/api/detail/";
    public static final String CHART_URL = "https://csci571-trading-platform.wl.r.appspot.com/api/chart/historical/";

    private Map<String, String> myFavourites;
    private boolean isFavourites;
    private String ticker;
    private String name;
    private Context ctx;
    private WebView wv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        isFavourites = false;
        Intent intent = getIntent();
        ticker = intent.getStringExtra(MainActivity.EXTRA_TICKER);
        ctx = this;

        wv = (WebView) findViewById(R.id.webView_chart);
        wv.loadUrl("file:///android_asset/charts.html");
        wv.getSettings().setJavaScriptEnabled(true);


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

                        TextView tvTicker = (TextView) findViewById(R.id.detail_ticker);
                        TextView tvLast = (TextView) findViewById(R.id.detail_last);
                        TextView tvChange = (TextView) findViewById(R.id.detail_change);

                        Locale locale = new Locale("en", "US");
                        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                        Company company = new Company(name, ticker,"", last, prevClose, "", ctx);

                        company.last = fmt.format(Double.parseDouble(company.last));

                        tvTicker.setText(company.ticker);
                        tvChange.setText(fmt.format(company.change));
                        tvChange.setTextColor(company.changeColor);
                        tvLast.setText(company.last);

                    }
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
                        tvCompanyName.setText(name);
                    }
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