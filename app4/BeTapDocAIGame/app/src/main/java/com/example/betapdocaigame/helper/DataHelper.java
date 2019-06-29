package com.example.betapdocaigame.helper;


import com.example.betapdocaigame.data.MyQuiz;
import com.example.betapdocaigame.data.MyTopic;

import java.util.ArrayList;

public class DataHelper {
    public ArrayList<MyTopic> topics = new ArrayList<>();
    private static DataHelper instance = null;

    private DataHelper() {}

    public static DataHelper getInstance() {
        if (instance == null) {
            instance = new DataHelper();
        }
        return instance;
    }

    public void setTopics(ArrayList<MyTopic> listTopics) {
        topics = listTopics;
    }

    public MyQuiz getQuiz(String topicName, int number) {
        for (MyTopic topic: topics) {
            if (topic.getTitle().equals(topicName)) {
                ArrayList<MyQuiz> quizes =  topic.getObjects();
                return quizes.get(number);
            }
        }
        return null;
    }
}
