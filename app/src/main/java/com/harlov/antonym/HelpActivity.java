package com.harlov.antonym;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class HelpActivity extends AppCompatActivity {

    private String allLanguagesCorrectAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_help);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.help_activity_title);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle quizData = getIntent().getExtras();
        if (quizData != null){
            allLanguagesCorrectAnswer = quizData.getString(Constants.ALL_LANG_CORRECT_ANSWER_STRING);
        }

        displayCorrectAnswers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayCorrectAnswers(){
        String answers = allLanguagesCorrectAnswer.toUpperCase();
        String[] listItems = answers.split(",");

        ListAdapter listAdapter = new ArrayAdapter<String>(this,
                R.layout.custom_text_view, listItems);

        ListView answersListView = (ListView) findViewById(R.id.answersListView);
        answersListView.setAdapter(listAdapter);
    }
}
