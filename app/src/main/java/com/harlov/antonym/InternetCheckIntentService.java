package com.harlov.antonym;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class InternetCheckIntentService extends IntentService {
    private static final String TAG = "InternetCheck";

    public InternetCheckIntentService() {
        super("InternetCheckIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean isInternetAvailable = false;
        boolean isNetworkConnected = isNetworkConnected(getBaseContext());

        if (isNetworkConnected == true){
            //Log.w(TAG, "isNetworkConnected() = true, Network is connected");
            isInternetAvailable = isInternetAvailable();
        } else {
            //Log.w(TAG, "isNetworkConnected() = false, Network is not connected");
        }

        Intent localIntent = new Intent(Constants.SERVICE_BROADCAST_ACTION);
        localIntent.putExtra(Constants.INTERNET_ACCESS_STATE, isInternetAvailable);
        sendBroadcast(localIntent);
    }

    private boolean isNetworkConnected(Context context){
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    private boolean isInternetAvailable(){
        try {
            URL url = new URL(Constants.WEB_SERVICE_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.connect();

            // 200 = "OK" code (http connection is fine)
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                /*Log.w(TAG, "isInternetAvailable() = true, urlConnection.getResponseCode() = "
                        + urlConnection.getResponseCode() + ", Internet is available");*/
                urlConnection.disconnect();
                return true;
            } else {
                /*Log.w(TAG, "isInternetAvailable() = false, urlConnection.getResponseCode() = "
                        + urlConnection.getResponseCode() + ", Internet is not available");*/
                urlConnection.disconnect();
                return false;
            }
        } catch (MalformedURLException e) {
            /*Log.w(TAG, "isInternetAvailable() = false, " + e.getClass().getSimpleName() +
                    ", Internet is not available");*/
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            /*Log.w(TAG, "isInternetAvailable() = false, " + e.getClass().getSimpleName() +
                    ", Internet is not available");*/
            e.printStackTrace();
            return false;
        }
    }
}
