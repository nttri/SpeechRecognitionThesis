package com.example.betapdocaigame;


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

    public void setTopics(ArrayList<MyTopic> topics) {
        topics = topics;
    }

    public MyQuiz getQuiz(String topicName, int number) {
        for (MyTopic topic: topics) {
            if (topic.getTitle() == topicName) {
                ArrayList<MyQuiz> quizes =  topic.getObjects();
                return quizes.get(number);
            }
        }
        return null;
    }
}
