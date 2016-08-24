package com.game.hardik.virtualstockgame;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SearchFragment extends Fragment {

    public SearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateStock();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_search_results, container, false);
        return rootView;
    }

    private void updateStock() {
        FetchStocks fetchStock = new FetchStocks();
        Intent intent = getActivity().getIntent();
        String symbol = "";
        if(intent.hasExtra(Intent.EXTRA_TEXT)) {
            symbol = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
        fetchStock.execute(symbol);
    }


    public class FetchStocks extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String stockStr = null;
            try {
                final String STOCK_BASE_URL = "http://marketdata.websol.barchart.com/getQuote.json?";
                final String KEY_PARAM = "key";
                final String SYMBOL_PARAM = "symbols";
                Uri builtUri = Uri.parse(STOCK_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_PARAM, BuildConfig.STOCK_API_KEY)
                        .appendQueryParameter(SYMBOL_PARAM, strings[0])
                        .build();
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                stockStr = buffer.toString();
            }
            catch (IOException e){
                Log.e(SearchFragment.class.getSimpleName(), "Error ", e);
                return null;
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Search Results: ", "Error closing stream", e);
                    }
                }
            }
            return stockStr;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    final String VSG_RESULTS = "results";
                    final String VSG_NAME = "name";
                    final String VSG_OPEN = "open";
                    final String VSG_HIGH = "high";
                    final String VSG_LOW = "low";
                    final String VSG_VOLUME = "volume";
                    final String VSG_EXCHANGE = "exchange";
                    final String VSG_PER_CHANGE = "percentChange";
                    final String VSG_CLOSE = "close";


                    TextView stock_name = (TextView) getView().findViewById(R.id.stockName);
                    TextView close_price = (TextView) getView().findViewById(R.id.close);
                    TextView open_price = (TextView) getView().findViewById(R.id.open);
                    TextView total_volume = (TextView) getView().findViewById(R.id.volume);
                    TextView price_high = (TextView) getView().findViewById(R.id.high);
                    TextView exchange_name = (TextView) getView().findViewById(R.id.exchange);
                    TextView price_low = (TextView) getView().findViewById(R.id.low);
                    TextView price_percent_change = (TextView) getView().findViewById(R.id.percent_change);

                    JSONObject forecastJson = new JSONObject(result);
                    JSONObject stockArray = forecastJson.getJSONArray(VSG_RESULTS).getJSONObject(0);

                    stock_name.setText(stockArray.getString(VSG_NAME));
                    close_price.setText(stockArray.getString(VSG_CLOSE));
                    open_price.setText(stockArray.getString(VSG_OPEN));
                    total_volume.setText(stockArray.getString(VSG_VOLUME));
                    price_high.setText(stockArray.getString(VSG_HIGH));
                    exchange_name.setText(stockArray.getString(VSG_EXCHANGE));
                    price_low.setText(stockArray.getString(VSG_LOW));
                    price_percent_change.setText(stockArray.getString(VSG_PER_CHANGE));




                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //
                //forecastAdapter.clear();
                // New data is back from the server.  Hooray!
            }
        }
    }
}
