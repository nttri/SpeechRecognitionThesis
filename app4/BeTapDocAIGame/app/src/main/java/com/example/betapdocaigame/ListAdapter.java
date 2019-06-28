package com.example.betapdocaigame;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {
    public ArrayList<MyTopic> topics;
    private Context context;

    public ListAdapter(Context context,ArrayList<MyTopic> topics) {
        this.context = context;
        this.topics = topics;
    }

    @Override
    public int getCount() {
        return topics.size();
    }

    @Override
    public MyTopic getItem(int position) {
        return topics.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row;
        final ListViewHolder listViewHolder;

        if(convertView == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.activity_custom_listview,parent,false);
            listViewHolder = new ListViewHolder();
            listViewHolder.tvTopicName = row.findViewById(R.id.tvTopicName);
            listViewHolder.ivTopicImage = row.findViewById(R.id.ivTopicImage);
            listViewHolder.cellView = row.findViewById(R.id.cellView);
            row.setTag(listViewHolder);
        }
        else
        {
            row=convertView;
            listViewHolder= (ListViewHolder) row.getTag();
        }

        setupUI(listViewHolder, position);
        setupEvent(listViewHolder, position);

        return row;
    }

    private void setupUI(ListViewHolder listViewHolder, int position) {
        final MyTopic topic = getItem(position);
        listViewHolder.tvTopicName.setText(topic.getTitle());
        int resID = context.getResources().getIdentifier(topic.getImage() , "drawable", context.getPackageName());
        listViewHolder.ivTopicImage.setImageResource(resID);
    }

    private void setupEvent(ListViewHolder listViewHolder, final int position) {
        listViewHolder.tvTopicName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePlayTopic(position);
            }
        });

        listViewHolder.ivTopicImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePlayTopic(position);
            }
        });

        listViewHolder.cellView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePlayTopic(position);
            }
        });
    }

    private void handlePlayTopic(int position) {
        MyTopic topic = getItem(position);
        Intent intent = new Intent(context, QuizActivity.class);
        MyQuiz quiz = DataHelper.getInstance().getQuiz(topic.getTitle(), 0);

        if(quiz == null) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
            return;
        }

        intent.putExtra("TopicName", topic.getTitle());
        intent.putExtra("QuizText", quiz.getText());
        intent.putExtra("QuizImage", quiz.getImage());
        intent.putExtra("QuizNumber", 0);
        intent.putExtra("Score", 0);

        context.startActivity(intent);
    }
}
