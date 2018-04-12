package com.shabk.moviesinformer;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
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

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by abdullah on 14-Mar-18.
 */

public class MovieDetail extends Activity {

    public static int selected;
    int f_item;
    int item;


    // not from db items
    @BindView(R.id.fav_bt) Button fav;
    @BindView(R.id.trailer) Button Trailer;
    @BindView(R.id.reviews) TextView reviews;
    @BindView(R.id.sv) ScrollView scrollView;

    // form db items
    @BindView(R.id.title) TextView title;
    @BindView(R.id.rating) TextView rating;
    @BindView(R.id.release) TextView release;
    @BindView(R.id.overview) TextView overview;
    @BindView(R.id.image_view) ImageView image;

    MovieObj resultObj;
    final int highest_rate = 1;
    final int favourite = 2;
    private int movie_id;
    private int waiting_time = 1000;
    private boolean pause = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moviedetail_fragment);
        ButterKnife.bind(this);


            final Intent intent = getIntent();
            if (intent != null && intent.hasExtra("item")) {
                item = intent.getIntExtra("item",60);
                selected = intent.getIntExtra("category", 0);
                f_item = intent.getIntExtra("array_size", 20);

            }
            else onresume();






            new DownloadMovieDetails().execute();

            title.setText("Checking..");


            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 1.5s = 1500ms
                    if(checkInFavorite()){
                        fav.setText(getString(R.string.remove_favorite));
                    }
                    else{
                        fav.setText(getString(R.string.add_favorite));
                    }
                }
            }, waiting_time);



            fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String l = getString(R.string.provider_path);
                    Uri movies = Uri.parse(l);
                    Cursor c = managedQuery(movies, null, null, null, "title");
                    c.moveToFirst();
                    boolean add = true;
                    while(c.getPosition()!=c.getCount()) {
                        if(c.getString(c.getColumnIndex(UserFavoritesDBProvider.TITLE)).equals(resultObj.name)){
                            add = false;
                            deleteFromDatabase(resultObj.name);
                            fav.setText(getString(R.string.add_favorite));
                            break;//deleted
                        }


                        c.moveToNext();
                    }

                    if(add==true) {
                        ContentValues values = new ContentValues();
                        values.put(UserFavoritesDBProvider.MOVIE_ID, resultObj.movie_id);
                        values.put(UserFavoritesDBProvider.TITLE, resultObj.name);
                        values.put(UserFavoritesDBProvider.DESCRIBTION, resultObj.describtion);
                        values.put(UserFavoritesDBProvider.IMAGE, resultObj.poster);
                        values.put(UserFavoritesDBProvider.RATE, resultObj.rate);
                        values.put(UserFavoritesDBProvider.DATE, resultObj.date);

                        Uri uri = getContentResolver().insert(UserFavoritesDBProvider.CONTENT_URI, values);
                        Toast.makeText(getBaseContext(),
                                uri.toString(), Toast.LENGTH_LONG).show();

                        fav.setText(getString(R.string.remove_favorite));

                    }

                }


            });

            Trailer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new GetTrailerDetail().execute();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 5s = 5000ms
                            if(TrailerLink!="")
                                watchYoutubeVideo(getBaseContext(),TrailerLink);
                            else{
                                Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.slow_connection),Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    }, 1000);

                }
            });
            reviews.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(),Reviews.class);
                  
                    intent.putExtra("id",resultObj.movie_id);
                    startActivity(intent);
                }
            });
        }

    @Override
    public void onPause()
    {
        super.onPause();
        SharedPreferences example = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor edit = example.edit();
        edit.putInt("item", item);
        edit.putInt("selected",selected);
       // edit.putInt("scroll",itemClicked);
        edit.commit();
        pause = true;
        edit.putBoolean("pause",pause);

    }




    public void onresume() {

        SharedPreferences example = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        pause = example.getBoolean("pause",false);

        if(pause) {
            item = example.getInt("item", 0);
            selected = example.getInt("selected", 0);
        }
        pause = false;

    }


    // delete favorite movie from db
    private void deleteFromDatabase(String position) {
        getContentResolver().delete(UserFavoritesDBProvider.CONTENT_URI,UserFavoritesDBProvider.TITLE + "='"+ position+"'",
                null);
    }


    // used to only to check if the movie already in the favorite DB or not
    private boolean checkInFavorite(){
        String l = getString(R.string.provider_path);;
        Uri movies = Uri.parse(l);
        Cursor c = managedQuery(movies, null, null, null, "title");
        c.moveToFirst();
        boolean add = false;
        while(c.getPosition()!=c.getCount()) {
            if(c.getString(c.getColumnIndex(UserFavoritesDBProvider.TITLE)).equals(resultObj.name)){
                add = true;

                break;
            }
            c.moveToNext();
        }
        return add;
    }





/*  reference
*
*   got from https://stackoverflow.com/questions/574195/android-youtube-app-play-video-intent
*/
    public void watchYoutubeVideo(Context context, String id){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            // it should Verify that the intent will resolve to an activity
            if (appIntent.resolveActivity(getPackageManager()) != null) {
                context.startActivity(appIntent);
            }
        } catch (ActivityNotFoundException ex) {
            if (appIntent.resolveActivity(getPackageManager()) != null) {
                context.startActivity(webIntent);
            }
        }
    }


    private class DownloadMovieDetails extends AsyncTask<String, Void, MovieObj> {


            private String myapi_key = getString(R.string.api_key);

            private MovieObj getMoviesDataFromJson(String MovieJsonStr)
                    throws JSONException {

                final String OWM_LIST = "results";

                final String MOVIE_ID = "id";
                final String TITLE = "title";
                final String DESCRIBTION = "overview";
                final String IMAGE = "poster_path";
                final String RATE = "vote_average";
                final String Date = "release_date";
                final String url_image = getString(R.string.image_url);


                JSONObject MoviesJSON = new JSONObject(MovieJsonStr);
                JSONArray movieArray = MoviesJSON.getJSONArray(OWM_LIST);


                resultObj = null;

                    String title, desctibe, rate, date;

                    String image;

                System.out.println(item+ " ::::::::::::;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;"+selected);
                    JSONObject movieDetail = movieArray.getJSONObject(item);

                    title = movieDetail.getString(TITLE);
                    movie_id = movieDetail.getInt(MOVIE_ID);
                    //if (i == item){
                    image = movieDetail.getString(IMAGE);
                    desctibe = movieDetail.getString(DESCRIBTION);
                    rate = movieDetail.getString(RATE);
                    date = movieDetail.getString(Date);
                    resultObj = new MovieObj(movie_id,url_image + image, title, desctibe, rate, date);





                return resultObj;
            }

            protected MovieObj doInBackground(String... urls) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String MoviesJsonStr = null;
                final String url_highest_rate = getString(R.string.url_highest_rating);
                final String url_most_popular = getString(R.string.popular);

                try {

                    URL url = null;
                    try { //
                        if (selected == highest_rate) {
                            url = new URL(url_highest_rate+myapi_key+"&language=en-US&page=1");
                        }else
                            url = new URL(url_most_popular+myapi_key);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }


                    if(url != null && selected != favourite){
                        urlConnection = (HttpURLConnection) url.openConnection();
                    }

                    //if movie already in favorite db
                    else if( selected == favourite) {
                        String l = getString(R.string.provider_path);;
                        Uri movies = Uri.parse(l);
                        Cursor c = managedQuery(movies, null, null, null, "title");
                        c.moveToPosition(item);
                        resultObj = new MovieObj(c.getInt(c.getColumnIndex(UserFavoritesDBProvider.MOVIE_ID)),c.getString(c.getColumnIndex(UserFavoritesDBProvider.IMAGE)), c.getString(c.getColumnIndex(UserFavoritesDBProvider.TITLE)),
                                        c.getString(c.getColumnIndex(UserFavoritesDBProvider.DESCRIBTION)), c.getString(c.getColumnIndex(UserFavoritesDBProvider.RATE)),
                                        c.getString(c.getColumnIndex(UserFavoritesDBProvider.DATE)));

                        return resultObj;
                    }
                    else
                        return null;


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
                    return getMoviesDataFromJson(MoviesJsonStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }

            protected void onPostExecute(MovieObj result) {
                if (result != null) {
                    title.setText(result.name);
                    rating.setText("Rating: "+result.rate);
                    release.setText("Release Date: "+result.date);
                    overview.setText("Overview: \n"+result.describtion);
                    new LayouterAdapter.DownloadImageTask(image).execute(result.poster);
                }else {
                    Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.no_connection),Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        }



    // get trailer link
    String TrailerLink = "";

    private class GetTrailerDetail extends AsyncTask<String, Void, String> {

        private String myapi_key = getString(R.string.api_key);

        private String getMoviesDataFromJson(String MovieJsonStr)
                throws JSONException {

            final String OWM_LIST = "results";

            final String KEY = "key";

            JSONObject MoviesJSON = new JSONObject(MovieJsonStr);
            JSONArray movieArray = MoviesJSON.getJSONArray(OWM_LIST);
            JSONObject movieDetail = movieArray.getJSONObject(0);

            String Key = movieDetail.getString(KEY);


            return Key;
        }

        protected String doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String MoviesJsonStr = null;


            try {

                URL url = null;
                try {
                    url = new URL("https://api.themoviedb.org/3/movie/"+resultObj.movie_id+"/videos?api_key="+myapi_key+"&language=en-US");
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
                return getMoviesDataFromJson(MoviesJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {

            if (result != null) {
                TrailerLink = result;
            }else {
                Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.no_connection),Toast.LENGTH_SHORT);
                toast.show();
            }

        }
    }

    }

