package com.mk.playAndLearn.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.fragment.LessonContent;
import com.mk.playAndLearn.model.Lesson;
import com.mk.playAndLearn.model.Post;

import java.util.ArrayList;

public class LessonsAdapter extends RecyclerView.Adapter<LessonsAdapter.MyHolder> {

    ArrayList<Lesson> list;
    Context context;

    public LessonsAdapter(ArrayList<Lesson> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.lesson_title_item, parent, false);
        MyHolder myHolder = new MyHolder(view);

        return myHolder;
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        final Lesson mylist = list.get(position);
        if (mylist.getArabicPosition() != null)
            holder.arabicPosition.setText(mylist.getArabicPosition());
        holder.lessonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
                Bundle lessonData = new Bundle();
                LessonContent lessonContent = new LessonContent();
                lessonData.putString("lessonTitle", mylist.getTitle());
                lessonData.putString("lessonContent", mylist.getContent());
                lessonContent.setArguments(lessonData);
                ((Activity) context).getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, lessonContent)
                        .addToBackStack(null)
                        .commit();
            }
        });

    }

    @Override
    public int getItemCount() {

        int arr = 0;

        try {
            if (list.size() == 0) {
                arr = 0;
            } else {
                arr = list.size();
            }
        } catch (Exception e) {
        }

        return arr;

    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView arabicPosition;
        Button lessonButton;

        public MyHolder(View itemView) {
            super(itemView);
            arabicPosition = itemView.findViewById(R.id.lessonArabicPosition);
            lessonButton = itemView.findViewById(R.id.lessonButton);
        }
    }

}
