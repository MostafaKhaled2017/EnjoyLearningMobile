package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.StudentsAdapter;
import com.mk.playAndLearn.presenter.SearchActivityPresenter;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements SearchActivityPresenter.View {
    String userName = "";
    private final String TAG = "SearchActivity";

    ProgressBar progressBar;
    RecyclerView recyclerView;
    TextView noInternetConnectionText, noResultsTv;
    SwipeRefreshLayout swipeRefreshLayout;

    StudentsAdapter recyclerAdapter;

    SearchActivityPresenter presenter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        actionBar.setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("إضافة صديق");

        Intent intent = getIntent();
        if(intent.getExtras() != null){
            userName = intent.getStringExtra("userName");
        }

        recyclerView = findViewById(R.id.searchRecyclerView);
        progressBar = findViewById(R.id.searchProgressBar);
        swipeRefreshLayout = findViewById(R.id.searchSwipeRefreshLayout);
        noResultsTv = findViewById(R.id.noResultsFoundTv);
        noInternetConnectionText = findViewById(R.id.noInternetConnectionText);
        
        presenter = new SearchActivityPresenter(this, this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.startAsynkTask(userName);
            }
        });

        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noInternetConnectionText.setVisibility(View.GONE);
                noResultsTv.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                presenter.startAsynkTask(userName);
            }
        });
        noInternetConnectionText.setText("لا يوجد اتصال بالانترنت");

        presenter.startAsynkTask(userName);

    }

    @Override
    public void hideSwipeRefreshLayout() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void startRecyclerAdapter(ArrayList list) {
        recyclerAdapter = new StudentsAdapter(list, this, TAG, null, null, null, -1);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyAdapter() {
        recyclerView.removeAllViews();
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void handleNoInternetConnection() {
        progressBar.setVisibility(View.GONE);
        noInternetConnectionText.setVisibility(View.VISIBLE);
        hideSwipeRefreshLayout();
    }

    @Override
    public void hideProgressBar() {
        if (progressBar.getVisibility() == View.VISIBLE)
            progressBar.setVisibility(View.GONE);
    }

    @Override
    public void hideNoInternetConnectionText() {
        noInternetConnectionText.setVisibility(View.GONE);
    }

    @Override
    public void showNoResultsTv() {
        noResultsTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoResultsTv() {
        noResultsTv.setVisibility(View.GONE);
    }

}
