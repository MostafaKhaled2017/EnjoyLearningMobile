package com.mk.playAndLearn.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.ChallengeStartActivity;
import com.mk.playAndLearn.activity.QuestionActivity;
import com.mk.playAndLearn.model.Challenge;
import com.mk.playAndLearn.model.Challenge;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;
import static com.mk.playAndLearn.utils.Strings.waitingChallengeText;
import static com.mk.playAndLearn.utils.Strings.yourTurnChallengeText;

public class ChallengesAdapter extends RecyclerView.Adapter<ChallengesAdapter.MyHolder> {
    ArrayList<Challenge> list;
    Context context;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("challenges");
    String secondChallengerName, secondChallengerImage;
    long secondChallengerPoints;

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
        final Challenge challenge = list.get(position);
        if (challenge.getChallengerName() != null)
            holder.challengerName.setText(challenge.getChallengerName());
        //TODO : think about changing the text below
        if (challenge.getState().equals("لم يكتمل")) {
            String stateText;
            if (challenge.getCurrentPlayer() == 1)
                stateText = waitingChallengeText;
            else
                stateText = yourTurnChallengeText;

            holder.state.setText(stateText);
        }
        if (challenge.getDate() != null)
            holder.date.setText(challenge.getDate());
        if (challenge.getSubject() != null)
            holder.subject.setText(challenge.getSubject());
        if (challenge.getScore() != null)
            holder.score.setText(challenge.getScore());
        if (challenge.getImage() != null)
            Picasso.with(context).load(challenge.getImage()).into(holder.imageView);

        if (challenge.getState().equals("لم يكتمل") && challenge.getCurrentPlayer() == 2) {
            //TODO : ensure that the button can't be pressed before loading
            DatabaseReference usersReference = database.getReference("users");
            usersReference.child(challenge.getSecondChallengerUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    secondChallengerName = dataSnapshot.child("userName").getValue().toString();
                    secondChallengerImage = dataSnapshot.child("userImage").getValue().toString();
                    secondChallengerPoints = (long) dataSnapshot.child("points").getValue();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            //TODO : edit this if needed
            holder.challengeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO : adjust this dialog content
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("تحدى جديد");
                    dialog.setMessage("هل تريد قبول هذا التحدى؟");
                    dialog.setNegativeButton("قبول", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //TODO : think about editing the intent to go to challenge startActivity
                            Intent intent = new Intent(context, ChallengeStartActivity.class);
                            intent.putExtra("challengeId", challenge.getId());
                            intent.putExtra("currentChallenger", 2);
                            intent.putExtra("secondChallengerName", secondChallengerName);
                            intent.putExtra("secondChallengerImage", secondChallengerImage);
                            intent.putExtra("secondChallengerPoints", (int) secondChallengerPoints);
                            intent.putParcelableArrayListExtra("questionsList", challenge.getQuestionsList());
                            context.startActivity(intent);
                        }
                    });
                    dialog.setPositiveButton("رفض", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //TODO : make the other user knows that challenge has refused
                            ref.child(challenge.getId()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    ref.child(challenge.getId()).child("player2score").setValue(0);
                                    ref.child(challenge.getId()).child("state").setValue(refusedChallengeText);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    });

                    dialog.create();
                    dialog.show();
                }
            });
        }
        else {
            holder.state.setText(challenge.getState());
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
        TextView challengerName, state, date, subject, score;
        ImageView imageView;
        RelativeLayout challengeView;

        public MyHolder(View itemView) {
            super(itemView);
            challengerName = itemView.findViewById(R.id.challengerName);
            state = itemView.findViewById(R.id.challengeState);
            date = itemView.findViewById(R.id.challengeDate);
            subject = itemView.findViewById(R.id.challengeSubject);
            score = itemView.findViewById(R.id.challengeScore);
            imageView = itemView.findViewById(R.id.challengeImage);
            challengeView = itemView.findViewById(R.id.challengeView);
        }
    }
}
