package com.mk.playAndLearn.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mk.enjoylearning.R;

public class LessonContent extends Fragment {
    View view;

    TextView tvContent;

    //TODO : think about removing the menu from the par in the inner learn fragment

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_lesson_content, container, false);
        tvContent = view.findViewById(R.id.lessonContent);

        TextView title = view.findViewById(R.id.toolbar_title_in_lesson);

        Bundle lessonData = this.getArguments();
        if (lessonData != null) {
            String lessonTitle = lessonData.getString("lessonTitle");
            String lessonContent = lessonData.getString("lessonContent");
            tvContent.setText(lessonContent);//TODO think about changing the font to the custom font in tahrir lounge app
            title.setText(lessonTitle);

        } else {
            Toast.makeText(getActivity(), "رجاء إعادة المحاولة", Toast.LENGTH_SHORT).show();
        }

        return view;
    }
}
