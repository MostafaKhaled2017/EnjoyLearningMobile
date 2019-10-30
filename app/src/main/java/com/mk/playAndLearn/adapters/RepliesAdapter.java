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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Reply;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.fireStoreReplies;
import static com.mk.playAndLearn.utils.Strings.adminEmail;

public class RepliesAdapter extends RecyclerView.Adapter<RepliesAdapter.MyHolder> {
    ArrayList<Reply> list;
    Context context;
    //used to load the votes number at the first time only
    boolean visited;
    long votes;
    String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public RepliesAdapter(ArrayList<Reply> list, Context context, boolean visited) {
        this.list = list;
        this.context = context;
        this.visited = visited;
    }

    @Override
    public RepliesAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.replies_item, parent, false);
        RepliesAdapter.MyHolder myHolder = new RepliesAdapter.MyHolder(view);

        return myHolder;
    }

    @Override
    public void onBindViewHolder(final RepliesAdapter.MyHolder holder, final int position) {
        final Reply reply = list.get(position);
        if (reply.getUserName() != null)
            holder.name.setText(reply.getUserName().trim());
        if (reply.getContent() != null)
            holder.content.setText(reply.getContent());
        if (reply.getDate() != null) {
            if (reply.isPosted() || !reply.getWriterUid().equals(localCurrentUserUid)) {
                holder.date.setText(reply.getDate());
            } else {
                holder.date.setText("جارى النشر ...");
            }
        }
        if (reply.getUserImage() != null)
            Picasso.with(context).load(reply.getUserImage()).placeholder(R.drawable.picasso_placeholder).into(holder.imageView);


        if (reply.getUpVotedUsers() != null) {
            holder.upVotesNo.setText(reply.getUpVotedUsers().split(" ").length - 1 + "");
        }
        if (reply.getDownVotedUsers() != null) {
            holder.downVotesNo.setText(reply.getDownVotedUsers().split(" ").length - 1 + "");
        }

        holder.upArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireStoreReplies.document(reply.getReplyId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        validateVoting(documentSnapshot, reply, holder, "upArrow");
                    }
                });
            }
        });

        holder.downArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireStoreReplies.document(reply.getReplyId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.v("Log83", "down arrow onDataChanged");
                        validateVoting(documentSnapshot, reply, holder, "downArrow");
                    }
                });
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String localCurrentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                //TODO : change this hardcoded way
                if (reply.getWriterUid().equals(localCurrentUserUid) || localCurrentUserEmail.equals(adminEmail)) {
                    boolean admin = false;
                    if (localCurrentUserEmail.equals(adminEmail))
                        admin = true;

                    showActionsDialog(reply.getReplyId(), holder, reply.getContent(), reply.getUserEmail(), admin, position); //TODO :  search why I need to add one
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
        TextView name, content, date, upVotesNo, downVotesNo;
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
            cardView = itemView.findViewById(R.id.card_view_of_replies);
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

    void validateVoting(DocumentSnapshot dataSnapshot, final Reply reply, final RepliesAdapter.MyHolder holder, String tag) {
        String upVotedUsers = dataSnapshot.getString("upVotedUsers");
        String downVotedUsers = dataSnapshot.getString("downVotedUsers");
        votes = dataSnapshot.getLong("votes");
        String[] upVotedUsersArray = upVotedUsers.split(" ");
        String[] downVotedUsersArray = downVotedUsers.split(" ");
        if (!isVoted(upVotedUsersArray, downVotedUsersArray)) {
            if (tag.equals("upArrow")) {
                final String newUpVotedUsers = upVotedUsers + localCurrentUserUid + " ";

                votes++;
                fireStoreReplies.document(reply.getReplyId()).update("upVotedUsers", upVotedUsers + localCurrentUserUid + " ");

                fireStoreReplies.document(reply.getReplyId()).update("votes", votes).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        holder.upVotesNo.setText(newUpVotedUsers.split(" ").length - 1 + "");
                        reply.setUpVotedUsers(newUpVotedUsers);
                        notifyDataSetChanged();
                    }
                });
            } else if (tag.equals("downArrow")) {
                final String newDownVotedUsers = downVotedUsers + localCurrentUserUid + " ";

                votes--;
                fireStoreReplies.document(reply.getReplyId()).update("downVotedUsers", downVotedUsers + localCurrentUserUid + " ");

                fireStoreReplies.document(reply.getReplyId()).update("votes", votes).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        holder.downVotesNo.setText(newDownVotedUsers.split(" ").length - 1 + "");
                        reply.setDownVotedUsers(newDownVotedUsers);
                        notifyDataSetChanged();
                    }
                });
            }
        } else {
            Toast.makeText(context, "لا يمكنك التصويت لنفس الرد أكثر من مرة", Toast.LENGTH_SHORT).show();
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
                    fireStoreReplies.document(id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(context, "تم حذف الرد بنجاح", Toast.LENGTH_SHORT).show();
                            if (admin && !email.equals(adminEmail)) {
                                composeEmail("تم حذف ردك", "تم حذف ردك " + "\"" + content + "\"", email);
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
        View view = layoutInflaterAndroid.inflate(R.layout.dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputTextEt = view.findViewById(R.id.dialog_value);
        inputTextEt.setText(content);
        final TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText("تعديل الرد");

        alertDialogBuilderUserInput.setCancelable(true);
        alertDialogBuilderUserInput.setPositiveButton("تعديل", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String inputText = inputTextEt.getText().toString();
                fireStoreReplies.document(id).update("content", inputText).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        list.get(position).setContent(inputText);
                        holder.content.setText(inputText);
                        Toast.makeText(context, "تم تعديل الرد بنجاح", Toast.LENGTH_SHORT).show();

                        if (admin && !email.equals(adminEmail)) {
                            composeEmail("تم تعديل ردك", "تم تعديل ردك " + "\"" + content + "\"", email);
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
