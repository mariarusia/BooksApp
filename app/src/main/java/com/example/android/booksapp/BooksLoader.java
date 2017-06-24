package com.example.android.booksapp;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Created by maria on 24.06.2017.
 * Creating BooksLoader
 */

public class BooksLoader extends AsyncTaskLoader<List<Books>> {

    /**
     * Loads a list of books by using an AsyncTask to perform the
     * network request to the given URL.
     */
    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = BooksLoader.class.getName();
    /**
     * Query
     */
    private String mQuery;

    /**
     * Constructs a new {@link BooksLoader}.
     *
     * @param context of the activity
     * @param query   to load data from
     */
    public BooksLoader(Context context, String query) {
        super(context);
        mQuery = query;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<Books> loadInBackground() {
        if (mQuery == null) {
            return null;
        }
        // Perform the network request, parse the response, and extract a list of books.
        List<Books> books = QueryUtils.fetchBooksData(mQuery);
        return books;
    }
}


