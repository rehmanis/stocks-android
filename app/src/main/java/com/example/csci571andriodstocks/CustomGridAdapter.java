package com.example.csci571andriodstocks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CustomGridAdapter extends BaseAdapter {

    Context context;
    String[] stats;
    LayoutInflater inflater;
    public CustomGridAdapter(Context ctx, String[] stats) {
        this.context = ctx;
        this.stats = stats;
        this.inflater = (LayoutInflater.from(ctx));
    }

    public void setData(String[] stats) {
        this.stats = stats;
    }

    @Override
    public int getCount() {
        return stats.length;
    }
    @Override
    public Object getItem(int i) {
        return stats[i];
    }
    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.stats_item, null); // inflate the layout
        TextView tv = (TextView) view.findViewById(R.id.tvStat_item); // get the reference of ImageView
        tv.setText(stats[i]);
        return view;
    }


}
