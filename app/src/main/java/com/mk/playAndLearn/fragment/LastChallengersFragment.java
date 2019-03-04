package com.mk.playAndLearn.fragment;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.StudentsAdapter;
import com.mk.playAndLearn.model.User;
import com.mk.playAndLearn.presenter.LastChallengersFragmentPresenter;

import java.util.ArrayList;


public class LastChallengersFragment extends Fragment implements LastChallengersFragmentPresenter.View{
    ArrayList<User> list = new ArrayList();
    StudentsAdapter recyclerAdapter;

    ProgressBar progressBar;
    RecyclerView recyclerView;
    String subject, userId;
    TextView noInternetConnectionText, noStudentsTv;
    SwipeRefreshLayout swipeRefreshLayout;
    LastChallengersFragmentPresenter presenter;

    private final String TAG = "LastChallengersFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();//TODO : check this
        if (intent != null) {
            subject = intent.getStringExtra("subject");
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_last_challengers, container, false);

        recyclerView = view.findViewById(R.id.challengersRecyclerView);
        progressBar = view.findViewById(R.id.challengersProgressBar);
        swipeRefreshLayout = view.findViewById(R.id.challengersSwipeRefreshLayout);
        noStudentsTv = view.findViewById(R.id.noActiveStudentsTv);

        presenter = new LastChallengersFragmentPresenter(this, getActivity());

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.startAsynkTask(subject);
            }
        });

        noInternetConnectionText = view.findViewById(R.id.noInternetConnectionText);
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noInternetConnectionText.setVisibility(View.GONE);
                noStudentsTv.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                presenter.startAsynkTask(subject);
            }
        });
        noInternetConnectionText.setText("لا يوجد اتصال بالانترنت");

        presenter.startAsynkTask(subject);

        return view;
    }

    @Override
    public void hideSwipeRefreshLayout() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void startRecyclerAdapter(ArrayList list) {
        recyclerAdapter = new StudentsAdapter(list, getActivity(), TAG, subject);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
        hideProgressBar();
        noInternetConnectionText.setVisibility(android.view.View.VISIBLE);
        hideSwipeRefreshLayout();
    }

    @Override
    public void hideProgressBar() {
        if (progressBar.getVisibility() == View.VISIBLE)
            progressBar.setVisibility(android.view.View.GONE);
    }

    @Override
    public void hideNoInternetConnectionText() {
            noInternetConnectionText.setVisibility(View.GONE);
    }

    @Override
    public void showNoStudentTv() {
        noStudentsTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoStudentTv() {
        noStudentsTv.setVisibility(View.GONE);
    }
}

