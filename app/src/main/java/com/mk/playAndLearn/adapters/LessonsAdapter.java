package com.mk.playAndLearn.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.LessonContentActivity;
import com.mk.playAndLearn.fragment.LessonsFragment;
import com.mk.playAndLearn.fragment.LessonsFragment;
import com.mk.playAndLearn.model.Lesson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.fireStoreComments;
import static com.mk.playAndLearn.utils.Firebase.fireStoreLessons;
import static com.mk.playAndLearn.utils.Firebase.fireStoreReplies;
import static com.mk.playAndLearn.utils.Strings.adminEmail;

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

