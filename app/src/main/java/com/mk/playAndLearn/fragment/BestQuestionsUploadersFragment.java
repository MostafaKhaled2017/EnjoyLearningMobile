package com.mk.playAndLearn.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.StudentsAdapter;
import com.mk.playAndLearn.presenter.BestQuestionsUploadersFragmentPresenter;
import com.mk.playAndLearn.utils.WrapContentLinearLayoutManager;

import java.util.ArrayList;

public class BestQuestionsUploadersFragment extends Fragment implements BestQuestionsUploadersFragmentPresenter.View {
    StudentsAdapter recyclerAdapter;

    ProgressBar progressBar;
    RecyclerView recyclerView;
    TextView noInternetConnectionText, noStudentsTv;
    SwipeRefreshLayout swipeRefreshLayout;

    BestQuestionsUploadersFragmentPresenter presenter;

    private final String TAG = "BestQuestionsUploadersActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new BestQuestionsUploadersFragmentPresenter(this, getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_best_questions_uploaders, container, false);


        recyclerView = view.findViewById(R.id.bestStudentsInGeneralChallengeRecyclerView);
        progressBar = view.findViewById(R.id.bestStudentsInGeneralChallengeProgressBar);
        swipeRefreshLayout = view.findViewById(R.id.bestStudentsInGeneralChallengeSwipeRefreshLayout);
        noStudentsTv = view.findViewById(R.id.noStudentsText);

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
    public void showNoStudentsText() {
        noStudentsTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoStudentsText() {
        noStudentsTv.setVisibility(View.INVISIBLE);
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
        recyclerAdapter = new StudentsAdapter(list, getActivity(), TAG, null, null, null, -1);
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
