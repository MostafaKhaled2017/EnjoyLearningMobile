package com.mk.playAndLearn.adapters;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.LessonContentActivity;
import com.mk.playAndLearn.fragment.LessonsFragment;
import com.mk.playAndLearn.model.Lesson;

import java.util.ArrayList;

public class LessonsAdapter extends RecyclerView.Adapter {

    ArrayList<Lesson> list;
    LessonsFragment lessonsFragment;
    Context context;
    String localCurrentUserUid;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;


    public LessonsAdapter(RecyclerView recyclerView, ArrayList<Lesson> list, Context context, LessonsFragment lessonsFragment, String localCurrentUserUid) {
        this.list = list;
        this.context = context;
        this.lessonsFragment = lessonsFragment;
        this.localCurrentUserUid = localCurrentUserUid;
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.lesson_title_item, parent, false);
            return new MyHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof MyHolder) {
            final Lesson lesson = list.get(position);
            if (lesson.getTitle() != null)
                ((MyHolder) holder).title.setText(lesson.getTitle());

            ((MyHolder) holder).title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, LessonContentActivity.class);
                    intent.putExtra("content", lesson.getContent());
                    intent.putExtra("title", lesson.getTitle());
                    if (lesson.getId() != null)
                        intent.putExtra("id", lesson.getId());
                    context.startActivity(intent);
                }
            });

        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView title;
        MyHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.lessonTitle);
        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
        }
    }
}

