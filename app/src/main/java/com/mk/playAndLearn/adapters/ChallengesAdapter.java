package com.mk.playAndLearn.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Challenge;
import com.mk.playAndLearn.model.Challenge;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChallengesAdapter extends RecyclerView.Adapter<ChallengesAdapter.MyHolder> {
    ArrayList<Challenge> list;
    Context context;

    public ChallengesAdapter(ArrayList<Challenge> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public ChallengesAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.challenge_item, parent, false);
        ChallengesAdapter.MyHolder myHolder = new ChallengesAdapter.MyHolder(view);

        return myHolder;
    }

    @Override
    public void onBindViewHolder(ChallengesAdapter.MyHolder holder, int position) {
        final Challenge mylist = list.get(position);
        if (mylist.getChallengerName() != null)
            holder.challengerName.setText(mylist.getChallengerName());
        if (mylist.getState() != null)
            holder.state.setText(mylist.getState());
        if (mylist.getDate() != null)
            holder.date.setText(mylist.getDate());
        if (mylist.getSubject() != null)
            holder.subject.setText(mylist.getSubject());
        if (mylist.getScore() != -1)
            holder.score.setText(mylist.getScore() + "");
        if (mylist.getImage() != null)
            Picasso.with(context).load(mylist.getImage()).into(holder.imageView);
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
        TextView challengerName, state, date, subject, score;
        ImageView imageView;

        public MyHolder(View itemView) {
            super(itemView);
            challengerName = itemView.findViewById(R.id.challengerName);
            state = itemView.findViewById(R.id.challengeState);
            date = itemView.findViewById(R.id.challengeDate);
            subject = itemView.findViewById(R.id.challengeSubject);
            score = itemView.findViewById(R.id.challengeScore);
            imageView = itemView.findViewById(R.id.challengeImage);
        }
    }
}
