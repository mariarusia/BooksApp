package com.example.android.booksapp;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.join;

/**
 * Created by maria on 22.06.2017.
 * A class to process the query
 */

final class QueryUtils {

    //private static final String API_BASE = "https://www.googleapis.com/books/v1/volumes?q=?+subject:";
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the USGS dataset and return a list of {@link Books} objects.
     */
    static List<Books> fetchBooksData(String query) {

        //Log.v("query is ", query);
        if (query.trim().length() == 0) {
            //Log.v("query null", query);
            return null;
        } else {
            Log.v("query not null", query);
            String requestUrl = createApiQuery(query);
            // Create URL object
            URL url = createUrl(requestUrl);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = null;

            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            }

            // Extract relevant fields from the JSON response and create a list of {@link books}s

            // Return the list of {@link Earthquake}s
            return extractFeatureFromJson(jsonResponse);
        }
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Handle redirect (response code 301)
            int httpResponse = urlConnection.getResponseCode();
            if (httpResponse == 301) {
                // Get redirect url from "location" header field
                String newUrl = urlConnection.getHeaderField("Location");

                // Open the new connection again
                urlConnection = (HttpURLConnection) new URL(newUrl).openConnection();

                Log.e(LOG_TAG, "Redirect to URL : " + newUrl);
            }

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    //builds a string for a Google Api request
    private static String createApiQuery(String query) {

        String result_uri;

        if (query.trim().length() == 0) return null;
        else {
            String[] words_in_query = query.split("\\s*(=>|,|\\s)\\s*");

            //if author is one, just returns it
            if (words_in_query.length == 1) {
                result_uri = "https://www.googleapis.com/books/v1/volumes?q=?+subject:".concat(words_in_query[0]);
                //makes a single string from a list
            } else {
                result_uri = "https://www.googleapis.com/books/v1/volumes?q=?+subject:".concat(join("_", words_in_query));
            }
            return result_uri;
        }
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /* Return a list of {@link Books} objects that has been built up from
    * parsing the given JSON response.
    */
    private static List<Books> extractFeatureFromJson(String booksJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(booksJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding books to
        List<Books> books = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(booksJSON);

            //not to cause json exception in case nothing is found
            if (baseJsonResponse.has("items")) {

                // Extract the JSONArray associated with the key called "items",
                // which represents a list of all items.
                JSONArray itemsArray = baseJsonResponse.getJSONArray("items");

                // For each item create a books object
                for (int i = 0; i < itemsArray.length(); i++) {

                    // Get a single item at position i within the list of books
                    JSONObject currentBook = itemsArray.getJSONObject(i);

                    //get bookInfo by tag volumeInfo
                    JSONObject bookInfo = currentBook.getJSONObject("volumeInfo");

                    //get title of the books with the name "title"
                    String title = bookInfo.getString("title");

                    String authors;
                    if (bookInfo.has("authors")) {
                        //get array with list of authors "authors":
                        JSONArray authorsArray = bookInfo.getJSONArray("authors");
                        //create 1 String out of lists
                        if (authorsArray.length() == 1) {
                            authors = (String) authorsArray.get(0);
                        } else {
                            authors = authorsArray.join(", ");
                        }
                    } else {
                        authors = "No authors available";
                    }

                    // find the preview url - previewLink
                    String url = bookInfo.getString("previewLink");

                    // Create a new {@link Books object from the JSON response.
                    Books book = new Books(title, authors, url);

                    // Add the new {@link Earthquake} to the list of earthquakes.
                    books.add(book);
                }
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the books JSON results", e);
        }

        // Return the list of books
        return books;
    }

}
