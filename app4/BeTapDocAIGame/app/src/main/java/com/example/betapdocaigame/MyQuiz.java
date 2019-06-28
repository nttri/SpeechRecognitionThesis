package com.example.betapdocaigame;

import org.json.JSONException;
import org.json.JSONObject;


public class MyQuiz {
    private String text = "";
    private String image = "";

    public MyQuiz(JSONObject obj) {
        try {
            this.text = obj.getString("text");
            this.image = obj.getString("image");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getText() {
        return text;
    }

    public String getImage() {
        return image;
    }
}
