package com.mk.playAndLearn.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.presenter.ProfileActivityPresenter;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity implements ProfileActivityPresenter.View {

    ProfileActivityPresenter presenter;
    TextView nameTv, gradeTv, pointsTv, schoolTypeTv;
    ImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("الصفحة الشخصية");

        presenter = new ProfileActivityPresenter(this,this);

        presenter.getUserData();

        nameTv = findViewById(R.id.name);
        gradeTv = findViewById(R.id.grade);
        pointsTv = findViewById(R.id.points);
        schoolTypeTv = findViewById(R.id.schoolType);
        userImage = findViewById(R.id.userImage);


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

    @Override
    public void setData(String userName, String grade, long points, String schoolType, String imageUrl) {
        nameTv.append(userName);
        gradeTv.append(grade);
        pointsTv.append(points + "");
        schoolTypeTv.append(schoolType);

        Picasso.with(this).load(imageUrl).placeholder(R.drawable.picasso_placeholder).into(userImage);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

}
