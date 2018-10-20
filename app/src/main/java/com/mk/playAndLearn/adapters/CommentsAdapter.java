package com.mk.playAndLearn.adapters;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Comment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.commentsReference;
import static com.mk.playAndLearn.utils.Firebase.postsReference;
import static com.mk.playAndLearn.utils.Strings.currentUserUid;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyHolder> {
    ArrayList<Comment> list;
    Context context;
    //used to load the votes number at the first time only
    boolean visied;
    long votes;

    public CommentsAdapter(ArrayList<Comment> list, Context context, boolean visited) {
        this.list = list;
        this.context = context;
        this.visied = visited;
    }

    @Override
    public CommentsAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        CommentsAdapter.MyHolder myHolder = new CommentsAdapter.MyHolder(view);

        return myHolder;
    }

    @Override
    public void onBindViewHolder(final CommentsAdapter.MyHolder holder, final int position) {
        final Comment comment = list.get(position);
        if (comment.getUserName() != null)
            holder.name.setText(comment.getUserName());
        if (comment.getContent() != null)
            holder.content.setText(comment.getContent());
        if (comment.getDate() != null) {
            if(comment.isPosted()) {
                holder.date.setText(comment.getDate());
            }
            else {
                holder.date.setText("جارى النشر ...");
            }
        }
        if (comment.getUserImage() != null)
            Picasso.with(context).load(comment.getUserImage()).placeholder(R.drawable.picasso_placeholder).into(holder.imageView);

            holder.votes.setText(comment.getVotes() + "");


        holder.upArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentsReference.orderByKey().equalTo(comment.getCommentId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.v("Log83", "up arrow onDataChanged");
                        validateVoting(dataSnapshot, comment, holder, "upArrow");
                        commentsReference.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        commentsReference.removeEventListener(this);
                    }
                });
            }
        });

        holder.downArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentsReference.orderByKey().equalTo(comment.getCommentId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.v("Log83", "down arrow onDataChanged");
                        validateVoting(dataSnapshot, comment, holder, "downArrow");
                        commentsReference.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        commentsReference.removeEventListener(this);
                    }
                });
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (comment.getWriterUid().equals(currentUserUid)) {
                    showActionsDialog(comment.getCommentId(), holder, comment.getContent(), position);
                }
                return true;
            }
        });
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
        TextView name, content, date, votes;
        ImageView imageView, upArrow, downArrow;
        CardView cardView;

        public MyHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.commentUserName);
            content = itemView.findViewById(R.id.commentContent);
            date = itemView.findViewById(R.id.commentDate);
            votes = itemView.findViewById(R.id.numberOfVotes);
            upArrow = itemView.findViewById(R.id.upArrow);
            downArrow = itemView.findViewById(R.id.downArrow);
            imageView = itemView.findViewById(R.id.commentImage);
            cardView = itemView.findViewById(R.id.card_view_of_comments);
        }
    }

    boolean isVoted(String[] upVotedUsersArray, String[] downVotedUsers) {
        for (String id : upVotedUsersArray) {
            if (id.equals(currentUserUid)) {
                return true;
            }
        }
        for (String id : downVotedUsers) {
            if (id.equals(currentUserUid)) {
                return true;
            }
        }
        return false;
    }

    void validateVoting(DataSnapshot dataSnapshot, final Comment comment, final CommentsAdapter.MyHolder holder, String tag) {
        String upVotedUsers = dataSnapshot.child(comment.getCommentId()).child("upVotedUsers").getValue().toString();
        String downVotedUsers = dataSnapshot.child(comment.getCommentId()).child("downVotedUsers").getValue().toString();
        votes = (long) dataSnapshot.child(comment.getCommentId()).child("votes").getValue();
        String[] upVotedUsersArray = upVotedUsers.split(" ");
        String[] downVotedUsersArray = downVotedUsers.split(" ");
        if (!isVoted(upVotedUsersArray, downVotedUsersArray)) {
            if (tag.equals("upArrow")) {
                votes++;
                commentsReference.child(comment.getCommentId()).child("upVotedUsers").setValue(upVotedUsers + currentUserUid + " ");
            } else if (tag.equals("downArrow")) {
                votes--;
                commentsReference.child(comment.getCommentId()).child("downVotedUsers").setValue(downVotedUsers + currentUserUid + " ");

            }
            commentsReference.child(comment.getCommentId()).child("votes").setValue(votes).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.v("Logging", "votes : " + votes);
                    holder.votes.setText(votes + "");
                    comment.setVotes(votes);
                }
            });
        } else {
            Toast.makeText(context, "لا يمكنك التصويت لنفس التعليق أكثر من مرة", Toast.LENGTH_SHORT).show();
        }
    }


    private void showActionsDialog(final String id, final MyHolder holder, final String content, final int position) {
        CharSequence colors[] = new CharSequence[]{"تعديل", "حذف"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("اختر من القائمة");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showDialog(id, holder, content, position);
                } else {
                    commentsReference.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(context, "تم حذف التعليق بنجاح", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        builder.show();
    }

    public void showDialog(final String id, final MyHolder holder, final String content, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        android.view.View view = layoutInflaterAndroid.inflate(R.layout.dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputTextEt = view.findViewById(R.id.dialog_value);
        inputTextEt.setText(content);
        final TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText("تعديل التعليق");

        alertDialogBuilderUserInput.setCancelable(true);
        alertDialogBuilderUserInput.setPositiveButton("تعديل", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String inputText = inputTextEt.getText().toString();
                commentsReference.child(id).child("content").setValue(inputText).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        list.get(position).setContent(inputText);
                        holder.content.setText(inputText);
                        Toast.makeText(context, "تم تعديل التعليق بنجاح", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        alertDialogBuilderUserInput.show();
    }
}
