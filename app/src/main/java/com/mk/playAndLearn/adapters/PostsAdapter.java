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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.AdminQuestionActivity;
import com.mk.playAndLearn.activity.PostInDetailsActivity;
import com.mk.playAndLearn.model.Post;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.commentsReference;
import static com.mk.playAndLearn.utils.Firebase.postsReference;
import static com.mk.playAndLearn.utils.Strings.adminEmail;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.MyHolder> {

    ArrayList<Post> list;
    Context context;
    String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
            if(post.isPosted()) {
                holder.date.setText(post.getDate());
            }
            else {
                holder.date.setText("جارى النشر...");
            }
        }
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
                    if(localCurrentUserEmail.equals(adminEmail))
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
                    postsReference.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(context, "تم حذف المنشور بنجاح", Toast.LENGTH_SHORT).show();
                            if(admin && !email.equals(adminEmail)) {
                                composeEmail("تم حذف منشورك", "تم حذف منشورك " + "\"" + content + "\"", email);
                            }
                        }
                    });

                    commentsReference.orderByChild("postId").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                commentsReference.child(dataSnapshot1.getKey()).removeValue();
                            }
                            commentsReference.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

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
                postsReference.child(id).child("content").setValue(inputText).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.v("Logging","position is : " + position + " , postContent is : " + list.get(position).getContent());
                        list.get(position).setContent(inputText);
                        holder.content.setText(inputText);
                        notifyDataSetChanged();

                        Toast.makeText(context, "تم تعديل المنشور بنجاح", Toast.LENGTH_SHORT).show();

                        if(admin && !email.equals(adminEmail)){
                            composeEmail("تم تعديل منشورك", "تم تعديل منشورك " + "\"" + content + "\"", email);
                        }
                    }
                });

            }
        });
        alertDialogBuilderUserInput.show();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView content, date, name;
        ImageView imageView;
        CardView cardView;


        public MyHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.postContentInDetails);
            date = itemView.findViewById(R.id.postDateInDetails);
            name = itemView.findViewById(R.id.postUserNameInDetails);
            imageView = itemView.findViewById(R.id.postImageInDetails);
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

}
