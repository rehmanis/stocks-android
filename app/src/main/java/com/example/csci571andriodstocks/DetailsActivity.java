package com.example.csci571andriodstocks;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class DetailsActivity extends AppCompatActivity {

    public static final String PRICE_URL = "https://csci571-trading-platform.wl.r.appspot.com/api/price/";
    public static final String DETAIL_URL = "https://csci571-trading-platform.wl.r.appspot.com/api/detail/";
    public static final String CHART_URL = "https://csci571-trading-platform.wl.r.appspot.com/api/chart/historical/";
    public static final String NEWS_URL = "https://csci571-trading-platform.wl.r.appspot.com/api/news/";
    public static final int TOT_API_CALLS = 3;

    private Map<String, String> myFavourites;
    private Map<String, Integer> myPortfolio;
    private boolean isFavourites;
    private String ticker;
    private String name;
    private Context ctx;
    private WebView wv;
    private GridView statGrid;
    private RecyclerView recyclerViewNews;
    private String[] stats;
    private ProgressBar spinner;
    private int numApiCalls;
    private boolean isApiFailed;
    private CustomGridAdapter customGridAdapter;
    private CustomNewsAdapter customNewsAdapter;
    private NestedScrollView nestedScrollView;
    private List<News> newsList;
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
        recyclerViewNews = (RecyclerView) findViewById(R.id.rvNews);
        recyclerViewNews.setLayoutManager(new LinearLayoutManager(this));

        newsList = new ArrayList<>();
        customNewsAdapter = new CustomNewsAdapter(newsList, this,
                (CustomNewsAdapter.OnItemClickListener) (newsItem, position) -> {
//                    Uri uri = Uri.parse(newsItem.url); // missing 'http://' will cause crashed
//                    Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
//                    startActivity(intent1);
                    openChrome(newsItem.url);
                    Log.e("CLICK_NEWS", "news item clicked at " + position);
                },

                (CustomNewsAdapter.OnItemLongClickListener) (newsItem, position) -> {

                    final Dialog dialog = new Dialog(ctx);
                    dialog.setContentView(R.layout.news_dialog);

                    TextView tvNewsTitle = (TextView) dialog.findViewById(R.id.tvDialog_news_title);
                    ImageView ivNewsImg = (ImageView) dialog.findViewById(R.id.ivDialog_news_img);
                    ImageButton btnTwitter = (ImageButton) dialog.findViewById(R.id.btn_twitter);
                    ImageButton btnChrome = (ImageButton) dialog.findViewById(R.id.btn_chrome);

                    tvNewsTitle.setText(newsItem.title);
                    Picasso.with(ctx).load(newsItem.img)
                            .into(ivNewsImg);

                    btnChrome.setOnClickListener(v -> openChrome(newsItem.url));
                    btnTwitter.setOnClickListener(v -> {
                        String url = "https://twitter.com/intent/tweet?text=Check out this link:" +
                                "&url=" + newsItem.url + "&hashtags=CSCI571StockApp";
                        openChrome(url);
                    });

                    dialog.show();
                    Log.e("LONGCLICK_NEWS", "news item clicked at " + position);
                }
        );
        recyclerViewNews.setAdapter(customNewsAdapter);


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
        makeApiCallNews(ticker);


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

    private void openChrome(String url){
        Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);

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

    private void makeApiCallNews(String ticker) {

        ApiCall.make(this, ticker, NEWS_URL, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(String response) {
                //parsing logic, please change it as per your requirement
                List<News> tempNewsList = new ArrayList<>();

                try {
                    JSONObject responseObject = new JSONObject(response);
                    JSONArray array = responseObject.getJSONArray("results");
                    Log.i("length:", "len: " + array.length());
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject row = array.getJSONObject(i);
                        String url = row.getString("url");
                        String title = row.getString("title");
                        String src = row.getString("source");
                        String urlToImg = row.getString("urlToImage");
                        String timestamp = row.getString("publishedAt");

                        if (i == 10){
                            Log.i("NEWS", "urlToImg: " + urlToImg + "\ntitle: " + title);

                        }


                        News newsItem = new News(src, urlToImg, title, timestamp, url);
                        tempNewsList.add(newsItem);

                    }
                } catch (Exception e) {
                    isApiFailed = true;
                    e.printStackTrace();
                }

                newsList.clear();
                newsList.addAll(tempNewsList);
                customGridAdapter.notifyDataSetChanged();

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