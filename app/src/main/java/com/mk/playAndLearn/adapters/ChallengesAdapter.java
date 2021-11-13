package com.mk.playAndLearn.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.ChallengeStartActivity;
import com.mk.playAndLearn.model.Challenge;
import com.mk.playAndLearn.utils.DateClass;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.fireStoreChallenges;
import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;
import static com.mk.playAndLearn.utils.Integers.dailyChallengesNumber;
import static com.mk.playAndLearn.utils.Strings.completedChallengeText;
import static com.mk.playAndLearn.utils.Strings.drawChallengeText;
import static com.mk.playAndLearn.utils.Strings.loseChallengeText;
import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;
import static com.mk.playAndLearn.utils.Strings.uncompletedChallengeText;
import static com.mk.playAndLearn.utils.Strings.waitingChallengeText;
import static com.mk.playAndLearn.utils.Strings.wonChallengeText;
import static com.mk.playAndLearn.utils.Strings.yourTurnChallengeText;

public class ChallengesAdapter extends RecyclerView.Adapter<ChallengesAdapter.MyHolder> {
    ArrayList<Challenge> list;
    Context context;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
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
        if (challenge.getOpponentName() != null)
            holder.challengerName.setText(challenge.getOpponentName().trim());
        //TODO : think about changing the text below
        if (challenge.getState().equals(uncompletedChallengeText)) {
            String stateText;
            if (challenge.getCurrentPlayer() == 1)
                stateText = waitingChallengeText;
            else
                stateText = yourTurnChallengeText;
            Log.v("challengesAdapter", "state text is : " + stateText + " current player : " + challenge.getCurrentPlayer());
            holder.state.setText(stateText);
        }

        if (challenge.getState().equals(completedChallengeText)) {
            if (challenge.getPlayer1Score() == challenge.getPlayer2Score()) {
                holder.state.setText(drawChallengeText);
            } else {
                if (challenge.getCurrentPlayer() == 1) {
                    if (challenge.getPlayer1Score() > challenge.getPlayer2Score()) {
                        holder.state.setText(wonChallengeText);
                        holder.state.setBackgroundColor(context.getResources().getColor(R.color.green));
                    } else {
                        holder.state.setText(loseChallengeText);
                        holder.state.setBackgroundColor(context.getResources().getColor(R.color.red));
                    }
                } else if (challenge.getCurrentPlayer() == 2) {
                    if (challenge.getPlayer2Score() > challenge.getPlayer1Score()) {
                        holder.state.setText(wonChallengeText);
                        holder.state.setBackgroundColor(context.getResources().getColor(R.color.green));
                    } else {
                        holder.state.setText(loseChallengeText);
                        holder.state.setBackgroundColor(context.getResources().getColor(R.color.red));
                    }
                }
            }
        }

        if (challenge.getState().equals(refusedChallengeText)) {
            holder.state.setText(refusedChallengeText);
        }

        if (challenge.getDate() != null)
            holder.date.setText(challenge.getDate());
        if (challenge.getSubject() != null)
            holder.subject.setText(challenge.getSubject());
        if (challenge.getScore() != null) {
            if (challenge.getState().equals(completedChallengeText) || challenge.getState().equals(refusedChallengeText)) {
                holder.score.setText(challenge.getScore());
            } else {
                holder.score.setText("");//TODO : think about adding space with the size of real scores
            }
        }
        if (challenge.getImage() != null)
            Picasso.with(context).load(challenge.getImage()).placeholder(R.drawable.picasso_placeholder).into(holder.imageView);
        //TODO : add condition if the challenge completed shows the challenge result and right and wrong answers booleans .. by sending the id of challenge to the new activity and get the new data there
        if (challenge.getState().equals(uncompletedChallengeText) && challenge.getCurrentPlayer() == 2) {
            //TODO : edit this if needed
            holder.challengeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO : adjust this dialog content
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                   // dialog.setTitle("تحدى جديد");
                    dialog.setMessage("هل تريد قبول هذا التحدى؟");
                    dialog.setNegativeButton("قبول", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //TODO : think about editing the intent to go to challenge startActivity
                            Intent intent = new Intent(context, ChallengeStartActivity.class);
                            intent.putExtra("challengeId", challenge.getId());
                            intent.putExtra("currentChallenger", 2);
                            intent.putExtra("uid", challenge.getOpponentUid());//second means that he isn't the current user
                            intent.putExtra("questionsList", challenge.getQuestionsList());
                            intent.putExtra("grade", challenge.getGrade());
                            intent.putExtra("subject", challenge.getSubject());
                            context.startActivity(intent);
                        }
                    });
                    dialog.setPositiveButton("رفض", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            fireStoreChallenges.document(challenge.getId()).update("player2score", 0);
                            fireStoreChallenges.document(challenge.getId()).update("state", refusedChallengeText);

                            //TODO : check that this date is correct
                            Date today = new Date();
                            DateClass dateClass = new DateClass();
                            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH);
                            format.setTimeZone(TimeZone.getTimeZone("GMT+2"));

                            dateClass.setDate(today);

                            //  usersReference.child(challenge.getPlayer2Uid()).child("lastChallengeDate").setValue(dateClass.getDate());
                        }
                    });


                    dialog.create();

                    dialog.show();
                }
            });
        }
        if (challenge.getState().equals(completedChallengeText)) {
            holder.challengeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //TODO : adjust this dialog content
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("تحدى جديد");
                    dialog.setMessage("هل تريد إعادة تحدى هذا الطالب؟");
                    dialog.setNegativeButton("نعم", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final Intent intent = new Intent(context, ChallengeStartActivity.class);

                            intent.putExtra("uid", challenge.getOpponentUid());
                            intent.putExtra("subject", challenge.getSubject());

                            /*if (dailyChallengesNumber - getSavedTodayChallengesNo(context) < 1) {
                                Toast.makeText(context, "لقد أنهيت عدد التحديات المسموح لك اليوم يمكنك العودة غدا للعب تحديات أخرى أو طلب من أحد أصدقائك بدء تحدى جديد ضدك", Toast.LENGTH_LONG).show();
                            } else {*/
                                fireStoreUsers.document(challenge.getOpponentUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        DocumentSnapshot document = task.getResult();
                                        if (task.isSuccessful()) {
                                            String secondPlayerPoints = "-1";

                                            String secondPlayerName = (String) document.getString("userName");
                                            String secondPlayerImage = (String) document.getString("userImage");
                                            String secondPlayerEmail = (String) document.getString("userEmail");
                                            if (document.getLong("points") != null)
                                                secondPlayerPoints = document.getLong("points").toString();

                                            intent.putExtra("name", secondPlayerName);
                                            intent.putExtra("image", secondPlayerImage);
                                            intent.putExtra("points", Integer.parseInt(secondPlayerPoints));
                                            intent.putExtra("email", secondPlayerEmail);
                                            context.startActivity(intent);
                                        }
                                    }
                                });

                            }

                    });
                    dialog.setPositiveButton("لا", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });


                    dialog.create();

                    dialog.show();
                }
            });
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

        MyHolder(View itemView) {
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