package com.example.android.booksapp;

/**
 * Created by maria on 24.06.2017.
 * A class for a book, which would contain author, name and url
 */

public class Books {
    private String mAuthors;
    private String mTitle;
    private String mUrl;

    // constructor
    public Books(String title, String authors, String url){
        mTitle = title;
        mAuthors = authors;
        mUrl = url;
    }

    // return String with authors
    public String getAuthors() {
        return mAuthors;
    }

    //return String with URL
    public String getTitle() {
        return mTitle;
    }

    public String getUrl(){
        return mUrl;
    }
}
