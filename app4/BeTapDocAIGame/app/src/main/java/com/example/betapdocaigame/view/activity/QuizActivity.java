package com.example.betapdocaigame.view.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.betapdocaigame.R;
import com.example.betapdocaigame.data.MyQuiz;
import com.example.betapdocaigame.helper.AudioConverter;
import com.example.betapdocaigame.helper.DataHelper;
import com.example.betapdocaigame.helper.PermissionHelper;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QuizActivity extends AppCompatActivity {


    private int currentQuizNumber = 0;
    private int currentScore = 0;
    private String currentQuizText = "";
    private String currentQuizImage = "";
    private String currentTopicName = "";
    private String currentAnswer = "";

    //----UI components
    private TextView tvScore;
    private ImageView ivQuizImage;
    private TextView tvQuizText;
    private TextView tvAnswer;
    private TextView tvTimer;
    private FloatingActionButton btnRecord;
    private FloatingActionButton btnNext;
    //------------------------------------------

    //handle audio variables
    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private int BufferElements2Rec = 1024;
    private int BytesPerElement = 2; // 2 bytes = 16 bits

    private String pcmPathSave = Environment.getExternalStorageDirectory() + File.separator + "recording.pcm";
    private String wavPathSave = Environment.getExternalStorageDirectory() + File.separator + "recording.wav";

    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    //------------------------------------------

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
        tvTimer.setText("3s");

        //buttons
        enableButton(btnRecord);
        disableButton(btnNext);
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
        if (isRecording) {
            Toast.makeText(this, "Không thể thao tác khi đang ghi âm!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(PermissionHelper.getInstance().checkPermissionOnDevice(this)) {
            disableButton(btnRecord);
            startRecording();
            startTimer();
        } else {
            PermissionHelper.getInstance().requestPermission(this);
        }
    }

    private void onNextPressed() {
        if (isRecording) {
            Toast.makeText(this, "Không thể thao tác khi đang ghi âm!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentQuizNumber == 10) {
            closeActivity();
            return;
        }

        Intent intent = new Intent(this, QuizActivity.class);
        MyQuiz quiz = DataHelper.getInstance().getQuiz(currentTopicName, currentQuizNumber);

        if(quiz == null) {
            Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
            return;
        }

        intent.putExtra("TopicName", currentTopicName);
        intent.putExtra("QuizText", quiz.getText());
        intent.putExtra("QuizImage", quiz.getImage());
        intent.putExtra("QuizNumber", currentQuizNumber);
        intent.putExtra("Score", currentScore);

        this.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (isRecording) {
            Toast.makeText(this, "Không thể thao tác khi đang ghi âm!", Toast.LENGTH_SHORT).show();
            return;
        }

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
        this.startActivity(new Intent(this, MainActivity.class));
    }

    private void startRecording() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void writeAudioDataToFile() {
        short sData[] = new short[BufferElements2Rec];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(pcmPathSave);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            recorder.read(sData, 0, BufferElements2Rec);
            try {
                byte bData[] = short2byte(sData);
                os.write(bData, 0, BufferElements2Rec * BytesPerElement);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
        tvTimer.setTextColor(getResources().getColor(R.color.colorGreyText));
    }

    private void saveToWave() {
        try {
            AudioConverter.PCMToWAV(new File(pcmPathSave), new File(wavPathSave), 1, RECORDER_SAMPLERATE, 16);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startTimer() {
        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
                long sec = millisUntilFinished / 1000;
                String strSec = Long.toString(sec) + "s";
                tvTimer.setText(strSec);
            }

            public void onFinish() {
                stopRecording();
                saveToWave();
                sendRequest();
            }

        }.start();
    }

    private void sendRequest() {
        File file = new File(wavPathSave);
        OkHttpClient client = new OkHttpClient();
        String url = "https://vasr002.appspot.com/file";

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("voice",file.getName(),RequestBody.create(MediaType.parse("audio/wav"),file))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                resetQuiz();
                Log.w("Message failure", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String mMessage = response.body().string();
                if (response.isSuccessful()){
                    try {
                        JSONObject json = new JSONObject(mMessage);
                        String text = json.getString("text");
                        currentAnswer = text.replaceAll("\n","").trim();
                        handleAnswer();
                    } catch (Exception e){
                        resetQuiz();
                        e.printStackTrace();
                    }
                } else {
                    resetQuiz();
                }
            }
        });
    }

    private void handleAnswer() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                processAnswer();
            }
        });
    }

    private void processAnswer() {
        //enable next button
        enableButton(btnNext);

        //show answer
        tvAnswer.setText(currentAnswer);

        if (!currentAnswer.equals(currentQuizText)) {
            tvAnswer.setTextColor(this.getResources().getColor(R.color.colorWrong));
            return;
        }

        //handle correct answer
        currentScore += 10;
        tvScore.setText("Điểm: " + currentScore);
        tvAnswer.setTextColor(this.getResources().getColor(R.color.colorCorrect));
    }

    private void resetQuiz() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                processResetQuiz();
            }
        });
    }

    private void processResetQuiz() {
        //enable button record
        enableButton(btnRecord);

        //setup timer text
        tvTimer.setText("3s");
        tvTimer.setTextColor(this.getResources().getColor(R.color.colorPurple));
    }

    private void disableButton(FloatingActionButton btn) {
        btn.setEnabled(false);
        btn.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorDisableButton));
    }

    private void enableButton(FloatingActionButton btn) {
        btn.setEnabled(true);
        btn.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorEnableButton));
    }
}
