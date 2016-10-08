package com.harlov.antonym;

public final class Constants {
    public static final String SERVICE_BROADCAST_ACTION = "com.harlov.antonym.SERVICE_INTERNET_STATUS";
    public static final String INTERNET_ACCESS_STATE = "com.harlov.antonym.INTERNET_ACCESS_STATE";

    public static final String ALERT_DIALOG_ACTION = "com.harlov.antonym.ALERT_DIALOG_ACTION";
    public static final String ALERT_DIALOG_STATE = "com.harlov.antonym.ALERT_DIALOG_STATE";

    public static final String WEB_SERVICE_URL = "http://bro.netau.net";

    // Constants for Search Activity
    public static final String SEARCH_ACTIVITY_URL = "http://bro.netau.net/translate.php?action=translations&english_words=";
    public static final String SEARCH_ACTIVITY_JSON_ARRAY = "translations";

    public static final String SEARCH_ACTIVITY_FIRST_RUN_PREF = "FirstRunSearchActivity";
    public static final String SEARCH_ACTIVITY_VERSION_CODE_PREF_KEY = "version_code";
    public static final String CURRENT_SEARCH_DATA_PREF = "SearchCurrentData";
    public static final String SEARCH_RESULTS_PREF_KEY = "searchResults";

    public static final String DEFAULT_SEARCH_RESULTS =
                    "english :" + "," + "arabic :" + "," + "chinese :" + "," +
                    "french :" + "," + "german :" + "," + "italian :" + "," +
                    "japanese :" + "," + "korean :" + "," + "russian :" + "," +
                    "spanish :" + "," + "swedish :" + ",";

    // Constants for Quiz Activity
    public static final String ALL_LANG_CORRECT_ANSWER_STRING = "allLanguagesCorrectAnswer";

    public static final String QUIZ_ACTIVITY_URL_QUIZ = "http://bro.netau.net/translate.php?action=quiz";
    public static final String QUIZ_ACTIVITY_JSON_ARRAY_QUIZ = "quiz";
    public static final String QUIZ_ACTIVITY_URL_TRANSLATIONS = "http://bro.netau.net/translate.php?action=translations&english_words=";
    public static final String QUIZ_ACTIVITY_JSON_ARRAY_TRANSLATIONS = "translations";

    public static final String QUIZ_ACTIVITY_FIRST_RUN_PREF = "FirstRunQuizActivity";
    public static final String QUIZ_ACTIVITY_VERSION_CODE_PREF_KEY = "version_code";

    public static final String QUIZ_ACTIVITY_NEXT_DATA_PREF = "QuizNextData";
    public static final String QUIZ_ACTIVITY_NEXT_QUIZ_WORD_PREF_KEY = "quizWord";
    public static final String QUIZ_ACTIVITY_NEXT_CORRECT_ANSWER_PREF_KEY = "correctAnswer";
    public static final String QUIZ_ACTIVITY_NEXT_ALL_LANG_CORRECT_ANSWER_PREF_KEY = "allLanguagesCorrectAnswer";
    public static final String QUIZ_ACTIVITY_NEXT_FIRST_BUTTON_PREF_KEY = "firstButton";
    public static final String QUIZ_ACTIVITY_NEXT_SECOND_BUTTON_PREF_KEY = "secondButton";
    public static final String QUIZ_ACTIVITY_NEXT_THIRD_BUTTON_PREF_KEY = "thirdButton";

    public static final String QUIZ_ACTIVITY_CURRENT_DATA_PREF = "QuizCurrentData";
    public static final String QUIZ_ACTIVITY_CURRENT_QUIZ_WORD_PREF_KEY = "quizWord";
    public static final String QUIZ_ACTIVITY_CURRENT_CORRECT_ANSWER_PREF_KEY = "correctAnswer";
    public static final String QUIZ_ACTIVITY_CURRENT_ALL_LANG_CORRECT_ANSWER_PREF_KEY = "allLanguagesCorrectAnswer";
    public static final String QUIZ_ACTIVITY_CURRENT_FIRST_BUTTON_PREF_KEY = "firstButton";
    public static final String QUIZ_ACTIVITY_CURRENT_SECOND_BUTTON_PREF_KEY = "secondButton";
    public static final String QUIZ_ACTIVITY_CURRENT_THIRD_BUTTON_PREF_KEY = "thirdButton";

    public static final String DEFAULT_QUIZ_WORD = "break";
    public static final String DEFAULT_CORRECT_ANSWER = "create";
    public static final String DEFAULT_FIRST_BUTTON = DEFAULT_CORRECT_ANSWER;
    public static final String DEFAULT_SECOND_BUTTON = "host";
    public static final String DEFAULT_THIRD_BUTTON = "expand";
    public static final String DEFAULT_ALL_LANG_CORRECT_ANSWER =
            "english : " + "create" + "," +
            "arabic : " + "خلق" + "," +
            "chinese : " + "创建" + "," +
            "french : " + "Créer" + "," +
            "german : " + "erstellen" + "," +
            "italian : " + "creare" + "," +
            "japanese : " + "作ります" + "," +
            "korean : " + "작성" + "," +
            "russian : " + "создать" + "," +
            "spanish : " + "crear" + "," +
            "swedish : " + "skapa" + ",";
}
