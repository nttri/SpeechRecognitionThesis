package com.example.betapdocaigame;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ListAdapter listAdapter;
    private ArrayList<MyTopic> topics = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.customListView);

        loadData();
    }

    private void setupUI() {
        //set title on toolbar
        getSupportActionBar().setTitle("Trang chủ");

        //set activity background color
        getWindow().getDecorView().setBackgroundColor(Color.rgb(238,238,238));
    }

    private void loadData() {
        //load data from json
        loadTopicsData();

        //setup data on listview
        listAdapter = new ListAdapter(this, topics);
        listView.setAdapter(listAdapter);
    }

    private void loadTopicsData() {
        try {
            JSONObject objects = new JSONObject(loadJSONFromAsset());
            JSONArray arrObj = objects.getJSONArray("objects");
            for (int i = 0; i < arrObj.length(); i++) {
                JSONObject obj = arrObj.getJSONObject(i);
                String title = obj.getString("type");
                String image = obj.getString("image");
                JSONArray listQuestions = obj.getJSONArray("list");
                MyTopic topic = new MyTopic(title, image, listQuestions);
                topics.add(topic);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
