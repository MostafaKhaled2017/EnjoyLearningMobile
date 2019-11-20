package com.mk.playAndLearn.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.presenter.ProfileActivityPresenter;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment implements ProfileActivityPresenter.View {

    ProfileActivityPresenter presenter;
    TextView nameTv, gradeTv, pointsTv, schoolTypeTv, emailTv, studentIdTv;
    ImageView userImage;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        presenter = new ProfileActivityPresenter(this,getActivity());

       /* PieChart pieChart = findViewById(R.id.piechart);

        ArrayList noOfChallenges = new ArrayList();

        noOfChallenges.add(new Entry(250f,0));
        noOfChallenges.add(new Entry(100f,0));
        noOfChallenges.add(new Entry(50f,0));
        PieDataSet dataSet = new PieDataSet(noOfChallenges, "عدد التحديات");


        ArrayList challengesType = new ArrayList();
        challengesType.add("ربح");
        challengesType.add("تعادل");
        challengesType.add("خسارة");

        //PieData data = new PieData(challengesType, dataSet);
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        pieChart.animateXY(5000, 5000);*/
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        nameTv = view.findViewById(R.id.nameInProfile);
        gradeTv = view.findViewById(R.id.grade);
        pointsTv = view.findViewById(R.id.points);
        schoolTypeTv = view.findViewById(R.id.schoolType);
        userImage = view.findViewById(R.id.replyImage);
        emailTv = view.findViewById(R.id.email);
        studentIdTv = view.findViewById(R.id.studentIdTv);

        presenter.getUserData();

        return view;
    }

    @Override
    public void setData(String userName, String grade, long points, String schoolType, String imageUrl, String email, String studentId) {
        Log.v("sharedPrefLog", "name is : " + userName + " , grade is : " + grade);

        nameTv.setText(userName);
        gradeTv.setText(grade);
        pointsTv.setText(points + "");
        schoolTypeTv.setText(schoolType);
        emailTv.setText(email);

        if(studentId != null)
            studentIdTv.setText(studentId);

        Picasso.with(getActivity()).load(imageUrl).placeholder(R.drawable.picasso_placeholder).into(userImage);

    }
}
