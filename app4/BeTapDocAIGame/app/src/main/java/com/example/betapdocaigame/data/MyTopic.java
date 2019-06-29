package com.example.betapdocaigame.data;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class MyTopic {
    private String title = "";
    private String image = "";
    private ArrayList<MyQuiz> objects = new ArrayList<>();

    public MyTopic(String title, String image, JSONArray arrObj) {
        this.title = title;
        this.image = image;

        try {
            for (int i = 0; i < arrObj.length(); i++) {
                MyQuiz myQuiz = new MyQuiz(arrObj.getJSONObject(i));
                objects.add(myQuiz);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<MyQuiz> getObjects() {
        return objects;
    }

    public String getImage() {
        return image;
    }
}
