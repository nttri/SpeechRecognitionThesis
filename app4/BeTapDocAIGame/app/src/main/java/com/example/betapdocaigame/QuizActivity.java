package com.example.betapdocaigame;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class QuizActivity extends AppCompatActivity {

    private int currentQuizNumber = 0;
    private int currentScore = 0;
    private String currentQuizText = "";
    private String currentQuizImage = "";
    private String currentTopicName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        loadData();
        setupUI();
    }

    private void loadData() {
        //load extra data
        currentQuizNumber = getIntent().getIntExtra("QuizNumber", 0) + 1;
        currentScore = getIntent().getIntExtra("Score", 0);
        currentQuizText = getIntent().getStringExtra("QuizText");
        currentQuizImage = getIntent().getStringExtra("QuizImage");
        currentTopicName = getIntent().getStringExtra("TopicName");
    }

    private void setupUI() {
        //set title on toolbar
        String title = "Câu hỏi số: " + currentQuizNumber;
        getSupportActionBar().setTitle(title);

        //set activity background color
        getWindow().getDecorView().setBackgroundColor(Color.rgb(218,218,218));
    }

    @Override
    public void onBackPressed() {
        if (currentQuizNumber == 10) {
            closeActivity();
            return;
        }

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        closeActivity();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_on_exit)
                .setPositiveButton(R.string.yes_button, dialogClickListener)
                .setNegativeButton(R.string.no_button, dialogClickListener).show();
    }

    private void closeActivity() {
        this.finish();
    }
}
