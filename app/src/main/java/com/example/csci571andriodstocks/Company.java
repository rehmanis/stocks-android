package com.example.csci571andriodstocks;

import androidx.annotation.DrawableRes;

public class Company {

    final String name;
    final String ticker;
    final int shares;
    final double price;
    final double change;
    @DrawableRes
    final int arrow;

    public Company(String name, String ticker, int shares, double price, double change) {
        this.name = name;
        this.ticker = ticker;
        this.shares = shares;
        this.price = price;
        this.change = change;

        if (change > 0) {
            this.arrow = R.drawable.ic_twotone_trending_up_24;
        }else  if (change < 0){
            this.arrow = R.drawable.ic_baseline_trending_down_24;
        }else{
            this.arrow = 0;
        }
    }

    public static void addCompany(){

    }

    public static void removeCompany(){

    }


}
