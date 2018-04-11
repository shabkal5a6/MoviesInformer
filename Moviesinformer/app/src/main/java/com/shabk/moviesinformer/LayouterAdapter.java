package com.shabk.moviesinformer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

/*
*
 * Created by shabk on 30-Jul-16.

*/


public class LayouterAdapter extends RecyclerView.Adapter<LayouterAdapter.MyViewHolder> {//ArrayAdapter<MovieObj> {
    private static final String LOG_TAG = LayouterAdapter.class.getSimpleName();

    private List<MovieObj> moviesList;
    View convertView;

    final private ListItemClickListener mOnClickListener;

    private static int viewHolderCount;





    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView title, year, genre;
        ImageView image;

        public MyViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            title = (TextView) view.findViewById(R.id.movie_name);
            image = (ImageView) view.findViewById(R.id.image_view);

        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getPosition();
            mOnClickListener.onListItemClick(clickedPosition);

        }
    }
    public LayouterAdapter(List<MovieObj> movies,  ListItemClickListener listener){
        moviesList = movies;
        mOnClickListener = listener;
        viewHolderCount = 0;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        MyViewHolder vh = new MyViewHolder(convertView);

        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MovieObj movie = moviesList.get(position);
       // System.out.println(movie.poster);
        new DownloadImageTask(holder.image) .execute(movie.poster);

        holder.title.setText(movie.name);


    }

    @Override
    public int getItemCount() {
        if(moviesList!=null)
            return moviesList.size();
        return 0;
    }


    // this method copied exactly from github for the user: tschellenbach
    //
    //
    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                //e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}

