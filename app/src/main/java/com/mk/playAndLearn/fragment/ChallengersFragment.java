package com.mk.playAndLearn.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.LeaderBoardActivity;
import com.mk.playAndLearn.adapters.StudentsAdapter;
import com.mk.playAndLearn.presenter.ChallengersFragmentPresenter;
import com.mk.playAndLearn.utils.WrapContentLinearLayoutManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChallengersFragment extends Fragment implements ChallengersFragmentPresenter.View {
    //TODO : adjust colors of fonts and colors of the app
    //TODO : think about remove fragments presenter

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    View view;

    StudentsAdapter recyclerAdapter;
    String currentSubject;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    TextView nameTv, pointsTv;
    ImageView userImageIv;
    Button leaderboardBtn;

    ChallengersFragmentPresenter presenter;

    boolean initialDataLoaded = false;

    private final String TAG = "ChallengersFragment";

    private OnFragmentInteractionListener mListener;

    TextView noStudentsTv, noInternetConnectionText;
   // SwipeRefreshLayout swipeRefreshLayout;

    public ChallengersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new ChallengersFragmentPresenter(this, getActivity());//TODO : check that this is correct

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_challengers, container, false);
        recyclerView = view.findViewById(R.id.studentsRecyclerViewInFragment);
        progressBar = view.findViewById(R.id.bestStudentsProgressBarInFragment);
        nameTv = view.findViewById(R.id.userNameInFragment);
        pointsTv = view.findViewById(R.id.pointsInFragment);
        userImageIv = view.findViewById(R.id.userImageInFragment);
        leaderboardBtn = view.findViewById(R.id.leaderboardBtn);
        //swipeRefreshLayout = view.findViewById(R.id.bestStudentsSwipeRefreshLayoutInFragment);
        noStudentsTv = view.findViewById(R.id.noStudentsTextInFragment);

        recyclerView.setNestedScrollingEnabled(false);

        noInternetConnectionText = view.findViewById(R.id.noInternetConnectionTextInFragment);
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryConnection();
            }
        });

        leaderboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), LeaderBoardActivity.class));
            }
        });

/*        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.startAsynkTask();
            }
        });*/

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            presenter.getUserData();
            FirebaseAuth auth = FirebaseAuth.getInstance();//TODO : remove repeating this
            auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    if (currentUser != null && !initialDataLoaded) {
                        presenter.startAsynkTask();
                        initialDataLoaded = true;
                    }
                }
            });
        } else {
            initialDataLoaded = false;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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
    public void setUserData(String name, String imageUrl, long points) {
        nameTv.setText(name);
        pointsTv.setText(points + "");
        Picasso.with(getActivity()).load(imageUrl).into(userImageIv);
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
       /* if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }*/
    }
}
