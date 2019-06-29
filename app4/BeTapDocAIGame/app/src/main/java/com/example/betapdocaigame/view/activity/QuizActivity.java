package com.example.betapdocaigame.view.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.betapdocaigame.R;

public class QuizActivity extends AppCompatActivity {

    private int currentQuizNumber = 0;
    private int currentScore = 0;
    private String currentQuizText = "";
    private String currentQuizImage = "";
    private String currentTopicName = "";

    //UI components
    private TextView tvScore;
    private ImageView ivQuizImage;
    private TextView tvQuizText;
    private TextView tvAnswer;
    private TextView tvTimer;
    private FloatingActionButton btnRecord;
    private FloatingActionButton btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvScore = findViewById(R.id.tvScore);
        ivQuizImage = findViewById(R.id.ivQuizImage);
        tvQuizText = findViewById(R.id.tvQuizText);
        tvAnswer = findViewById(R.id.tvAnswer);
        tvTimer = findViewById(R.id.tvTimer);
        btnRecord = findViewById(R.id.btnRecord);
        btnNext = findViewById(R.id.btnNext);

        loadData();
        setupUI();
        setupEvent();
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

        //score
        tvScore.setText("Điểm: " + currentScore);

        //quiz
        int id = this.getResources().getIdentifier(currentQuizImage , "drawable", this.getPackageName());
        ivQuizImage.setImageResource(id);
        tvQuizText.setText(currentQuizText);

        //answer
        tvAnswer.setText("");

        //timer
        tvTimer.setText("");
    }

    private void setupEvent() {

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecordPressed();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextPressed();
            }
        });
    }

    private void onRecordPressed() {

    }

    private void onNextPressed() {
        if (currentQuizNumber == 10) {
            closeActivity();
            return;
        }
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
