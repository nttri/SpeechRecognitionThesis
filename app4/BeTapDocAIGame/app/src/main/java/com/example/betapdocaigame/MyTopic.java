package com.example.betapdocaigame;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class MyTopic {
    private String title = "";
    private String image = "";
    private ArrayList<MyQuestion> objects = new ArrayList<>();

    public MyTopic(String title, String image, JSONArray arrObj) {
        this.title = title;
        this.image = image;

        try {
            for (int i = 0; i < arrObj.length(); i++) {
                MyQuestion myQuestion = new MyQuestion(arrObj.getJSONObject(i));
                objects.add(myQuestion);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<MyQuestion> getObjects() {
        return objects;
    }

    public String getImage() {
        return image;
    }
}
