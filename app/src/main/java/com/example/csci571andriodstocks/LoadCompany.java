//package com.example.csci571andriodstocks;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//import android.util.Log;
//import android.view.View;
//
//import androidx.annotation.RequiresApi;
//
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
//
//public class LoadCompany {
//
//    private Map<String, String> fromStorage;
//    public static final String PRICE_URL = "https://csci571-trading-platform.wl.r.appspot.com/api/price/";
//    private Context ctx;
//    private List<Map<String, List<Integer>>> compPrices;
//    private List<Company> list;
//    private SectionedRecyclerViewAdapter sectionedAdapter;
//
//
//    public LoadCompany (Context ctx, SectionedRecyclerViewAdapter sectionedAdapter){
//        this.ctx = ctx;
//        compPrices = new ArrayList<>();
//        this.sectionedAdapter = sectionedAdapter;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public List<Company> getCompanies(String key) {
//        list = new ArrayList<>();
//        fromStorage = LocalStorage.getFromStorage(key);
//        String tickers = String.join(",", fromStorage.values());
//        makeApiCallPrice(tickers, key);
//        return list;
//    }
//
//
//    private void makeApiCallPrice(String tickers, String key) {
//
//        ApiCall.make(ctx, tickers, PRICE_URL, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                //parsing logic, please change it as per your requirement
////                List<Map<String, List<Integer>>> companyPrices = new ArrayList<>();
//                List<Company> companiesList = new ArrayList<>();
//
//                try {
//                    JSONObject responseObject = new JSONObject(response);
//                    JSONArray array = responseObject.getJSONArray("results");
//                    Log.i("length:", "len: " + array.length());
//                    for (int i = 0; i < array.length(); i++) {
//                        JSONObject row = array.getJSONObject(i);
//                        String ticker = row.getString("ticker");
//                        String last = row.getString("last");
//                        String prevClose = row.getString("last");
//                        String name = "";
//
//                        if (key.equals(LocalStorage.FAVOURITES)){
//                            name = fromStorage.get(ticker);
//                        }
//
//                        companiesList.add(new Company(name, ticker,"", last, prevClose, "", ctx));
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                list.clear();
//                list.addAll(companiesList);
//                sectionedAdapter.notifyDataSetChanged();
//            }
//        }, error -> Log.i("error", "error in search http " + error));
//    }
//
//}
