package com.shabk.moviesinformer;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

/**
 * Created by abdullah on 11-Mar-18.
 */

class MovieObj implements Parcelable{
    String poster;
    String name, describtion, rate, date;
    int movie_id;
    public MovieObj(String movie_poster, String movie_name){
        poster = movie_poster;
        name = movie_name;
    }

    public MovieObj(int id, String movie_poster, String movie_name, String describe, String rating, String release){
        movie_id = id;
        poster = movie_poster;
        name = movie_name;
        describtion = describe;
        rate = rating;
        date = release;
    }

    public static final Creator<MovieObj> CREATOR = new Creator<MovieObj>() {
        @Override
        public MovieObj createFromParcel(Parcel in) {
            return new MovieObj(in);
        }

        @Override
        public MovieObj[] newArray(int size) {
            return new MovieObj[size];
        }
    };


    public String getName(){
        return name;
    }

    public MovieObj(Parcel pl){

        poster = pl.readString();
        name = pl.readString();
        describtion = pl.readString();
        rate = pl.readString();
        date = pl.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(poster);
        dest.writeString(name);
        dest.writeString(describtion);
        dest.writeString(rate);
        dest.writeString(date);
    }
}
