package com.mk.playAndLearn.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.ChallengeStartActivity;
import com.mk.playAndLearn.activity.ChallengersActivity;
import com.mk.playAndLearn.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class StudentsAdapter extends RecyclerView.Adapter<StudentsAdapter.MyHolder> {
    ArrayList<User> list;
    Context context;

    private final String TAG, subject;
    int currentPosition = 0, lastPoints = (int)1e9;

    public StudentsAdapter(ArrayList<User> list, Context context, String TAG, String subject) {
        this.list = list;
        this.context = context;
        this.TAG = TAG;
        this.subject = subject;
    }

    @Override
    public StudentsAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.student_item, parent, false);
        StudentsAdapter.MyHolder myHolder = new StudentsAdapter.MyHolder(view);

        return myHolder;
    }

    @Override
    public void onBindViewHolder(StudentsAdapter.MyHolder holder, int position) {
        final User user = list.get(position);
        if (TAG.equals("ChallengersActivity")) {
            holder.position.setVisibility(View.GONE);
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ChallengeStartActivity.class);
                    intent.putExtra("name", user.getName());
                    intent.putExtra("image", user.getImageUrl());
                    intent.putExtra("points", user.getPoints());
                    intent.putExtra("email", user.getEmail());
                    intent.putExtra("uid", user.getUid());
                    intent.putExtra("subject", subject);
                    context.startActivity(intent);
                    ((ChallengersActivity)context).finish();
                }
            });
        }
        else {
            if(user.getPoints() < lastPoints){
                currentPosition ++;
            }
            holder.position.setText(currentPosition + "");
            lastPoints = user.getPoints();
        }
        holder.points.setText(user.getPoints() + " XP");
        if (user.getName() != null)
            holder.name.setText(user.getName());
        if (user.getImageUrl() != null)
            Picasso.with(context).load(user.getImageUrl()).into(holder.imageView);
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
        TextView name, points, position;
        ImageView imageView;
        CardView cardView;

        public MyHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userNameInBest);
            points = itemView.findViewById(R.id.pointsInBest);
            position = itemView.findViewById(R.id.positionInBest);
            imageView = itemView.findViewById(R.id.userImageInBest);
            cardView = itemView.findViewById(R.id.card_view_of_student);
        }
    }


}