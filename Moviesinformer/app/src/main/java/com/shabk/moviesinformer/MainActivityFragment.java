package com.shabk.moviesinformer;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


import static android.content.Context.MODE_PRIVATE;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LayouterAdapter.ListItemClickListener,LoaderManager.LoaderCallbacks<MovieObj[]> {

    List<MovieObj> MoviesList;
    public LayouterAdapter mViewAdapter;
    private boolean valid = false;
    public  int select ;
    //public RecyclerView recyclerView;
    @BindView(R.id.r_view) RecyclerView recyclerView;
    final int highest_rate = 1;
    final int popular = 0;
   // public LinearLayout mainLayout, itemLayout;
    @BindView(R.id.main_linear_layout) LinearLayout mainLayout;
    @BindView(R.id.spinner) Spinner sort;
    int s ;
    private int favourite = 2;
    private SharedPreferences pr;
    private boolean pause = false;
    List<String> categories;
    String[] so = {"popular","hieghst","fav"};
    final String sort_by_popular = "Most popular";
    final String sort_by_highest_rate = "Highest-Rated";
    final String sort_by_favorite = "Favorites";
     int scrollstate;
    GridLayoutManager mLayoutManager;
    private int itemClicked;
    private boolean rotate = false;

    public MainActivityFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);
        rotate = false;
        getActivity().getSupportLoaderManager().initLoader(OPERATION_SEARCH_LOADER, null, this);


        // here the user preferences will be applied to the main Fragment
        getSharedPreferences();
        if(color.equalsIgnoreCase("red")){
            mainLayout.setBackgroundColor(getResources().getColor(R.color.darkRed));
        }
        else {
            mainLayout.setBackgroundColor(getResources().getColor(R.color.Blue));
        }




        if (savedInstanceState == null) {
            MoviesList = new ArrayList();
        }
        else{
            rotate = true;
            MoviesList = savedInstanceState.getParcelableArrayList("my_list");
            // used if the user rotates the phone
            scrollstate = savedInstanceState.getInt("scroll");
            select = savedInstanceState.getInt("select");



        }


        // set up adapter with movies list
        mViewAdapter = new LayouterAdapter(MoviesList,this);


        mLayoutManager = new GridLayoutManager(getContext(),numberOfColumns());

        mLayoutManager.scrollToPosition(scrollstate);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());










        categories = new ArrayList<String>();
        addItemsToCategory();   // 3 orders to sort movies list

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this.getActivity(),android.R.layout.simple_spinner_dropdown_item,categories);

        // add spinner , which used to sort the movies list
        sort.setAdapter(dataAdapter);

        // set the old selection, if there is one
        sort.setSelection(select,false);

        // get Movies data
        makeOperationSearchQuery();

        sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item;


                if(pause == true){
                    pause = false;
                }

                item = parent.getSelectedItem().toString();

                if (item.equalsIgnoreCase(sort_by_highest_rate))
                    select = highest_rate;
                else if (item.equalsIgnoreCase(sort_by_favorite))
                    select = favourite; // 2 for favorite
                else if(item.equalsIgnoreCase(sort_by_popular))
                    select = popular;

                makeOperationSearchQuery();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });






        return rootView;

    }


        // add sort list
    private void addItemsToCategory(){

        categories.add(sort_by_popular);
        categories.add(sort_by_highest_rate);
        categories.add(sort_by_favorite);

    }
    @Override
    public void onPause()
    {
        super.onPause();
        SharedPreferences example = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor edit = example.edit();
        edit.putInt("select", select);
        edit.putBoolean("pause",true);
        edit.putInt("scroll",itemClicked);
        edit.commit();
        rotate = false;

    }



    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences example = PreferenceManager.getDefaultSharedPreferences(getContext());

        pause = example.getBoolean("pause", false);
        select = example.getInt("select", 0);
        if(rotate) // the app run onPause method while rotating the phone, so this will specifiy if there is rotation or MovieDetail activity runs
            scrollstate = example.getInt("scroll_rotate",0);
        else
            scrollstate = example.getInt("scroll",0);
    }



    // app theme color
    String color ="red";
    // get the user choice of the theme color
    private void getSharedPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        color = prefs.getString("listpref", "red");

    }


    //reference:::: udacity reviewer
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SharedPreferences example = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor edit = example.edit();
        edit.putInt("scroll_rotate",mLayoutManager.findFirstVisibleItemPosition());
        edit.commit();
        outState.putInt("select",select);
        outState.putInt("scroll",mLayoutManager.findFirstVisibleItemPosition());
        outState.putParcelableArrayList("my_list", (ArrayList<? extends Parcelable>) MoviesList);


    }



    // as commented by a udacityan reviewer
    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // You can change this divider to adjust the size of the poster
        int widthDivider = 350;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
    }


    // if user clicks on a movie item, MovieDetail activity shall start
    @Override
    public void onListItemClick(int clickedItemIndex) {
        itemClicked = clickedItemIndex;
        Intent i = new Intent(this.getContext(), MovieDetail.class);
        i.putExtra("item", clickedItemIndex);
        i.putExtra("category",select);
        i.putExtra("array_size",20);
        startActivity(i);

    }



    /// this will replace AsyncTask
    // wich used to connect to the server and receives data
    //
    //
    //
    //ref. https://medium.com/@sanjeevy133/an-idiots-guide-to-android-asynctaskloader-76f8bfb0a0c0

    public static final int OPERATION_SEARCH_LOADER = 22;
    public static final String OPERATION_URL_EXTRA = "url_that_return_json_data";
    //
    private void makeOperationSearchQuery() {

        // Create a bundle called queryBundle
        Bundle queryBundle = new Bundle();
        // Use putString with OPERATION_QUERY_URL_EXTRA as the key and the String value of the URL as the value
        //url value here is https://jsonplaceholder.typicode.com/posts
       // queryBundle.putString(OPERATION_QUERY_URL_EXTRA,url);
        // Call getSupportLoaderManager and store it in a LoaderManager variable
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        // Get our Loader by calling getLoader and passing the ID we specified
        Loader<String> loader = loaderManager.getLoader(OPERATION_SEARCH_LOADER);
        // If the Loader was null, initialize it. Else, restart it.
        if(loader==null){
            loaderManager.initLoader(OPERATION_SEARCH_LOADER, queryBundle, this);
        }else{
            loaderManager.restartLoader(OPERATION_SEARCH_LOADER, queryBundle, this);
        }
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<MovieObj[]> onCreateLoader(int id, Bundle args) {
        final String LOG_TAG = com.shabk.moviesinformer.MainActivityFragment.class.getSimpleName();



        final int i_item = 0;
        final int f_item = 20;

        return new AsyncTaskLoader<MovieObj[]>(getContext()) {
            @Override
            public MovieObj[] loadInBackground() {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Will contain the raw JSON response as a string.
                String MoviesJsonStr = null;
                final String apiKey =  getString(R.string.api_key);
                final String url_highest_rate = getString(R.string.url_highest_rating);
                final String url_most_popular =  getString(R.string.popular);



                try {


                    URL url = null;

                    try {

                        if (select == highest_rate) {
                            url = new URL(url_highest_rate + apiKey + "&language=en-US&page=1");
                        }  else
                            url = new URL(url_most_popular + apiKey);


                    } catch (MalformedURLException e) {
                        e.printStackTrace();

                    }

                    // Create the request to OpenWeatherMap, and open the connection
                    if(url != null && select != favourite){
                        urlConnection = (HttpURLConnection) url.openConnection();
                    }
                    else if( select == favourite) {
                        String l = getString(R.string.provider_path);

                        Uri movies = Uri.parse(l);
                        Cursor c = getActivity().managedQuery(movies, null, null, null, "title");

                        MovieObj[] mObj = new MovieObj[c.getCount()];
                        if (c.moveToFirst()) {
                            do {
                                mObj[c.getPosition()] = new MovieObj(c.getInt(c.getColumnIndex(UserFavoritesDBProvider.MOVIE_ID)),c.getString(c.getColumnIndex(UserFavoritesDBProvider.IMAGE)),
                                        c.getString(c.getColumnIndex(UserFavoritesDBProvider.TITLE)),
                                        c.getString(c.getColumnIndex(UserFavoritesDBProvider.DESCRIBTION)),
                                        c.getString(c.getColumnIndex(UserFavoritesDBProvider.RATE)),
                                        c.getString(c.getColumnIndex(UserFavoritesDBProvider.DATE)));
                            } while (c.moveToNext());



                        }

                        return mObj;
                    }
                    else
                        return null;

                    try {

                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                        reader = new BufferedReader(new InputStreamReader(in));
                    } catch (IOException e) {


                        return null;
                    }

                    String line;
                    StringBuffer buffer = new StringBuffer();
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }


                    MoviesJsonStr = buffer.toString();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);

                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
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
            @Override
            protected void onStartLoading() {
                //Think of this as AsyncTask onPreExecute() method,you can start your progress bar,and at the end call forceLoad();
                forceLoad();
            }


            private MovieObj[] getMoviesDataFromJson(String MovieJsonStr)
                    throws JSONException {


                final String OWM_LIST = "results";


                final String TITLE = "title";
                final String IMAGE = "poster_path";
                String image_url = null;
                if(isAdded())
                    image_url = getString(R.string.image_url);


                JSONObject MoviesJSON = new JSONObject(MovieJsonStr);
                JSONArray movieArray = MoviesJSON.getJSONArray(OWM_LIST);

                MovieObj[] resultObj = new MovieObj[f_item];
                for (int i = i_item; i < f_item; i++) {

                    String title;

                    String image;
                    JSONObject movieDetail = movieArray.getJSONObject(i);


                    title = movieDetail.getString(TITLE);

                    image = movieDetail.getString(IMAGE);


                    resultObj[i] = new MovieObj(image_url + image, title);

                }

                return resultObj;


                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<MovieObj[]> loader, MovieObj[] data) {
                if (data != null) {
                    MoviesList.clear();
                    for (MovieObj movie : data) {
                        MoviesList.add(movie);
                    }
                    //mLayoutManager.scrollToPosition(scrollstate);
                    recyclerView.setAdapter(mViewAdapter);
                    mLayoutManager.scrollToPosition(scrollstate);
                    //scrollstate = 0;
                }
                else {
                    Toast toast = Toast.makeText(getContext(),getString(R.string.no_connection),Toast.LENGTH_SHORT);
                    toast.show();
                }

                scrollstate = 0;
            }

            @Override
            public void onLoaderReset(Loader<MovieObj[]> loader) {

            }


    }












