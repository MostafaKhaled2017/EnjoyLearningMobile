package com.mk.playAndLearn.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class BestStudentsAdapter extends RecyclerView.Adapter<BestStudentsAdapter.MyHolder> {
    ArrayList<User> list;
    Context context;

    public BestStudentsAdapter(ArrayList<User> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public BestStudentsAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.best_students_item, parent, false);
        BestStudentsAdapter.MyHolder myHolder = new BestStudentsAdapter.MyHolder(view);

        return myHolder;
    }

    @Override
    public void onBindViewHolder(BestStudentsAdapter.MyHolder holder, int position) {
        final User mylist = list.get(position);
        holder.position.setText(position + 1 + "");//TODO : find a better  way to get positions with tie
        holder.points.setText( mylist.getPoints() + " XP");
        if (mylist.getName() != null)
            holder.name.setText(mylist.getName());
        if (mylist.getImageUrl() != null)
            Picasso.with(context).load(mylist.getImageUrl()).into(holder.imageView);
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

        public MyHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userNameInBest);
            points = itemView.findViewById(R.id.pointsInBest);
            position = itemView.findViewById(R.id.positionInBest);
            imageView = itemView.findViewById(R.id.userImageInBest);
        }
    }


}
