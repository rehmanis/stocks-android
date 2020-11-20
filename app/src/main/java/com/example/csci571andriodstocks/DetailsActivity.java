package com.example.csci571andriodstocks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.Map;

public class DetailsActivity extends AppCompatActivity {

    private Map<String, String> myFavourites;
    private boolean isFavourites;
    private String ticker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        isFavourites = false;
        Intent intent = getIntent();
        ticker = intent.getStringExtra(MainActivity.EXTRA_TICKER);
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
                myFavourites.put(ticker, "Advanced Micro Devices");
            }
            Log.i("added-removed", "favouroites......" + myFavourites);
            LocalStorage.<String, String>setMap(LocalStorage.FAVOURITES, myFavourites);

        }
        return true;
    }

}