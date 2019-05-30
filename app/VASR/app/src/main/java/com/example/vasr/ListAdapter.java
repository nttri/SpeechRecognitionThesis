package com.example.vasr;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ListAdapter extends BaseAdapter {

    public ArrayList<Audio> listAudios;
    private Context context;
    private MediaPlayer mediaPlayer;
    private ProgressDialog progressDialog;
    private String errorMsg;
    private Boolean isFailed = true;

    public ListAdapter(Context context,ArrayList<Audio> listAudios) {
        this.context = context;
        this.listAudios = listAudios;
        this.progressDialog = new ProgressDialog(context);
        setupDialog();
    }

    private void setupDialog() {
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (isFailed) {
                    Toast.makeText(context,errorMsg,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getCount() {
        return listAudios.size();
    }

    @Override
    public Audio getItem(int position) {
        return listAudios.get(position);
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
            listViewHolder.tvAudioName = row.findViewById(R.id.tvAudioName);
            listViewHolder.ibPlay = row.findViewById(R.id.ibPlay);
            listViewHolder.ibTranslate = row.findViewById(R.id.ibTranslate);
            row.setTag(listViewHolder);
        }
        else
        {
            row=convertView;
            listViewHolder= (ListViewHolder) row.getTag();
        }

        final Audio audio = getItem(position);
        listViewHolder.tvAudioName.setText(audio.AudioName);

        listViewHolder.ibTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                handleTranslateAudio(position);
            }
        });

        listViewHolder.ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePlayAudio(position);
            }
        });

        return row;
    }

    private void handlePlayAudio(int position) {
        //get file audio
        Audio audio = getItem(position);
        String wavPath = Environment.getExternalStorageDirectory() + File.separator + audio.AudioName;

        //play audio
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(wavPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            //handle when the audio is over
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                }
            });
            Toast.makeText(context, "Đang phát âm thanh", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleTranslateAudio(int position) {
        //get file audio
        Audio audio = getItem(position);
        String wavPath = Environment.getExternalStorageDirectory() + File.separator + audio.AudioName;

        //show progressing dialog
        progressDialog.setMessage("Âm thanh đang được xử lý, vui lòng chờ trong giây lát.");
        progressDialog.setCancelable(false);
        progressDialog.show();

        //send request to server to process
        postRequest(new File(wavPath));
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
                isFailed = true;
                errorMsg = e.getMessage();
                Log.w("Message failure",errorMsg);
                progressDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String mMessage = response.body().string();
                isFailed = false;
                progressDialog.dismiss();
                if (response.isSuccessful()){
                    try {
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
        Intent intent = new Intent(context,ResultActivity.class);
        intent.putExtra("TEXT", text);
        context.startActivity(intent);
    }
}
