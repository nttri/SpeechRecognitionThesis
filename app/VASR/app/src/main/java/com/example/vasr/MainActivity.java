package com.example.vasr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Button btnRecord, btnStop, btnPlay, btnTest;
    private MediaPlayer mediaPlayer;
    private String pcmPathSave = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.pcm";
    private String wavPathSave = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.wav";
    private final int REQUEST_PERMISSION_CODE = 1000;

    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;

    int BufferElements2Rec = 1024;
    int BytesPerElement = 2; // 2 bytes = 16 bits

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("VASR");

        btnTest   = findViewById(R.id.btn_test);
        btnRecord = findViewById(R.id.btn_record);
        btnStop   = findViewById(R.id.btn_stop);
        btnPlay   = findViewById(R.id.btn_play);

        if(!checkPermissionOnDevice()) {
            requestPermission();
        }

        setupButtonHandler();
        changeButtonsStatus(true, false, false, false);
    }

    private void changeButtonsStatus(boolean s1, boolean s2, boolean s3, boolean s4) {
        btnRecord.setEnabled(s1);
        btnStop.setEnabled(s2);
        btnPlay.setEnabled(s3);
        btnTest.setEnabled(s4);
    }

    private void setupButtonHandler() {
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermissionOnDevice()) {
                    changeButtonsStatus(false, true, false, false);
                    startRecording();
                    Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
                } else {
                    requestPermission();
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeButtonsStatus(true, false, true, true);
                stopRecording();
                try {
                    AudioConverter.PCMToWAV(new File(pcmPathSave), new File(wavPathSave), 1, RECORDER_SAMPLERATE, 16);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Audio Recorder successfully", Toast.LENGTH_LONG).show();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeButtonsStatus(false, false, false, true);
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(wavPathSave);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            changeButtonsStatus(true, false, true, true);
                        }
                    });
                    Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeButtonsStatus(false, false, false, false);
                postRequest(new File(wavPathSave));
            }
        });
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
    }

    private boolean checkPermissionOnDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);
        int read_external_storage_result = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED
                && record_audio_result == PackageManager.PERMISSION_GRANTED
                && read_external_storage_result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        },REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Permission granted",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void postRequest(File file) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://vnstt001.herokuapp.com/file";

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
                String errMsg = e.getMessage();
                Log.w("Message failure",errMsg);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeButtonsStatus(true, false, true, true);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String mMessage = response.body().string();
                if (response.isSuccessful()){
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                changeButtonsStatus(true, false, true, true);
                            }
                        });
                        JSONObject json = new JSONObject(mMessage);
                        String text = json.getString("text");
                        showResultScreen(text);
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private void showResultScreen(String text) {
        Intent intent = new Intent(this,ResultActivity.class);
        intent.putExtra("TEXT", text);
        startActivity(intent);
    }

    private void showAudioListScreen() {
        Intent intent = new Intent(this,AudioListActivity.class);
        startActivity(intent);
    }
}
