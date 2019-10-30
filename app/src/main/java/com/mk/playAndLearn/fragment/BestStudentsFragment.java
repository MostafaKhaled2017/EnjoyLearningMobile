package com.mk.playAndLearn.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
    TextView noInternetConnectionText, noStudentsTv;
    SwipeRefreshLayout swipeRefreshLayout;
    Spinner spinner;

    String currentOption;

    BestStudentsFragmentPresenter presenter;

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


        recyclerView = view.findViewById(R.id.bestStudentsInGeneralChallengeRecyclerView);
        progressBar = view.findViewById(R.id.bestStudentsInGeneralChallengeProgressBar);
        swipeRefreshLayout = view.findViewById(R.id.bestStudentsInGeneralChallengeSwipeRefreshLayout);
        spinner = view.findViewById(R.id.typeSpinner);
        noStudentsTv = view.findViewById(R.id.noStudentsTextInBestStudents);

        final ArrayAdapter<CharSequence> typesAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.leaderboard_options_array, R.layout.simple_spinner_item);
        typesAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(typesAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentOption = adapterView.getItemAtPosition(i).toString();
                presenter.startAsynkTask(currentOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.startAsynkTask(currentOption);
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

        return view;
    }

    @Override
    public void retryConnection() {
        noInternetConnectionText.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        presenter.startAsynkTask(currentOption);
    }

    @Override
    public void hideProgressBar() {
        if (progressBar.getVisibility() != android.view.View.GONE)
            progressBar.setVisibility(android.view.View.GONE);
    }

    @Override
    public void showProgressBar() {
        if (progressBar.getVisibility() != View.VISIBLE)
            progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void notifyAdapter() {
        recyclerView.removeAllViews();
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void startRecyclerAdapter(ArrayList list, String Tag) {
        recyclerAdapter = new StudentsAdapter(list, getActivity(), Tag, null, null , null, -1);
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
    public void showNoStudentsText() {
        noStudentsTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoStudentsText() {
        noStudentsTv.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideSwipeRefreshLayout() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
