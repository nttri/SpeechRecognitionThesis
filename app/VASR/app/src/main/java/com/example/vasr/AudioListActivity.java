package com.example.vasr;

import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class AudioListActivity extends AppCompatActivity {

    private ListView listView;
    private ListAdapter listAdapter;
    ArrayList<Audio> audios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_list);

        listView = findViewById(R.id.customListView);

        setupUI();
        setupData();
    }

    private void setupUI() {
        //set title on toolbar
        getSupportActionBar().setTitle("Danh sách bản ghi âm");

        //set activity background color
        getWindow().getDecorView().setBackgroundColor(Color.rgb(238,238,238));

        //add back button on toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupData() {
        //get data
        getAudio();
        //setup data on listview
        listAdapter = new ListAdapter(this,audios);
        listView.setAdapter(listAdapter);
    }

    public void getAudio() {
        //access storage
        String path = Environment.getExternalStorageDirectory().toString();
        File directory = new File(path);
        File[] files = directory.listFiles();

        //get all wav files
        for (int i = 0; i < files.length; i++)
        {
            String fileName = files[i].getName();
            if(fileName.endsWith(".wav") && fileName.startsWith("vars_")) {
                audios.add(new Audio(fileName));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //handle user click on button back
        if (id == android.R.id.home) {
            //close activity
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
