package com.example.csci571andriodstocks;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CustomNewsAdapter extends RecyclerView.Adapter<CustomNewsAdapter.ViewHolder> {

//    private String[] localDataSet;
    private final List<News> newsList;
    private final Context ctx;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNewsSrc;
        private final ImageView ivNewsImg;
        private final TextView tvLastUpdated;
        private final TextView tvNewsTitle;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            tvNewsSrc = (TextView) view.findViewById(R.id.tv_news_src);
            ivNewsImg = (ImageView) view.findViewById(R.id.iv_news_img);
            tvNewsTitle = (TextView) view.findViewById(R.id.tv_news_title);
            tvLastUpdated = (TextView) view.findViewById(R.id.tv_news_last_updated);
        }

        public TextView getTvNewsSrc() {
            return tvNewsSrc;
        }

        public TextView getTvLastUpdated() {
            return tvLastUpdated;
        }

        public TextView getTvNewsTitle() {
            return tvNewsTitle;
        }
        public ImageView getIvNewsImg() {
            return ivNewsImg;
        }
    }

    /**
     * Initialize the newsList of the Adapter.
     *
     * @param newsList List<News> containing the data to populate views to be used
     * by RecyclerView.
     */
    public CustomNewsAdapter(List<News> newsList, Context ctx) {

        this.newsList = newsList;
        this.ctx = ctx;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.news_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        final News news = newsList.get(position);

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTvLastUpdated().setText(news.lastUpdated);
        viewHolder.getTvNewsTitle().setText(news.title);
        viewHolder.getTvNewsSrc().setText(news.src);

        ImageView ivNewsImg = (ImageView)  viewHolder.getIvNewsImg();
//        Picasso.with(ctx).load(news.img).into(ivNewsImg);
//        Picasso.with(ctx).load(news.img).resize(0, 150).into(ivNewsImg);
        Picasso.with(ctx).load(news.img).resize(500, 500).into(ivNewsImg);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return newsList.size();
    }

}
