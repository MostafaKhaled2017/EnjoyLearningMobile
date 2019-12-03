package com.mk.playAndLearn.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.StudentsAdapter;
import com.mk.playAndLearn.presenter.BestStudentsInGeneralChallengeActivityPresenter;
import com.mk.playAndLearn.utils.WrapContentLinearLayoutManager;

import java.util.ArrayList;

public class BestStudentsInGeneralChallengeActivity extends AppCompatActivity implements BestStudentsInGeneralChallengeActivityPresenter.View {

    StudentsAdapter recyclerAdapter;

    ProgressBar progressBar;
    RecyclerView recyclerView;
    TextView noInternetConnectionText;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView noStudentsTv;

    BestStudentsInGeneralChallengeActivityPresenter presenter;

    private final String TAG = "BestStudentsFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_best_students);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        /*final Drawable upArrow = getResources().getDrawable(R.drawable.backf);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);*/


        TextView toolbarTitle = toolbar.findViewById(R.id.tahadi);
        toolbarTitle.setText("أوائل التحدى العام");

        presenter = new BestStudentsInGeneralChallengeActivityPresenter(this);

        recyclerView = findViewById(R.id.bestStudentsInGeneralChallengeRecyclerView);
        progressBar = findViewById(R.id.bestStudentsInGeneralChallengeProgressBar);
        swipeRefreshLayout = findViewById(R.id.bestStudentsInGeneralChallengeSwipeRefreshLayout);
        noStudentsTv = findViewById(R.id.noStudentsText);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.startAsynkTask();
            }
        });

        noInternetConnectionText = findViewById(R.id.noInternetConnectionText);
        noInternetConnectionText.setText("لا يوجد اتصال بالانترنت");
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryConnection();
            }
        });

        presenter.startAsynkTask();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showNoStudentsText() {
        noStudentsTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoStudentsText() {
        noStudentsTv.setVisibility(View.INVISIBLE);
    }

    @Override
    public void retryConnection() {
        noInternetConnectionText.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        presenter.startAsynkTask();
    }

    @Override
    public void hideProgressBar() {
        if (progressBar.getVisibility() != android.view.View.GONE)
            progressBar.setVisibility(android.view.View.GONE);
    }

    @Override
    public void notifyAdapter() {
        recyclerView.removeAllViews();
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void startRecyclerAdapter(ArrayList list) {
        recyclerAdapter = new StudentsAdapter(list, this, TAG, null, null, null, -1, false);
        RecyclerView.LayoutManager layoutManager = new WrapContentLinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void handleNoInternetConnection() {
        progressBar.setVisibility(android.view.View.GONE);
        noInternetConnectionText.setVisibility(android.view.View.VISIBLE);
        hideSwipeRefreshLayout();
    }

    @Override
    public void hideSwipeRefreshLayout() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void finishch(View view) {

        finish();
    }
}
