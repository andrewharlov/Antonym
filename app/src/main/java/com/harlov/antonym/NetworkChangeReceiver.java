package com.harlov.antonym;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "InternetCheck";

    public NetworkChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null){
            if (intent.getAction().equals(Constants.SERVICE_BROADCAST_ACTION)){
                boolean isInternetAvailable = intent.getExtras().
                        getBoolean(Constants.INTERNET_ACCESS_STATE);

                if (isInternetAvailable != true){
                    //Toast.makeText(context, "Internet is not available", Toast.LENGTH_SHORT).show();
                    //Log.w(TAG, "onReceive() , Network is not connected");

                    Intent quizActivityIntent;

                    if (QuizActivity.mQuizActivity != null){
                        quizActivityIntent = new Intent(Constants.ALERT_DIALOG_ACTION, null, context, QuizActivity.class);
                    } else {
                        quizActivityIntent = new Intent(Constants.ALERT_DIALOG_ACTION, null, context, SearchActivity.class);
                    }

                    quizActivityIntent.putExtra(Constants.ALERT_DIALOG_STATE, true);
                    quizActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(quizActivityIntent);
                } else {
                    //Toast.makeText(context, "Internet is available", Toast.LENGTH_SHORT).show();
                    //Log.w(TAG, "onReceive() , Network is connected");
                }
            } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                Intent mServiceIntent = new Intent(context, InternetCheckIntentService.class);
                context.startService(mServiceIntent);
            }
        }
    }
}
