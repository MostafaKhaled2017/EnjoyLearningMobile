package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mk.enjoylearning.R;

public class LessonContentActivity extends AppCompatActivity {
    View view;

    TextView tvContent;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_lesson_content);
        Toolbar toolbar = findViewById(R.id.toolbarInLesson);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        TextView title = findViewById(R.id.toolbar_title_in_lesson);

        tvContent = findViewById(R.id.lessonContent);
        tvContent.setMovementMethod(LinkMovementMethod.getInstance());

        Intent intent = getIntent();
        if (intent != null) {
            String lessonTitle = intent.getStringExtra("title");
            String lessonContent = intent.getStringExtra("content");
            tvContent.setText(lessonContent);//TODO think about changing the font to the custom font in tahrir lounge app
            title.setText(lessonTitle);

        } else {
            Toast.makeText(this, "رجاء إعادة المحاولة", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
