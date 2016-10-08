package com.harlov.antonym;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CustomSuggestionsProvider extends ContentProvider{
    private static final String TAG = "SuggestionsDatabase";

    public static final String COL_ID = "_id";
    public static final String COL_WORD = "suggest_text_1";
    public static final String COL_INTENT_DATA = "suggest_intent_data";

    private static final String DATABASE_NAME = "SUGGESTIONS";
    private static final String FTS_VIRTUAL_TABLE = "FTS";
    private static final int DATABASE_VERSION = 1;

    private DatabaseOpenHelper mDatabaseOpenHelper;
    private static SQLiteDatabase mDatabase;

    @Override
    public boolean onCreate() {
        //Log.w(TAG, "ContentProvider - onCreate");
        mDatabaseOpenHelper = new DatabaseOpenHelper(getContext());
        //Log.w(TAG, "ContentProvider - called getReadableDatabase()");
        mDatabaseOpenHelper.getReadableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setTables(FTS_VIRTUAL_TABLE);

        String query = uri.getLastPathSegment().toLowerCase();
        selection = FTS_VIRTUAL_TABLE + " MATCH ?";
        selectionArgs = new String[] {query + "*"};

        Cursor cursor = qBuilder.query(mDatabaseOpenHelper.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private static class DatabaseOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        COL_ID + ", " +
                        COL_WORD + ", " +
                        COL_INTENT_DATA + ")";

        DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            //Log.w(TAG, "SQLiteOpenHelper - constructor");
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //Log.w(TAG, "SQLiteOpenHelper - onCreate");
            db.execSQL(FTS_TABLE_CREATE);
            loadDictionary();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /*Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");*/
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }

        private void loadDictionary() {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        loadWords();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        private void loadWords() throws IOException {
            //Log.w(TAG, "SQLiteOpenHelper - called loadWords()");
            final Resources resources = mHelperContext.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.suggestions);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] strings = TextUtils.split(line, "-");
                    if (strings.length < 2) continue;
                    long id = addWord(strings[0].trim(), strings[1].trim());
                    if (id < 0) {
                        //Log.e(TAG, "Unable to add word: " + strings[0].trim());
                    }
                }
            } finally {
                reader.close();
            }
        }

        public long addWord(String id, String word) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_ID, id);
            initialValues.put(COL_WORD, word);
            initialValues.put(COL_INTENT_DATA, word);

            mDatabase = getWritableDatabase();
            return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }
    }
}
