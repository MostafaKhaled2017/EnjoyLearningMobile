package com.mk.playAndLearn.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.SearchActivity;
import com.mk.playAndLearn.adapters.StudentsAdapter;
import com.mk.playAndLearn.model.User;
import com.mk.playAndLearn.presenter.FriendsFragmentPresenter;

import java.util.ArrayList;


public class FriendsFragment extends Fragment implements FriendsFragmentPresenter.View {
    ArrayList<User> list = new ArrayList();
    StudentsAdapter recyclerAdapter;

    ProgressBar progressBar;
    RecyclerView recyclerView;
    String subject, userId;
    TextView noInternetConnectionText, noFriendsTv;
    SwipeRefreshLayout swipeRefreshLayout;
    FriendsFragmentPresenter presenter;

    private final String TAG = "FriendsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();//TODO : check this
        if (intent != null) {
            subject = intent.getStringExtra("subject");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        presenter = new FriendsFragmentPresenter(this, getActivity());
        presenter.startAsynkTask(subject);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        recyclerView = view.findViewById(R.id.friendsRecyclerView);
        progressBar = view.findViewById(R.id.friendsProgressBar);
        swipeRefreshLayout = view.findViewById(R.id.friendsSwipeRefreshLayout);
        noFriendsTv = view.findViewById(R.id.noActiveStudentsTv);
        noInternetConnectionText = view.findViewById(R.id.noInternetConnectionText);

        presenter = new FriendsFragmentPresenter(this, getActivity());

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.startAsynkTask(subject);
            }
        });

        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noInternetConnectionText.setVisibility(View.GONE);
                noFriendsTv.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                presenter.startAsynkTask(subject);
            }
        });
        noInternetConnectionText.setText("لا يوجد اتصال بالانترنت");

        //presenter.startAsynkTask(subject); it's called in onResume

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFriendsSearchDialog();
            }
        });

        return view;
    }

    public void showFriendsSearchDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity());//TODO : check this
        android.view.View view = layoutInflaterAndroid.inflate(R.layout.dialog, null);

        final AlertDialog alertDialogBuilderUserInput = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("إلغاء", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("بحث", null)
                .create();

        final EditText inputComment = view.findViewById(R.id.dialog_value);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText("إضافة صديق");
        inputComment.setHint("اكتب اسم صديقك هنا للبحث عنه");

        alertDialogBuilderUserInput.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialogInterface) {

                Button button = alertDialogBuilderUserInput.getButton(AlertDialog.BUTTON_NEGATIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String commentText = inputComment.getText().toString().trim();
                        if (TextUtils.isEmpty(commentText)) {
                            inputComment.setError("لا يمكنك ترك هذا الحقل فارغا");
                        } else {
                            Intent intent = new Intent(getActivity(), SearchActivity.class);
                            intent.putExtra("userName", commentText);
                            startActivity(intent);
                            dialogInterface.dismiss();
                        }
                    }
                });
            }
        });

        alertDialogBuilderUserInput.show();

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
        progressBar.setVisibility(View.GONE);
        noInternetConnectionText.setVisibility(View.VISIBLE);
        hideNoFriendsTv();
        hideSwipeRefreshLayout();
    }

    @Override
    public void hideProgressBar() {
        if (progressBar.getVisibility() == View.VISIBLE)
            progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showProgressBar() {
        if (progressBar.getVisibility() != View.VISIBLE)
            progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoInternetConnectionText() {
        noInternetConnectionText.setVisibility(View.GONE);
    }

    @Override
    public void showNoFriendsTv() {
        noFriendsTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoFriendsTv() {
        noFriendsTv.setVisibility(View.GONE);
    }

}

