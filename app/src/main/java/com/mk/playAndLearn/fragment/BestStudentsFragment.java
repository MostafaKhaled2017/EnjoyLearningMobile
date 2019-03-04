package com.mk.playAndLearn.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.StudentsAdapter;
import com.mk.playAndLearn.presenter.BestStudentsFragmentPresenter;
import com.mk.playAndLearn.utils.WrapContentLinearLayoutManager;

import java.util.ArrayList;

public class BestStudentsFragment extends Fragment implements BestStudentsFragmentPresenter.View {
    StudentsAdapter recyclerAdapter;

    ProgressBar progressBar;
    RecyclerView recyclerView;
    TextView noInternetConnectionText;
    SwipeRefreshLayout swipeRefreshLayout;

    BestStudentsFragmentPresenter presenter;

    private final String TAG = "BestStudentsFragment";

    //TODO : put the code that brings the data in a method and when the page refreshes run the method again and the mehod should ensure that lists are clear at its begging
    //TODO : see what happens when the data changed rapidly and if there is a problem handle it by make data fixed and change it by refreshing
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new BestStudentsFragmentPresenter(this, getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_best_students, container, false);


        recyclerView = view.findViewById(R.id.bestStudentsRecyclerView);
        progressBar = view.findViewById(R.id.bestStudentsProgressBar);
        swipeRefreshLayout = view.findViewById(R.id.bestStudentsSwipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.startAsynkTask();
            }
        });

        noInternetConnectionText = view.findViewById(R.id.noInternetConnectionTextInBestStudents);
        noInternetConnectionText.setText("لا يوجد اتصال بالانترنت");
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryConnection();
            }
        });

        presenter.startAsynkTask();

        return view;
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
        recyclerAdapter = new StudentsAdapter(list, getActivity(), TAG, null);
        RecyclerView.LayoutManager layoutManager = new WrapContentLinearLayoutManager(getActivity());
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
}