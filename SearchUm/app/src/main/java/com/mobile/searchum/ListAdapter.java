package com.mobile.searchum;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class ListAdapter extends ArrayAdapter<String> {
    private String TAG = ListAdapter.class.getSimpleName();

    int viewGroup;
    String[] score_list;
    Context context;

    public ListAdapter(Context context, int viewGroup, int id, String[] score_list) {
        super(context, viewGroup, id, score_list);
        this.context = context;
        this.score_list = score_list;
        this.viewGroup = viewGroup;


    }

    static class ViewHolder{
        public TextView usernameTextView, scoreTextView;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(viewGroup, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.usernameTextView =(TextView)rowView.findViewById(R.id.usernameTextView);
            holder.scoreTextView =(TextView)rowView.findViewById(R.id.scoreTextView);
            rowView.setTag(holder);
        }
        String[] scores = score_list[position].split("=");
        ViewHolder holder = (ViewHolder)rowView.getTag();
        Log.d("TAG", "In List: " + scores[0] + " " + scores[1]);
        holder.usernameTextView.setText(scores[0]);
        holder.scoreTextView.setText(scores[1]);
        return rowView;
    }

}
