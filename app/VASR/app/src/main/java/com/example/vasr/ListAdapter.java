package com.example.vasr;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter{

    public ArrayList<Audio> listAudios;
    private Context context;

    public ListAdapter(Context context,ArrayList<Audio> listAudios) {
        this.context = context;
        this.listAudios = listAudios;
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
//        listViewHolder.ibPlay.setImageResource(audio.ProductImage);
        listViewHolder.ibTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                System.out.println("btnTranslate");
            }
        });

        listViewHolder.ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("btnPlay");
            }
        });

        return row;
    }
}
