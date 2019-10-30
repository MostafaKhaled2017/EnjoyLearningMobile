package com.mk.playAndLearn.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.ChallengeStartActivity;
import com.mk.playAndLearn.activity.ChallengersActivity;
import com.mk.playAndLearn.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;

public class StudentsAdapter extends RecyclerView.Adapter<StudentsAdapter.MyHolder> {
    ArrayList<User> list;
    Context context;

    private final String TAG, subject, unit, lesson;
    long term = -2;
    String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public StudentsAdapter(ArrayList<User> list, Context context, String TAG, String subject, String unit, String lesson, long term) {
        this.list = list;
        this.context = context;
        this.TAG = TAG;
        this.subject = subject;
        this.unit = unit;
        this.lesson = lesson;
        this.term = term;

        Log.v("termLogging", "term in adapter constructor is : " + term
        + " , unit is : " + unit);
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
        if (TAG.equals("LastChallengers")|| TAG.equals("FriendsFragment")) {
            holder.position.setVisibility(View.GONE);
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v("termLogging", "term in adapter is : " + term
                    + " , unit is : " + unit);
                    Intent intent = new Intent(context, ChallengeStartActivity.class);
                    intent.putExtra("name", user.getName());
                    intent.putExtra("image", user.getImageUrl());
                    intent.putExtra("points", user.getPoints());
                    intent.putExtra("email", user.getEmail());
                    intent.putExtra("uid", user.getUid());
                    intent.putExtra("subject", subject);
                    intent.putExtra("unit", unit);
                    intent.putExtra("lesson", lesson);
                    intent.putExtra("term", term);
                    context.startActivity(intent);
                    ((ChallengersActivity) context).finish();
                }
            });
        } else if (TAG.equals("LessonsFragment") || TAG.equals("SearchActivity")) {
            holder.position.setVisibility(View.GONE);
        } else {
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
            if (user.getPosition() != -1) {
                holder.position.setText(user.getPosition() + "");
            }

        }
        if (TAG.equals("BestQuestionsUploadersActivity")) {
            holder.points.setText("عدد الأسئلة : " + user.getAcceptedQuestions());
        } else {
            holder.points.setText(user.getPoints() + " XP");
        }
        if (user.getName() != null)
            holder.name.setText(user.getName().trim());
        if (user.getImageUrl() != null)
            Picasso.with(context).load(user.getImageUrl()).placeholder(R.drawable.picasso_placeholder).into(holder.imageView);

        if (user.isAdmin()) {
            holder.isAdminImageView.setVisibility(View.VISIBLE);
        } else {
            holder.isAdminImageView.setVisibility(View.GONE);
        }

        if (currentUserUid.equals(user.getUid())) {
            holder.cardView.setBackgroundResource(R.color.lightColor);
        } else {
            holder.cardView.setBackgroundResource(R.color.white);
        }

        if(TAG.equals("SearchActivity")){
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showConfirmationDialog(user);
                }
            });
        }

     /*   if(user.isOnline()){
            holder.isOnlineImageView.setVisibility(View.VISIBLE);
        }
        else {
            holder.isOnlineImageView.setVisibility(View.GONE);
        }*/
    }

    public void showConfirmationDialog(final User user){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("إضافة صديق");
        dialog.setMessage("هل تريد إضافة هذا الطالب إلى أصدقائك");
        dialog.setPositiveButton("لا", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.setNegativeButton("نعم", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(context, "جارى إضافة الطالب", Toast.LENGTH_SHORT).show();
                fireStoreUsers.document(currentUserUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            String friendsList = documentSnapshot.getString("friends");
                            String friendUid = user.getUid();

                            if(!isFriend(friendUid, friendsList)){
                                fireStoreUsers.document(currentUserUid).update("friends", friendsList + friendUid + " ").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "تم إضافة الطالب بنجاح", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(context, "هذا الطالب من أصدقائك بالفعل", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(context, "حدثت مشكلة أثناء محاولة الإضافة برجاء المحاولة لاحقا", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        dialog.create();
        dialog.show();
    }

    private boolean isFriend(String friendUid, String friendsList) {
        String[] friendsListArray = friendsList.split(" ");
        for (String id : friendsListArray) {
            if (id.equals(friendUid)) {
                return true;
            }
        }
        return false;
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

        MyHolder(View itemView) {
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
