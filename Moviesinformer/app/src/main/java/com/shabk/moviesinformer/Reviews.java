package com.shabk.moviesinformer;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abdullah on 04-Apr-18.
 */

public class Reviews extends AppCompatActivity {
    LinearLayout ll;
    List<String> reviews;
    List<String> authors;
    private int movie_;
    ReviewAdapter RA;
    private int position;
    private int waitingTime;
    ListView listView;
    Parcelable state;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_);
        reviews = new ArrayList<>();
        authors = new ArrayList<>();
        listView = (ListView)findViewById(R.id.lv);

        if(savedInstanceState!=null){
            reviews = savedInstanceState.getStringArrayList("reviews");
            authors = savedInstanceState.getStringArrayList("authors");

            state = savedInstanceState.getParcelable("state");
            waitingTime = 0;
        }else{
            new GetReviews().execute();
            position = 0;
            waitingTime = 1000;
        }


        final Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            movie_ = intent.getIntExtra("id",60);
        }



        // used to delay the app while it get the data from server
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(reviews.size()!=0){
                for(int i = 0;i<reviews.size();i++){
                    RA = new ReviewAdapter(getApplicationContext(), reviews, authors);
                }
                }
                else {
                    reviews.add("No Reviews Available");
                    authors.add("");
                    RA = new ReviewAdapter(getApplicationContext(), reviews, authors);
                }


                listView.setAdapter(RA);
                if(state!=null) {
                    listView.onRestoreInstanceState(state);

                }

            }
        }, waitingTime);




    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList("reviews", (ArrayList<String>) reviews);
        outState.putStringArrayList("authors", (ArrayList<String>) authors);
        outState.putParcelable("state",listView.onSaveInstanceState());


    }










    private class GetReviews extends AsyncTask<String, Void, MovieObj> {


        private String myapi_key = getString(R.string.api_key);

        private MovieObj getMoviesDataFromJson(String MovieJsonStr)
                throws JSONException {

            final String OWM_LIST = "results";

            final String AUTHOR = "author";
            final String REVIEW = "content";
            JSONObject MoviesJSON = new JSONObject(MovieJsonStr);
            JSONArray movieArray = MoviesJSON.getJSONArray(OWM_LIST);

            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movieDetail = movieArray.getJSONObject(i);
                reviews.add(movieDetail.getString(REVIEW));
                authors.add(movieDetail.getString(AUTHOR));

            }


            return null;
        }

        protected MovieObj doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String MoviesJsonStr = null;
            final String url_revies = getString(R.string.url_reviews);
            final String url_reviews_rest = getString(R.string.url_reviews_rest);

            try {

                URL url = null;
                try { //

                        url = new URL(url_revies+movie_+url_reviews_rest+myapi_key+"&language=en-US&page=1");


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                    urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    reader = new BufferedReader(new InputStreamReader(in));
                } catch (IOException e) {

                }

                String line;
                StringBuffer buffer = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }


                MoviesJsonStr = buffer.toString();
            } catch (IOException e) {
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                    }
                }
            }
            try {
                getMoviesDataFromJson(MoviesJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        protected void onPostExecute(MovieObj result) {
        // not required

        }
    }
}
