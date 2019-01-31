package com.mk.playAndLearn.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.AddArticleActivity;
import com.mk.playAndLearn.activity.LessonContentActivity;
import com.mk.playAndLearn.model.Lesson;

import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.fireStoreComments;
import static com.mk.playAndLearn.utils.Firebase.fireStoreLessons;
import static com.mk.playAndLearn.utils.Strings.adminEmail;

public class LessonsAdapter extends RecyclerView.Adapter<LessonsAdapter.MyHolder> {

    ArrayList<Lesson> list;
    Context context;
    String localCurrentUserUid;

    public LessonsAdapter(ArrayList<Lesson> list, Context context, String localCurrentUserUid) {
        this.list = list;
        this.context = context;
        this.localCurrentUserUid = localCurrentUserUid;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.lesson_title_item, parent, false);
        MyHolder myHolder = new MyHolder(view);

        return myHolder;
    }

    @Override
    public void onBindViewHolder(final MyHolder holder, final int position) {
        final Lesson lesson = list.get(position);
        if (lesson.getTitle() != null)
            holder.title.setText(lesson.getTitle());

        holder.lessonTitleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent lessonIntent = new Intent(context, LessonContentActivity.class);
                lessonIntent.putExtra("lessonTitle", lesson.getTitle());
                lessonIntent.putExtra("lessonContent", lesson.getContent());
                context.startActivity(lessonIntent);
            }
        });

        holder.lessonTitleLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                String localCurrentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                //TODO : change this way
                if (lesson.getWriterUid().equals(localCurrentUserUid) || localCurrentUserEmail.equals(adminEmail)) {
                    boolean admin = false;
                    if (localCurrentUserEmail.equals(adminEmail))
                        admin = true;

                    showActionsDialog(lesson.getLessonId(), holder, lesson, lesson.getWriterEmail(), admin, position);
                }
                return true;//TODO : check this
            }
        });

    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView title, date;
        RelativeLayout lessonTitleLayout;

        MyHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.lessonTitle);
            date = itemView.findViewById(R.id.date);
            lessonTitleLayout = itemView.findViewById(R.id.lessonTitleView);
        }
    }

    private void showActionsDialog(final String id, final LessonsAdapter.MyHolder holder, final Lesson lesson, final String email, final boolean admin, final int position) {
        CharSequence colors[] = new CharSequence[]{"تعديل", "حذف"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("اختر من القائمة");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent intent = new Intent(context, AddArticleActivity.class);
                    intent.putExtra("lesson", lesson);
                    context.startActivity(intent);
                } else {
                    fireStoreLessons.document(id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(context, "تم حذف الموضوع بنجاح", Toast.LENGTH_SHORT).show();
                            list.remove(position);
                            notifyDataSetChanged();
                            // homeFragment.loadData();

                            if (admin && !email.equals(adminEmail)) {
                                composeEmail("تم حذف موضوعك", "تم حذف موضوعك " + "\"" + lesson.getContent() + "\"", email);
                            }
                        }
                    });

                }
            }
        });
        builder.show();
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