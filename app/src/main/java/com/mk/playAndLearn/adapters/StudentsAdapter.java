package com.mk.playAndLearn.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    int currentPosition = 0,  lastPoints  = (int)1e9, lastPosition = -1;


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
            //TODO
           /* if(position > lastPosition) {
                //Scroll Down
                if (user.getPoints() != lastPoints) {
                    currentPosition = position + 1;
                }
            }
            else {
                //scroll Up
                if(position != 0) {
                    for (int i = position; i > 0; i--) {
                        if(position != list.size()) {
                            Log.v("studentsAdapterLogging", "points is : " + list.get(i).getPoints()
                            + " , before points : " + list.get(i - 1).getPoints()
                            + " , after points : " + list.get(i + 1).getPoints());
                            if (list.get(i).getPoints() != list.get(i + 1).getPoints()
                                    && list.get(i).getPoints() != list.get(i - 1).getPoints()) {
                                currentPosition = i + 1;
                                Log.v("studentsAdapterLogging", "i : " + i +
                                        " , current position is : " +currentPosition);
                                break;
                            }
                        }
                        else {
                            //TODO
                            if (list.get(i).getPoints() != list.get(i - 1).getPoints()) {
                                currentPosition = i + 2;
                                break;
                            }
                        }
                    }
                }
                else {
                    position = currentPosition + 1;
                }
            }
            Log.v("studentsAdapter", "user points is : " + user.getPoints()
                    + " , last points is : " + lastPoints
                    + " , current position is : " + currentPosition);
            holder.position.setText(currentPosition + "");
            lastPosition = position;
            lastPoints = user.getPoints();*/
           if(user.getPosition() != -1) {
               holder.position.setText(user.getPosition() + "");
           }

        }
        holder.points.setText(user.getPoints() + " XP");
        if (user.getName() != null)
            holder.name.setText(user.getName());
        if (user.getImageUrl() != null)
            Picasso.with(context).load(user.getImageUrl()).placeholder(R.drawable.picasso_placeholder).into(holder.imageView);
        if(user.isAdmin()){
            holder.isAdminImageView.setVisibility(View.VISIBLE);
        }
        else {
            holder.isAdminImageView.setVisibility(View.GONE);
        }

        if(user.isOnline()){
            holder.isOnlineImageView.setVisibility(View.VISIBLE);
        }
        else {
            holder.isOnlineImageView.setVisibility(View.GONE);
        }
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
        ImageView imageView, isAdminImageView, isOnlineImageView;
        CardView cardView;

        public MyHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userNameInBest);
            points = itemView.findViewById(R.id.pointsInBest);
            position = itemView.findViewById(R.id.positionInBest);
            imageView = itemView.findViewById(R.id.userImageInBest);
            cardView = itemView.findViewById(R.id.card_view_of_student);
            isAdminImageView = itemView.findViewById(R.id.isAdminImageView);
            isOnlineImageView = itemView.findViewById(R.id.isOnlineImageView);
        }
    }


}
