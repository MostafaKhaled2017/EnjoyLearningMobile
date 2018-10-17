package com.mk.playAndLearn.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mk.enjoylearning.R;

import java.util.List;

public class SubjectsSpinnerAdapter extends ArrayAdapter<String> {
    //TODO : use this adapter instead of using different list with add activities

    LayoutInflater flater;

    public SubjectsSpinnerAdapter(Activity context, int textviewId, String[] list){

        super(context,textviewId, list);
        flater = context.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String subject = getItem(position);

        View rowview = flater.inflate(R.layout.simple_spinner_dropdown_item, null, true);

        //TextView txtTitle = (TextView) rowview.findViewById(R.id.text1); TODO
        //txtTitle.setText(subject); TODO
        if (position == 0)
            rowview.setVisibility(View.GONE);//TODO : check this

        return rowview;
    }
}
