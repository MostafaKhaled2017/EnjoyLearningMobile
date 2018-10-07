package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Lesson;

import java.util.ArrayList;

public class AdminLessonContentActivity extends AppCompatActivity {

    DatabaseReference usersReference, lessonsReference;
    FirebaseDatabase database;

    TextView writerNameTv, subjectTv, writerTypeTv, lessonPositionTv;
    EditText lessonTitleEt, lessonContentEt;
    Button skipLessonButton;
    ArrayList list = new ArrayList();
    Lesson lesson;
    int index;
    String writerType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_lesson_content);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            list = intent.getParcelableArrayListExtra("lessonsList");
            index = intent.getIntExtra("index", -1);
        }

        writerNameTv = findViewById(R.id.writerNameTv);
        subjectTv = findViewById(R.id.subjectTv);
        writerTypeTv = findViewById(R.id.writerTypeTv);
        lessonPositionTv = findViewById(R.id.lessonPositionTv);
        lessonTitleEt = findViewById(R.id.lessonTitleEt);
        lessonContentEt = findViewById(R.id.lessonContentEt);
        skipLessonButton = findViewById(R.id.skipLessonButton);

        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
        lessonsReference = database.getReference("lessons");

        if (index < list.size()) {
            lesson = (Lesson) list.get(index);
            writerNameTv.append(lesson.getWriterName());
            subjectTv.append(lesson.getSubject());
            lessonPositionTv.append("الوحدة : " + lesson.getUnitNo() + ", الدرس : " + lesson.getLessonNo());
            lessonTitleEt.setText(lesson.getTitle());
            lessonContentEt.setText(lesson.getContent());

            usersReference.child(lesson.getWriterUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    writerType = dataSnapshot.child("userType").getValue().toString();
                    writerTypeTv.append(writerType);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {
            Toast.makeText(this, "لا يوجد دروس أخري", Toast.LENGTH_SHORT).show();
        }

        skipLessonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                skipLesson();
            }
        });
    }

    public void nextLesson() {
        index++;
        Intent intent1 = new Intent(this, AdminLessonContentActivity.class);
        intent1.putParcelableArrayListExtra("lessonsList", list);
        intent1.putExtra("index", index);
        if (index < list.size()) {
            startActivity(intent1);
            finish();
        } else {
            Toast.makeText(this, "انتهت الدروس", Toast.LENGTH_SHORT).show();
        }
    }

    public void skipLesson() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("تنبيه هام");
        dialog.setMessage("هل تريد تخطى هذا الدرس؟");
        dialog.setPositiveButton("موافق", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                nextLesson();
            }
        });

        dialog.create();
        dialog.show();
    }

    public void approvePart(View view) {
        usersReference.child(lesson.getWriterUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int userPoints = Integer.parseInt(dataSnapshot.child("points").getValue().toString());
                usersReference.child(lesson.getWriterUid()).child("points").setValue(userPoints + 5);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        lessonsReference.child(lesson.getLessonId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               /* lessonsReference.child(lesson.getLessonId()).child("reviewed").setValue(true);
                lessonsReference.child(lesson.getLessonId()).child("title").setValue(lesson.getTitle());
                lessonsReference.child(lesson.getLessonId()).child("content").setValue(lesson.getContent());*/
               //TODO
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Toast.makeText(this, "تم قبول الدرس", Toast.LENGTH_SHORT).show();
        if (writerType.equals("طالب")) {
            composeEmail("تم قبول الدرس الذى رفعته", "تم قبول الدرس الذى رفعته قبولا جزئيا حيث سيتم رفع أجزاء منه فى البرنامج" + "\"" + lesson.getTitle() + "\"" + " وسيتم زيادة نقطك 5 نقاط");
        } else {
            composeEmail("تم قبول الدرس الذى رفعته", "تم قبول الدرس الذى رفعته " + "\"" + lesson.getTitle() + "\"");
        }
    }

    public void refuseLesson(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("تنبيه هام");
        dialog.setMessage("هل أنت متأكد أنك تريد حذف هذا السؤال ؟");
        dialog.setPositiveButton("نعم", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                usersReference.child(lesson.getWriterUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int userRefusedLessons = Integer.parseInt(dataSnapshot.child("refusedLessons").getValue().toString());
                        usersReference.child(lesson.getWriterUid()).child("refusedLessons").setValue(userRefusedLessons + 1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                lessonsReference.child(lesson.getLessonId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        lessonsReference.child(lesson.getLessonId()).removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                composeEmail("تم رفض الدرس الذى رفعته", "تم رفض الدرس الذى رفعته " + "\"" + lesson.getTitle() + "\"");
                Toast.makeText(AdminLessonContentActivity.this, "تم رفض الدرس", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.create();
        dialog.show();
    }

    public void acceptLesson(View view) {
        usersReference.child(lesson.getWriterUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int userPoints = Integer.parseInt(dataSnapshot.child("points").getValue().toString());
                usersReference.child(lesson.getWriterUid()).child("points").setValue(userPoints + 10);
                int userAcceptedLessons = Integer.parseInt(dataSnapshot.child("acceptedLessons").getValue().toString());
                usersReference.child(lesson.getWriterUid()).child("acceptedLessons").setValue(userAcceptedLessons + 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        
        lessonsReference.child(lesson.getLessonId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lessonsReference.child(lesson.getLessonId()).child("reviewed").setValue(true);
                lessonsReference.child(lesson.getLessonId()).child("title").setValue(lesson.getTitle());
                lessonsReference.child(lesson.getLessonId()).child("content").setValue(lesson.getContent());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Toast.makeText(this, "تم قبول الدرس", Toast.LENGTH_SHORT).show();
        if (writerType.equals("طالب")) {
            composeEmail("تم قبول الدرس الذى رفعته", "تم قبول الدرس الذى رفعته " + "\"" + lesson.getTitle() + "\"" + " وسيتم زيادة نقطك 10 نقاط");
        } else {
            composeEmail("تم قبول الدرس الذى رفعته", "تم قبول الدرس الذى رفعته " + "\"" + lesson.getTitle() + "\"");
        }    }

    public void composeEmail(String subject, String body) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{lesson.getWriterEmail()});
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, body);
        try {
            startActivityForResult(Intent.createChooser(i, "Send mail..."), 0);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(AdminLessonContentActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            nextLesson();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return  true;
    }
}

