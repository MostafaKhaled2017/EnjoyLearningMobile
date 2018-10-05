package com.mk.playAndLearn.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Comment;
import com.mk.playAndLearn.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyHolder> {
    ArrayList<Comment> list;
    Context context;

    public CommentsAdapter(ArrayList<Comment> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public CommentsAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        CommentsAdapter.MyHolder myHolder = new CommentsAdapter.MyHolder(view);

        return myHolder;
    }

    @Override
    public void onBindViewHolder(CommentsAdapter.MyHolder holder, int position) {
        final Comment comment = list.get(position);
        if (comment.getUserName() != null)
            holder.name.setText(comment.getUserName());
        if (comment.getContent() != null)
            holder.content.setText(comment.getContent());
        if (comment.getDate() != null)
            holder.date.setText(comment.getDate());
        if (comment.getUserImage() != null)
            Picasso.with(context).load(comment.getUserImage()).into(holder.imageView);
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
        TextView name, content, date;
        ImageView imageView;

        public MyHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.commentUserName);
            content = itemView.findViewById(R.id.commentContent);
            date = itemView.findViewById(R.id.commentDate);
            imageView = itemView.findViewById(R.id.commentImage);
        }
    }


}
