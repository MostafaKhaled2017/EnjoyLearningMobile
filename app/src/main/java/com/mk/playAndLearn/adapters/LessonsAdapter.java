package com.mk.playAndLearn.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.LessonContentActivity;
import com.mk.playAndLearn.model.Lesson;

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
        final Lesson lesson = list.get(position);
        if (lesson.getTitle() != null)
            holder.arabicPosition.setText(lesson.getTitle());
        holder.lessonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent lessonIntent = new Intent(context, LessonContentActivity.class);
                lessonIntent.putExtra("lessonTitle", lesson.getTitle());
                lessonIntent.putExtra("lessonContent", lesson.getContent());
                ((Activity) context).startActivity(lessonIntent);
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
            arabicPosition = itemView.findViewById(R.id.lessonTitle);
            lessonButton = itemView.findViewById(R.id.lessonButton);
        }
    }

}
