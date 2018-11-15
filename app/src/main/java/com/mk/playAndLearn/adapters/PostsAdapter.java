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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.AdminQuestionActivity;
import com.mk.playAndLearn.activity.PostInDetailsActivity;
import com.mk.playAndLearn.model.Post;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.currentUser;
import static com.mk.playAndLearn.utils.Firebase.fireStoreComments;
import static com.mk.playAndLearn.utils.Firebase.fireStorePosts;
import static com.mk.playAndLearn.utils.Strings.adminEmail;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.MyHolder> {

    ArrayList<Post> list;
    Context context;
    String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    long votes;

    public PostsAdapter(ArrayList<Post> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        MyHolder myHolder = new MyHolder(view);

        return myHolder;
    }

    @Override
    public void onBindViewHolder(final MyHolder holder, final int position) {
        final Post post = list.get(position);
        if (post.getContent() != null)
            holder.content.setText(post.getContent());
        if (post.getDate() != null) {
            if (post.isPosted() || !post.getWriterUid().equals(localCurrentUserUid)) {
                holder.date.setText(post.getDate());
            } else {
                holder.date.setText("جارى النشر...");
            }
        }

        holder.votes.setText(post.getVotes() + "");

        if (post.getWriter() != null && !post.getWriter().equals(""))
            holder.name.setText(post.getWriter());
        if (post.getImage() != null && !post.getImage().equals(""))
            Picasso.with(context).load(post.getImage()).placeholder(R.drawable.picasso_placeholder).into(holder.imageView);
      /*  if (!post.isPosted()) {
            {
                holder.warningIcon.setVisibility(View.VISIBLE);
                holder.warningIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "لم يتم رفع المنشور برجاء التأكد من الاتصال بالانترنت وسيتم رفعه تلقائيا عند الإتصال بالإنترنت", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        else {
            holder.warningIcon.setVisibility(View.GONE);
        }*/


        holder.upArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireStorePosts.document(post.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.v("Log83", "up arrow onDataChanged");
                        validateVoting(documentSnapshot, post, holder, "upArrow");
                    }
                });
            }
        });

        holder.downArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireStorePosts.document(post.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.v("Log83", "down arrow onDataChanged");
                        validateVoting(documentSnapshot, post, holder, "downArrow");
                    }
                });

            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostInDetailsActivity.class);
                intent.putExtra("content", post.getContent());
                intent.putExtra("date", post.getDate());
                intent.putExtra("name", post.getWriter());
                intent.putExtra("image", post.getImage());
                intent.putExtra("postWriterUid", post.getWriterUid());
                if (post.getId() != null)
                    intent.putExtra("id", post.getId());
                context.startActivity(intent);
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                String localCurrentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                //TODO : change this way
                if (post.getWriterUid().equals(localCurrentUserUid) || localCurrentUserEmail.equals(adminEmail)) {
                    boolean admin = false;
                    if (localCurrentUserEmail.equals(adminEmail))
                        admin = true;

                    showActionsDialog(post.getId(), holder, post.getContent(), post.getEmail(), admin, position); //TODO :  search why I need to add one
                }
                return true;//TODO : check this
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
                    fireStorePosts.document(id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(context, "تم حذف المنشور بنجاح", Toast.LENGTH_SHORT).show();
                            if (admin && !email.equals(adminEmail)) {
                                composeEmail("تم حذف منشورك", "تم حذف منشورك " + "\"" + content + "\"", email);
                            }
                        }
                    });

                    fireStoreComments.whereEqualTo("postId", id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            for (DocumentSnapshot dataSnapshot1 : documentSnapshots.getDocuments()) {
                                fireStoreComments.document(dataSnapshot1.getId()).delete();
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
        dialogTitle.setText("تعديل المنشور");

        alertDialogBuilderUserInput.setCancelable(true);
        alertDialogBuilderUserInput.setPositiveButton("تعديل", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String inputText = inputTextEt.getText().toString();
                fireStorePosts.document(id).update("content", inputText).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.v("Logging", "position is : " + position + " , postContent is : " + list.get(position).getContent());
                        list.get(position).setContent(inputText);
                        holder.content.setText(inputText);
                        notifyDataSetChanged();

                        Toast.makeText(context, "تم تعديل المنشور بنجاح", Toast.LENGTH_SHORT).show();

                        if (admin && !email.equals(adminEmail)) {
                            composeEmail("تم تعديل منشورك", "تم تعديل منشورك " + "\"" + content + "\"", email);
                        }
                    }
                });

            }
        });
        alertDialogBuilderUserInput.show();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView content, date, name, votes;
        ImageView imageView, upArrow, downArrow;
        CardView cardView;


        MyHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.postContentInDetails);
            date = itemView.findViewById(R.id.postDateInDetails);
            name = itemView.findViewById(R.id.postUserNameInDetails);
            votes = itemView.findViewById(R.id.numberOfVotes);
            imageView = itemView.findViewById(R.id.postImageInDetails);
            upArrow = itemView.findViewById(R.id.upArrow);
            downArrow = itemView.findViewById(R.id.downArrow);
            cardView = itemView.findViewById(R.id.card_view_of_posts);
        }
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

    void validateVoting(DocumentSnapshot dataSnapshot, final Post post, final PostsAdapter.MyHolder holder, String tag) {
        Log.v("postVotingLogging", "datasnapshot is : " + dataSnapshot);
        String upVotedUsers = dataSnapshot.getString("upVotedUsers");
        String downVotedUsers = dataSnapshot.getString("downVotedUsers");
        votes = dataSnapshot.getLong("votes");
        if (upVotedUsers != null && downVotedUsers != null) {
            String[] upVotedUsersArray = upVotedUsers.split(" ");
            String[] downVotedUsersArray = downVotedUsers.split(" ");
            if (!isVoted(upVotedUsersArray, downVotedUsersArray)) {
                if (tag.equals("upArrow")) {
                    votes++;
                    fireStorePosts.document(post.getId()).update("upVotedUsers", upVotedUsers + localCurrentUserUid + " ");
                } else if (tag.equals("downArrow")) {
                    votes--;
                    fireStorePosts.document(post.getId()).update("downVotedUsers", downVotedUsers + localCurrentUserUid + " ");

                }
                fireStorePosts.document(post.getId()).update("votes", votes).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.v("Logging", "votes : " + votes);
                        holder.votes.setText(votes + "");
                        post.setVotes(votes);
                        notifyDataSetChanged();
                    }
                });
            } else {
                Toast.makeText(context, "لا يمكنك التصويت لنفس السؤال أكثر من مرة", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "لا يمكن التصويت لهذا المنشور", Toast.LENGTH_SHORT).show();
        }
    }

}
