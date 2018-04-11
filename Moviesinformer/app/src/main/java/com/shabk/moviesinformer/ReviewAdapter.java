package com.shabk.moviesinformer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by abdullah on 04-Apr-18.
 */

public class ReviewAdapter extends BaseAdapter {
    Context context;
    List<String> reviews;
    List<String> authors;
    LayoutInflater inflter;

    public ReviewAdapter(Context applicationContext, List<String> reviews, List<String> authors) {
        this.context = context;
        this.reviews = reviews;
        this.authors = authors;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return reviews.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.review_item, null);
        TextView review = (TextView) view.findViewById(R.id.__review);
        TextView author = (TextView) view.findViewById(R.id.auth);
        review.setText(reviews.get(i));
        if(authors.isEmpty())
            author.setText("written by "+authors.get(i));
        return view;
    }

}