package com.harlov.antonym;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import java.util.ArrayList;
import java.util.Collections;

public class QuizActivity extends AppCompatActivity
                        implements NavigationView.OnNavigationItemSelectedListener{

    public static QuizActivity mQuizActivity = null;
    private String quizWord;
    private String correctAnswer;
    private String allLanguagesCorrectAnswer;
    private ArrayList<String> quizAnswersList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_quiz);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        
        getSupportActionBar().setTitle(R.string.quiz_activity_title);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_quiz);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view_quiz);
        navigationView.setNavigationItemSelectedListener(this);

        checkFirstRun();
        quizAnswersList.clear();
        retrieveCurrentQuizData();
        displayQuizData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction().equals(Constants.ALERT_DIALOG_ACTION)){
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

    class GetNextWords extends AsyncTask<Void, Void, Void> {

        private String jsonString = "";
        private StringBuilder resultSB = new StringBuilder();

        @Override
        protected Void doInBackground(Void... params) {

            InputStream inputStream = null;

            try {

                URL urlQuiz = new URL(Constants.QUIZ_ACTIVITY_URL_QUIZ);
                HttpURLConnection urlConnection = (HttpURLConnection) urlQuiz.openConnection();
                inputStream = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null){
                    sb.append(line + "\n");
                }

                jsonString = sb.toString();
                JSONObject jObject = new JSONObject(jsonString);
                JSONArray jArrayQuiz = jObject.getJSONArray(Constants.QUIZ_ACTIVITY_JSON_ARRAY_QUIZ);

                inputStream.close();
                reader.close();
                urlConnection.disconnect();

                outputQuizData(jArrayQuiz);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void outputQuizData(JSONArray jsonArrayQuiz) {

            final String PREFS_NAME = Constants.QUIZ_ACTIVITY_NEXT_DATA_PREF;
            final String PREF_QUIZ_WORD_KEY = Constants.QUIZ_ACTIVITY_NEXT_QUIZ_WORD_PREF_KEY;
            final String PREF_CORRECT_ANSWER_KEY = Constants.QUIZ_ACTIVITY_NEXT_CORRECT_ANSWER_PREF_KEY;
            final String PREF_FIRST_BUTTON_KEY = Constants.QUIZ_ACTIVITY_NEXT_FIRST_BUTTON_PREF_KEY;
            final String PREF_SECOND_BUTTON_KEY = Constants.QUIZ_ACTIVITY_NEXT_SECOND_BUTTON_PREF_KEY;
            final String PREF_THIRD_BUTTON_KEY = Constants.QUIZ_ACTIVITY_NEXT_THIRD_BUTTON_PREF_KEY;
            final String PREF_ALL_LANG_CORRECT_ANSWER_KEY = Constants.QUIZ_ACTIVITY_NEXT_ALL_LANG_CORRECT_ANSWER_PREF_KEY;

            String wordToGetOpposite = "";

            String[] words = {"quiz_word", "correct_answer", "first_wrong_answer",
                    "second_wrong_answer"};

            String[] languages = {"english", "arabic", "chinese", "french",
                    "german", "italian", "japanese", "korean",
                    "russian", "spanish", "swedish"};

            SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            try {
                for (int i = 0; i < jsonArrayQuiz.length(); i++){

                    JSONObject jsonObject = jsonArrayQuiz.getJSONObject(i);

                    if (i == 0){
                        wordToGetOpposite = jsonObject.getString(words[i]);
                        editor.putString(PREF_QUIZ_WORD_KEY, jsonObject.getString(words[i]));
                    } else if (i == 1){
                        editor.putString(PREF_CORRECT_ANSWER_KEY, jsonObject.getString(words[i]));
                        editor.putString(PREF_FIRST_BUTTON_KEY, jsonObject.getString(words[i]));
                    } else if (i == 2) {
                        editor.putString(PREF_SECOND_BUTTON_KEY, jsonObject.getString(words[i]));
                    } else if (i == 3) {
                        editor.putString(PREF_THIRD_BUTTON_KEY, jsonObject.getString(words[i]));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            InputStream inputStream = null;
            JSONArray jsonArrayTranslations = new JSONArray();

            try {
                URL urlTranslations = new URL(Constants.QUIZ_ACTIVITY_URL_TRANSLATIONS + wordToGetOpposite);
                HttpURLConnection urlConnection = (HttpURLConnection) urlTranslations.openConnection();
                inputStream = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null){
                    sb.append(line + "\n");
                }

                jsonString = sb.toString();
                JSONObject jObject = new JSONObject(jsonString);
                jsonArrayTranslations = jObject.getJSONArray(Constants.QUIZ_ACTIVITY_JSON_ARRAY_TRANSLATIONS);

                inputStream.close();
                reader.close();
                urlConnection.disconnect();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                for (int i = 0; i < jsonArrayTranslations.length(); i++){
                    JSONObject oppositeWordObject = jsonArrayTranslations.getJSONObject(i);

                    resultSB.append(languages[i]).append(" : ").
                            append(oppositeWordObject.getString(languages[i])).append(",");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            editor.putString(PREF_ALL_LANG_CORRECT_ANSWER_KEY, resultSB.toString());
            editor.apply();
        }
    }

    public void displayQuizData(){

        TextView quizWordTextView = (TextView) findViewById(R.id.quizWordTextView);
        quizWordTextView.setText(quizWord);
        Collections.shuffle(quizAnswersList);

        Button firstButton = (Button) findViewById(R.id.firstButton);
        Button secondButton = (Button) findViewById(R.id.secondButton);
        Button thirdButton = (Button) findViewById(R.id.thirdButton);

        if (!quizAnswersList.isEmpty()){
            try {
                firstButton.setText(quizAnswersList.get(0));
                secondButton.setText(quizAnswersList.get(1));
                thirdButton.setText(quizAnswersList.get(2));
            } catch (IndexOutOfBoundsException e){
                e.printStackTrace();
            }
        }

    }

    public void saveCurrentQuizData(){

        final String PREFS_NAME = Constants.QUIZ_ACTIVITY_CURRENT_DATA_PREF;
        final String PREF_QUIZ_WORD_KEY = Constants.QUIZ_ACTIVITY_CURRENT_QUIZ_WORD_PREF_KEY;
        final String PREF_CORRECT_ANSWER_KEY = Constants.QUIZ_ACTIVITY_CURRENT_CORRECT_ANSWER_PREF_KEY;
        final String PREF_ALL_LANG_CORRECT_ANSWER_KEY = Constants.QUIZ_ACTIVITY_CURRENT_ALL_LANG_CORRECT_ANSWER_PREF_KEY;
        final String PREF_FIRST_BUTTON_KEY = Constants.QUIZ_ACTIVITY_CURRENT_FIRST_BUTTON_PREF_KEY;
        final String PREF_SECOND_BUTTON_KEY = Constants.QUIZ_ACTIVITY_CURRENT_SECOND_BUTTON_PREF_KEY;
        final String PREF_THIRD_BUTTON_KEY = Constants.QUIZ_ACTIVITY_CURRENT_THIRD_BUTTON_PREF_KEY;

        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(PREF_QUIZ_WORD_KEY, quizWord);
        editor.putString(PREF_CORRECT_ANSWER_KEY, correctAnswer);
        editor.putString(PREF_ALL_LANG_CORRECT_ANSWER_KEY, allLanguagesCorrectAnswer);

        if (!quizAnswersList.isEmpty()) {
            try {
                editor.putString(PREF_FIRST_BUTTON_KEY, quizAnswersList.get(0));
                editor.putString(PREF_SECOND_BUTTON_KEY, quizAnswersList.get(1));
                editor.putString(PREF_THIRD_BUTTON_KEY, quizAnswersList.get(2));
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        editor.apply();
    }

    public void retrieveCurrentQuizData(){

        final String PREFS_NAME = Constants.QUIZ_ACTIVITY_CURRENT_DATA_PREF;
        final String PREF_QUIZ_WORD_KEY = Constants.QUIZ_ACTIVITY_CURRENT_QUIZ_WORD_PREF_KEY;
        final String PREF_CORRECT_ANSWER_KEY = Constants.QUIZ_ACTIVITY_CURRENT_CORRECT_ANSWER_PREF_KEY;
        final String PREF_ALL_LANG_CORRECT_ANSWER_KEY = Constants.QUIZ_ACTIVITY_CURRENT_ALL_LANG_CORRECT_ANSWER_PREF_KEY;
        final String PREF_FIRST_BUTTON_KEY = Constants.QUIZ_ACTIVITY_CURRENT_FIRST_BUTTON_PREF_KEY;
        final String PREF_SECOND_BUTTON_KEY = Constants.QUIZ_ACTIVITY_CURRENT_SECOND_BUTTON_PREF_KEY;
        final String PREF_THIRD_BUTTON_KEY = Constants.QUIZ_ACTIVITY_CURRENT_THIRD_BUTTON_PREF_KEY;
        final String DEFAULT_VALUE = "";

        resetButtonsColor();

        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        quizWord = sharedPref.getString(PREF_QUIZ_WORD_KEY, DEFAULT_VALUE);
        correctAnswer = sharedPref.getString(PREF_CORRECT_ANSWER_KEY, DEFAULT_VALUE);
        allLanguagesCorrectAnswer = sharedPref.getString(PREF_ALL_LANG_CORRECT_ANSWER_KEY, DEFAULT_VALUE);
        quizAnswersList.add(sharedPref.getString(PREF_FIRST_BUTTON_KEY, DEFAULT_VALUE));
        quizAnswersList.add(sharedPref.getString(PREF_SECOND_BUTTON_KEY, DEFAULT_VALUE));
        quizAnswersList.add(sharedPref.getString(PREF_THIRD_BUTTON_KEY, DEFAULT_VALUE));
    }

    public void retrieveNextQuizData(){

        final String PREFS_NAME = Constants.QUIZ_ACTIVITY_NEXT_DATA_PREF;
        final String PREF_QUIZ_WORD_KEY = Constants.QUIZ_ACTIVITY_NEXT_QUIZ_WORD_PREF_KEY;
        final String PREF_CORRECT_ANSWER_KEY = Constants.QUIZ_ACTIVITY_NEXT_CORRECT_ANSWER_PREF_KEY;
        final String PREF_ALL_LANG_CORRECT_ANSWER_KEY = Constants.QUIZ_ACTIVITY_NEXT_ALL_LANG_CORRECT_ANSWER_PREF_KEY;
        final String PREF_FIRST_BUTTON_KEY = Constants.QUIZ_ACTIVITY_NEXT_FIRST_BUTTON_PREF_KEY;
        final String PREF_SECOND_BUTTON_KEY = Constants.QUIZ_ACTIVITY_NEXT_SECOND_BUTTON_PREF_KEY;
        final String PREF_THIRD_BUTTON_KEY = Constants.QUIZ_ACTIVITY_NEXT_THIRD_BUTTON_PREF_KEY;

        final String DEFAULT_QUIZ_WORD = Constants.DEFAULT_QUIZ_WORD;
        final String DEFAULT_CORRECT_ANSWER = Constants.DEFAULT_CORRECT_ANSWER;
        final String DEFAULT_ALL_LANG_CORRECT_ANSWER = Constants.DEFAULT_ALL_LANG_CORRECT_ANSWER;
        final String DEFAULT_FIRST_BUTTON = Constants.DEFAULT_FIRST_BUTTON;
        final String DEFAULT_SECOND_BUTTON = Constants.DEFAULT_SECOND_BUTTON;
        final String DEFAULT_THIRD_BUTTON = Constants.DEFAULT_THIRD_BUTTON;

        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (quizWord.equals(sharedPref.getString(PREF_QUIZ_WORD_KEY, DEFAULT_QUIZ_WORD))){
            Toast.makeText(this, R.string.quiz_getting_next_word_toast, Toast.LENGTH_LONG).show();
        }else {
            resetButtonsColor();
            quizWord = sharedPref.getString(PREF_QUIZ_WORD_KEY, DEFAULT_QUIZ_WORD);
            correctAnswer = sharedPref.getString(PREF_CORRECT_ANSWER_KEY, DEFAULT_CORRECT_ANSWER);
            allLanguagesCorrectAnswer = sharedPref.getString(PREF_ALL_LANG_CORRECT_ANSWER_KEY, DEFAULT_ALL_LANG_CORRECT_ANSWER);
            quizAnswersList.add(sharedPref.getString(PREF_FIRST_BUTTON_KEY, DEFAULT_FIRST_BUTTON));
            quizAnswersList.add(sharedPref.getString(PREF_SECOND_BUTTON_KEY, DEFAULT_SECOND_BUTTON));
            quizAnswersList.add(sharedPref.getString(PREF_THIRD_BUTTON_KEY, DEFAULT_THIRD_BUTTON));
        }
    }

    public void onNextClick(View view) {

        quizAnswersList.clear();
        retrieveNextQuizData();
        displayQuizData();
        saveCurrentQuizData();

        new GetNextWords().execute();

        Intent mServiceIntent = new Intent(getBaseContext(), InternetCheckIntentService.class);
        getBaseContext().startService(mServiceIntent);
    }

    public void onFirstButtonClick(View view) {

        String selectedAnswer = "";
        Button firstButton = (Button) findViewById(R.id.firstButton);
        selectedAnswer = firstButton.getText().toString();

        if (selectedAnswer.equals(correctAnswer)){
            setButtonColor(firstButton, true);
        } else {
            setButtonColor(firstButton, false);
        }
    }

    public void onSecondButtonClick(View view) {

        String selectedAnswer = "";
        Button secondButton = (Button) findViewById(R.id.secondButton);
        selectedAnswer = secondButton.getText().toString();

        if (selectedAnswer.equals(correctAnswer)){
            setButtonColor(secondButton, true);
        } else {
            setButtonColor(secondButton, false);
        }
    }

    public void onThirdButtonClick(View view) {

        String selectedAnswer = "";
        Button thirdButton = (Button) findViewById(R.id.thirdButton);
        selectedAnswer = thirdButton.getText().toString();

        if (selectedAnswer.equals(correctAnswer)){
            setButtonColor(thirdButton, true);
        } else {
            setButtonColor(thirdButton, false);
        }
    }

    public void setButtonColor(Button button, boolean isCorrect){
        GradientDrawable drawable = (GradientDrawable) button.getBackground();
        Resources resources = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, resources.getDisplayMetrics());

        if (isCorrect == true){
            drawable.setStroke(px, Color.GREEN);
        } else {
            drawable.setStroke(px, Color.RED);
        }
    }

    public void resetButtonsColor(){
        Button firstButton = (Button) findViewById(R.id.firstButton);
        Button secondButton = (Button) findViewById(R.id.secondButton);
        Button thirdButton = (Button) findViewById(R.id.thirdButton);

        Resources resources = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, resources.getDisplayMetrics());

        GradientDrawable drawable = (GradientDrawable) firstButton.getBackground();
        drawable.setStroke(px, Color.WHITE);
        drawable = (GradientDrawable) secondButton.getBackground();
        drawable.setStroke(px, Color.WHITE);
        drawable = (GradientDrawable) thirdButton.getBackground();
        drawable.setStroke(px, Color.WHITE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quiz_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.navigate_to_help){
            saveCurrentQuizData();
            Intent intent = new Intent(this, HelpActivity.class);
            intent.putExtra(Constants.ALL_LANG_CORRECT_ANSWER_STRING, allLanguagesCorrectAnswer);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_quiz) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_quiz);
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_search) {
            saveCurrentQuizData();
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_quiz);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_quiz);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void checkFirstRun() {

        final String PREFS_NAME = Constants.QUIZ_ACTIVITY_FIRST_RUN_PREF;
        final String PREF_VERSION_CODE_KEY = Constants.QUIZ_ACTIVITY_VERSION_CODE_PREF_KEY;
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = 0;
        try {
            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            // handle exception
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
            quizWord = Constants.DEFAULT_QUIZ_WORD;
            correctAnswer = Constants.DEFAULT_CORRECT_ANSWER;
            quizAnswersList.add(Constants.DEFAULT_FIRST_BUTTON);
            quizAnswersList.add(Constants.DEFAULT_SECOND_BUTTON);
            quizAnswersList.add(Constants.DEFAULT_THIRD_BUTTON);
            allLanguagesCorrectAnswer = "";
            allLanguagesCorrectAnswer = allLanguagesCorrectAnswer + Constants.DEFAULT_ALL_LANG_CORRECT_ANSWER;

            saveCurrentQuizData();

            new GetNextWords().execute();

            //Intent mServiceIntent = new Intent(getBaseContext(), InternetCheckIntentService.class);
            //getBaseContext().startService(mServiceIntent);
        } else if (currentVersionCode > savedVersionCode) {
            // This is an upgrade
        }

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mQuizActivity = null;
        saveCurrentQuizData();
        setReceiverState(PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mQuizActivity = this;
        setReceiverState(PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
    }

    private void setReceiverState(int receiverState) {
        ComponentName receiver = new ComponentName(this, NetworkChangeReceiver.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver, receiverState,
                PackageManager.DONT_KILL_APP);
    }
}
