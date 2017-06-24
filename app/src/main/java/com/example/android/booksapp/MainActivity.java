package com.example.android.booksapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static com.example.android.booksapp.R.id.search;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Books>> {

    private static final int BOOKS_LOADER_ID = 1;
    //the empty text view
    private TextView textView;
    //adapter for the list of Books
    private BooksAdapter mAdapter;
    //the query which would later become a url
    private String mQuery;
    //the input edit text
    private EditText editText;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        //books array, adapter to show it
        List<Books> books = new ArrayList<Books>();
        mAdapter = new BooksAdapter(this, books);

        // Find a reference to the {@link ListView} in the layout
        ListView booksListView = (ListView) findViewById(R.id.list);
        //set adapter to the listView
        booksListView.setAdapter(mAdapter);

        //we do not need the spinner right at the start, only when we are fetching information
        View loadingIndicator = findViewById(R.id.loading_spinner);
        loadingIndicator.setVisibility(GONE);

        //set empty view if there are no books founf
        textView = (TextView) findViewById(R.id.empty);
        booksListView.setEmptyView(textView);

        //the views needed to process the query
        editText = (EditText) findViewById(R.id.search_field);
        Button button = (Button) findViewById(search);

        if (networkInfo != null && networkInfo.isConnected()) {
            getLoaderManager();
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(GONE);
            // Update empty state with no connection error message
            textView.setText(R.string.no_internet_message);
        }

        if (networkInfo != null && networkInfo.isConnected()) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mQuery = String.valueOf(editText.getText());
                    mAdapter.clear();

                    //Hide the "No books message" during the request
                    textView.setVisibility(GONE);

                    //show the spinner
                    View loadingIndicator = findViewById(R.id.loading_spinner);
                    loadingIndicator.setVisibility(View.VISIBLE);

                    //start fetching
                    getLoaderManager().restartLoader(BOOKS_LOADER_ID, null, MainActivity.this);
                }
            });
        }

        //set on click listener to open the preview page for a book
        booksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current book that was clicked on
                Books currentBook = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri bookUri = Uri.parse(currentBook.getUrl());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, bookUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });
    }

    //overriding the Loader methods
    @Override
    public Loader<List<Books>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        return new BooksLoader(this, mQuery);
    }

    @Override
    public void onLoadFinished(Loader<List<Books>> loader, List<Books> books) {
        // Clear the adapter of previous earthquake data

        textView.setText(R.string.no_books_message);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.loading_spinner);
        progressBar.setVisibility(GONE);
        mAdapter.clear();

        // If there is a valid list of {@link Books}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (books != null && !books.isEmpty()) {
            mAdapter.addAll(books);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Books>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    //Saving the mQuery values to be able to start the activity when the phone rotates
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //Get the query given by the user
        mQuery = String.valueOf(editText.getText());
        outState.putString("query", mQuery);
        super.onSaveInstanceState(outState);
    }

    //restoring the values and starting the search
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mQuery = savedInstanceState.getString("query");
        if (mQuery.length() > 0) {
            getLoaderManager().restartLoader(BOOKS_LOADER_ID, null, MainActivity.this);
            editText.setText(mQuery);
        }
    }
}
