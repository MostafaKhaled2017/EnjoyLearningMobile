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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.RepliesActivity;
import com.mk.playAndLearn.model.Comment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.fireStoreComments;
import static com.mk.playAndLearn.utils.Firebase.fireStoreReplies;
import static com.mk.playAndLearn.utils.Strings.adminEmail;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyHolder> {
    ArrayList<Comment> list;
    Context context;
    //used to load the votes number at the first time only
    boolean visited;
    long votes;
    String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public CommentsAdapter(ArrayList<Comment> list, Context context, boolean visited) {
        this.list = list;
        this.context = context;
        this.visited = visited;
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
            holder.name.setText(comment.getUserName().trim());
        if (comment.getContent() != null)
            holder.content.setText(comment.getContent());
        if (comment.getDate() != null) {
            if (comment.isPosted() || !comment.getWriterUid().equals(localCurrentUserUid)) {
                holder.date.setText(comment.getDate());
            } else {
                holder.date.setText("جارى النشر ...");
            }
        }
        if (comment.getUserImage() != null)
            Picasso.with(context).load(comment.getUserImage()).placeholder(R.drawable.picasso_placeholder).into(holder.imageView);


        if (comment.getUpVotedUsers() != null) {
            ((MyHolder) holder).upVotesNo.setText(comment.getUpVotedUsers().split(" ").length - 1 + "");
        }
        if (comment.getDownVotedUsers() != null) {
            ((MyHolder) holder).downVotesNo.setText(comment.getDownVotedUsers().split(" ").length - 1 + "");
        }

        ((MyHolder) holder).replies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RepliesActivity.class);
                intent.putExtra("commentWriterUid", comment.getWriterUid());
                if (comment.getId() != null)
                    intent.putExtra("id", comment.getId());
                context.startActivity(intent);
            }
        });

        holder.upArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireStoreComments.document(comment.getCommentId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        validateVoting(documentSnapshot, comment, holder, "upArrow");
                    }
                });
            }
        });

        holder.downArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireStoreComments.document(comment.getCommentId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.v("Log83", "down arrow onDataChanged");
                        validateVoting(documentSnapshot, comment, holder, "downArrow");
                    }
                });
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String localCurrentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                //TODO : change this hardcoded way
                if (comment.getWriterUid().equals(localCurrentUserUid) || localCurrentUserEmail.equals(adminEmail)) {
                    boolean admin = false;
                    if (localCurrentUserEmail.equals(adminEmail))
                        admin = true;

                    showActionsDialog(comment.getCommentId(), holder, comment.getContent(), comment.getUserEmail(), admin, position); //TODO :  search why I need to add one
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
        TextView name, content, date, votes, replies, upVotesNo, downVotesNo;
        ImageView imageView, upArrow, downArrow;
        CardView cardView;

        MyHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.replyUserName);
            content = itemView.findViewById(R.id.replyContent);
            date = itemView.findViewById(R.id.replyDate);
            upVotesNo = itemView.findViewById(R.id.upVotesNo);
            downVotesNo = itemView.findViewById(R.id.downVotesNo);
            upArrow = itemView.findViewById(R.id.like);
            downArrow = itemView.findViewById(R.id.downVote);
            imageView = itemView.findViewById(R.id.replyImage);
            cardView = itemView.findViewById(R.id.card_view_of_comments);
            replies = itemView.findViewById(R.id.repliesTv);
        }
    }

    boolean isVoted(String[] upVotedUsersArray, String[] downVotedUsers) {
        for (String id : upVotedUsersArray) {
            if (id.equals(localCurrentUserUid)) {
                return true;
            }
        }
        for (String id : downVotedUsers) {
            if (id.equals(localCurrentUserUid)) {
                return true;
            }
        }
        return false;
    }

    void validateVoting(DocumentSnapshot dataSnapshot, final Comment comment, final CommentsAdapter.MyHolder holder, String tag) {
        String upVotedUsers = dataSnapshot.getString("upVotedUsers");
        String downVotedUsers = dataSnapshot.getString("downVotedUsers");
        votes = dataSnapshot.getLong("votes");
        String[] upVotedUsersArray = upVotedUsers.split(" ");
        String[] downVotedUsersArray = downVotedUsers.split(" ");
        if (!isVoted(upVotedUsersArray, downVotedUsersArray)) {
            if (tag.equals("upArrow")) {
                final String newUpVotedUsers = upVotedUsers + localCurrentUserUid + " ";

                votes++;
                fireStoreComments.document(comment.getId()).update("upVotedUsers", upVotedUsers + localCurrentUserUid + " ");

                fireStoreComments.document(comment.getId()).update("votes", votes).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        holder.upVotesNo.setText(newUpVotedUsers.split(" ").length - 1 + "");
                        comment.setUpVotedUsers(newUpVotedUsers);
                        notifyDataSetChanged();
                    }
                });
            } else if (tag.equals("downArrow")) {
                final String newDownVotedUsers = downVotedUsers + localCurrentUserUid + " ";

                votes--;
                fireStoreComments.document(comment.getId()).update("downVotedUsers", downVotedUsers + localCurrentUserUid + " ");

                fireStoreComments.document(comment.getId()).update("votes", votes).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        holder.downVotesNo.setText(newDownVotedUsers.split(" ").length - 1 + "");
                        comment.setDownVotedUsers(newDownVotedUsers);
                        notifyDataSetChanged();
                    }
                });
            }
        } else {
            Toast.makeText(context, "لا يمكنك التصويت لنفس التعليق أكثر من مرة", Toast.LENGTH_SHORT).show();
        }
    }


    private void showActionsDialog(final String id, final MyHolder holder, final String content, final String email, final boolean admin, final int position) {
        CharSequence colors[] = new CharSequence[]{"تعديل", "حذف"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("اختر من القائمة");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showDialog(id, holder, content, email, admin, position);
                } else {
                    fireStoreComments.document(id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(context, "تم حذف التعليق بنجاح", Toast.LENGTH_SHORT).show();
                            if (admin && !email.equals(adminEmail)) {
                                composeEmail("تم حذف تعليقك", "تم حذف تعليقك " + "\"" + content + "\"", email);
                            }
                        }
                    });

                    fireStoreReplies.whereEqualTo("commentId", id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            for (DocumentSnapshot dataSnapshot1 : documentSnapshots.getDocuments()) {
                                fireStoreReplies.document(dataSnapshot1.getId()).delete();
                            }
                        }
                    });
                }
            }
        });
        builder.show();
    }

    public void showDialog(final String id, final MyHolder holder, final String content, final String email, final boolean admin, final int position) {
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
                fireStoreComments.document(id).update("content", inputText).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        list.get(position).setContent(inputText);
                        holder.content.setText(inputText);
                        Toast.makeText(context, "تم تعديل التعليق بنجاح", Toast.LENGTH_SHORT).show();

                        if (admin && !email.equals(adminEmail)) {
                            composeEmail("تم تعديل تعليقك", "تم تعديل تعليقك " + "\"" + content + "\"", email);
                        }
                    }
                });

            }
        });
        alertDialogBuilderUserInput.show();
    }

    public void composeEmail(String subject, String body, String email) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, body);
        try {
            context.startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.v("Logging", "There are no email clients installed.");
        }
    }
}
