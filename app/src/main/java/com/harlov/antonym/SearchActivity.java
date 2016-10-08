package com.harlov.antonym;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SearchActivity extends AppCompatActivity
                    implements NavigationView.OnNavigationItemSelectedListener {

    private String searchResults;
    private boolean isSearchResultsEmpty = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setTitle(R.string.search_activity_title);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_search);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view_search);
        navigationView.setNavigationItemSelectedListener(this);

        checkFirstRun();
        retrieveCurrentSearchData();
        displaySearchResults();
    }

    @Override
    protected void onNewIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            onSearchButtonClicked(query);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            SearchView searchView = (SearchView) findViewById(R.id.action_search);
            searchView.setQuery(data.toString(), false);
            searchView.clearFocus();
            onSuggestionClicked(data);
        } else if (intent.getAction().equals(Constants.ALERT_DIALOG_ACTION)){
            boolean showAlert = intent.getExtras().getBoolean(Constants.ALERT_DIALOG_STATE);
            if (showAlert == true){
                showAlertDialog();
            }
        }

        super.onNewIntent(intent);
    }

    private void showAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.alert_dialog_title);
        alertDialogBuilder.setMessage(R.string.alert_dialog_message);
        alertDialogBuilder.setPositiveButton(R.string.alert_dialog_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent mServiceIntent = new Intent(getBaseContext(), InternetCheckIntentService.class);
                getBaseContext().startService(mServiceIntent);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_quiz) {
            saveCurrentSearchData();
            Intent intent = new Intent(this, QuizActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_search) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_search);
            drawer.closeDrawer(GravityCompat.START);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_search);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onSearchButtonClicked(String searchWord) {

        if (!isEmpty(searchWord)){
            Toast.makeText(this, R.string.search_performed_toast, Toast.LENGTH_SHORT).show();
            searchWord = searchWord.trim().replace(" ", "");
            new GetSearchResults().execute(searchWord);
            Intent mServiceIntent = new Intent(getBaseContext(), InternetCheckIntentService.class);
            getBaseContext().startService(mServiceIntent);
        } else {
            Toast.makeText(this, R.string.search_wrong_query_toast, Toast.LENGTH_LONG).show();
        }
    }

    public void onSuggestionClicked(Uri data) {

        String searchWord = data.getLastPathSegment().toLowerCase();

        if (!isEmpty(searchWord)){
            Toast.makeText(this, R.string.search_performed_toast, Toast.LENGTH_SHORT).show();
            searchWord = searchWord.trim().replace(" ", "");
            new GetSearchResults().execute(searchWord);
            Intent mServiceIntent = new Intent(getBaseContext(), InternetCheckIntentService.class);
            getBaseContext().startService(mServiceIntent);
        } else {
            Toast.makeText(this, R.string.search_wrong_query_toast, Toast.LENGTH_LONG).show();
        }
    }

    protected boolean isEmpty(String searchWord){
        return searchWord.trim().replace(" ", "").length() == 0;
    }

    private class GetSearchResults extends AsyncTask<String, Void, Void> {

        private String jsonString = "";
        private Boolean isEmpty = true;
        private StringBuilder result = new StringBuilder();

        @Override
        protected Void doInBackground(String... params) {

            String searchWord = params[0];
            /*DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httpPost = new HttpPost("http://bro.netau.net/translate.php?action=translations&english_words=" + searchWord);
            httpPost.setHeader("Content-type", "application/json");*/
            InputStream inputStream = null;

            try{
                /*HttpResponse response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();*/

                URL urlSearch = new URL(Constants.SEARCH_ACTIVITY_URL + searchWord);
                HttpURLConnection urlConnection = (HttpURLConnection) urlSearch.openConnection();
                inputStream = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null){
                    sb.append(line + "\n");
                }

                jsonString = sb.toString();
                JSONObject jObject = new JSONObject(jsonString);
                JSONArray jArray = jObject.getJSONArray(Constants.SEARCH_ACTIVITY_JSON_ARRAY);

                inputStream.close();
                reader.close();
                urlConnection.disconnect();

                extractSearchResults(jArray);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        private void extractSearchResults(JSONArray jsonArray) {

            String[] languages = {"english", "arabic", "chinese", "french",
                    "german", "italian", "japanese", "korean",
                    "russian", "spanish", "swedish"};

            try {
                JSONObject oppositeWordObject = jsonArray.getJSONObject(0);

                if (!oppositeWordObject.getString(languages[0]).equals("")){
                    isEmpty = false;

                    for (int i = 0; i < jsonArray.length(); i++) {

                        oppositeWordObject = jsonArray.getJSONObject(i);

                        result.append(languages[i]).append(" : ").
                                append(oppositeWordObject.getString(languages[i])).append(",");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if (isEmpty.equals(false)){
                searchResults = result.toString().toUpperCase();
                isSearchResultsEmpty = isEmpty;
                displaySearchResults();
            } else {
                isSearchResultsEmpty = isEmpty;
                displaySearchResults();
            }
        }
    }

    private void checkFirstRun() {

        final String PREFS_NAME = Constants.SEARCH_ACTIVITY_FIRST_RUN_PREF;
        final String PREF_VERSION_CODE_KEY = Constants.SEARCH_ACTIVITY_VERSION_CODE_PREF_KEY;
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = 0;

        try {
            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            // This is just a normal run
            //Intent mServiceIntent = new Intent(getBaseContext(), InternetCheckIntentService.class);
            //getBaseContext().startService(mServiceIntent);
            return;
        } else if (savedVersionCode == DOESNT_EXIST) {
            // This is a new install (or the user cleared the shared preferences)
            searchResults = "";
            searchResults = searchResults + Constants.DEFAULT_SEARCH_RESULTS;
            searchResults = searchResults.toUpperCase();
            saveCurrentSearchData();

            //Intent mServiceIntent = new Intent(getBaseContext(), InternetCheckIntentService.class);
            //getBaseContext().startService(mServiceIntent);
        } else if (currentVersionCode > savedVersionCode) {
            // This is an upgrade
        }

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }

    private void saveCurrentSearchData() {

        final String PREFS_NAME = Constants.CURRENT_SEARCH_DATA_PREF;
        final String PREF_SEARCH_RESULTS_KEY = Constants.SEARCH_RESULTS_PREF_KEY;

        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(PREF_SEARCH_RESULTS_KEY, searchResults);
        editor.apply();
    }

    private void retrieveCurrentSearchData(){

        final String PREFS_NAME = Constants.CURRENT_SEARCH_DATA_PREF;
        final String PREF_SEARCH_RESULTS_KEY = Constants.SEARCH_RESULTS_PREF_KEY;
        final String DEFAULT_VALUE = "";

        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        searchResults = sharedPref.getString(PREF_SEARCH_RESULTS_KEY, DEFAULT_VALUE);
    }

    public void displaySearchResults(){
        if (isSearchResultsEmpty == true){
            Toast.makeText(this, R.string.search_wrong_query_toast, Toast.LENGTH_SHORT).show();
        }

        String[] listItems = searchResults.split(",");

        ListAdapter listAdapter = new ArrayAdapter<String>(
                getBaseContext(), R.layout.custom_text_view, listItems);

        ListView searchResultsListView = (ListView) findViewById(R.id.searchResultsListView);
        searchResultsListView.setAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_search);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCurrentSearchData();
        setReceiverState(PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setReceiverState(PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
    }

    private void setReceiverState(int receiverState) {
        ComponentName receiver = new ComponentName(this, NetworkChangeReceiver.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver, receiverState,
                PackageManager.DONT_KILL_APP);
    }
}
