package com.example.android.booksapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by maria on 24.06.2017.
 * BooksAdapter
 */

public class BooksAdapter extends ArrayAdapter<Books> {

    /**
     * Constructs a new {@link BooksAdapter}.
     *
     * @param context     of the app
     * @param books is the list of books, which is the data source of the adapter
     */
    public BooksAdapter(Context context, List<Books> books) {
        super(context, 0, books);
    }

    /**
     * Returns a list item view that displays information about the book
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        // Find the Book at the given position in the list of books
        Books currentBook = getItem(position);

        // Find the TextView with view ID title
        TextView titleView = (TextView) listItemView.findViewById(R.id.title);
        // Display title
        titleView.setText(currentBook.getTitle());

        TextView authorsView = (TextView) listItemView.findViewById(R.id.authors);
        // Display the authors
        authorsView.setText(currentBook.getAuthors());

        // Return the list item view that is now showing the appropriate data
        return listItemView;
    }
}
