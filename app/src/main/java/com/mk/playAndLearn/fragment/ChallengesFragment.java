package com.mk.playAndLearn.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.ChallengersActivity;
import com.mk.playAndLearn.adapters.ChallengesAdapter;
import com.mk.playAndLearn.presenter.ChallengesFragmentPresenter;
import com.mk.playAndLearn.service.NotificationsService;

import java.util.ArrayList;

import static com.mk.playAndLearn.activity.MainActivity.deleteCache;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChallengesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class ChallengesFragment extends Fragment implements ChallengesFragmentPresenter.View{

    //TODO : handle when fire base failed to get data
    //TODO : handle when the second player doesn't complete the challenge or connection problem happens to him
    //TODO : ensure that the two users of the challenge solve the same questions
    //TODO : think about adding  if the challenger has won or lose or drawn

    // TODO : Rename parameter arguments, choose names that match
    // TODO : think about making when an image clicked it opens in a full activity like other programs

    View view;
    Button startChallengeButton;
    ChallengesFragmentPresenter presenter;

    String currentSubject;

    Spinner spinner;
    ProgressBar progressBar;

    ChallengesAdapter completedChallengeRecyclerAdapter, uncompletedChallengeRecyclerAdapter;

    RecyclerView completedChallengesRecyclerView, uncompletedChallengesRecyclerView;
    TextView completeChallengesTv, uncompletedChallengesTv, noChallengesTv, noInternetConnectionText;

    private OnFragmentInteractionListener mListener;

    public ChallengesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deleteCache(getActivity());
        presenter = new ChallengesFragmentPresenter(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_challenges, container, false);

        startChallengeButton = view.findViewById(R.id.startChallengeButton);
        startChallengeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate();
            }
        });


        progressBar = view.findViewById(R.id.challengesProgressBar);
        noChallengesTv = view.findViewById(R.id.loadingText);

        noInternetConnectionText = view.findViewById(R.id.noInternetConnectionText);
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryConnection();
            }
        });

        completeChallengesTv = view.findViewById(R.id.completedChallengesText);
        uncompletedChallengesTv = view.findViewById(R.id.uncompletedChallengesText);
        completedChallengesRecyclerView = view.findViewById(R.id.completedChallengesRecyclerView);
        uncompletedChallengesRecyclerView = view.findViewById(R.id.uncompletedChallengesRecyclerView);

        spinner = view.findViewById(R.id.subjectsSpinnerInChallengesFragment);
        final ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.subjects_array, android.R.layout.simple_spinner_item);
        subjectsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(subjectsAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSubject = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        presenter.startAsynkTask();

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
    public void onResume() {
        super.onResume();
        deleteCache(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.removeListeners();
    }

    @Override
    public void navigate() {
        Intent i = new Intent(getActivity(), ChallengersActivity.class);
        i.putExtra("subject", currentSubject);
        startActivity(i);
    }

    @Override
    public void retryConnection() {
        noInternetConnectionText.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        presenter.startAsynkTask();
    }

    @Override
    public void startUnCompletedChallengesAdapter(ArrayList uncompletedChallengesList) {
            uncompletedChallengeRecyclerAdapter = new ChallengesAdapter(uncompletedChallengesList, getActivity());
            RecyclerView.LayoutManager uncompletedChallengesLayoutManager = new LinearLayoutManager(getActivity());
            uncompletedChallengesRecyclerView.setLayoutManager(uncompletedChallengesLayoutManager);
            uncompletedChallengesRecyclerView.setItemAnimator(new DefaultItemAnimator());
            uncompletedChallengesRecyclerView.setNestedScrollingEnabled(false);
            uncompletedChallengesRecyclerView.setAdapter(uncompletedChallengeRecyclerAdapter);
    }

    @Override
    public void startCompletedChallengesAdapter(ArrayList completedChallengesList) {
            completedChallengeRecyclerAdapter = new ChallengesAdapter(completedChallengesList, getActivity());
            RecyclerView.LayoutManager completedChallengesLayoutManager = new LinearLayoutManager(getActivity());
            completedChallengesRecyclerView.setLayoutManager(completedChallengesLayoutManager);
            completedChallengesRecyclerView.setItemAnimator(new DefaultItemAnimator());
            completedChallengesRecyclerView.setNestedScrollingEnabled(false);
            completedChallengesRecyclerView.setAdapter(completedChallengeRecyclerAdapter);

    }

    @Override
    public void notifyAdapters(int completedListSize, int uncompletedListSize, String src) {
        completedChallengesRecyclerView.removeAllViews();
        uncompletedChallengesRecyclerView.removeAllViews();
        completedChallengeRecyclerAdapter.notifyDataSetChanged();
        uncompletedChallengeRecyclerAdapter.notifyDataSetChanged();
        Log.v("Logging23", "completed list size : " + completedListSize
                + ", uncompleted list size : " + uncompletedListSize + " in : " + src);
    }

    @Override
    public void onDataFound() {
        hideProgressBar();
        noChallengesTv.setVisibility(android.view.View.GONE);
        noInternetConnectionText.setVisibility(android.view.View.GONE);
    }

    @Override
    public void hideProgressBar() {
        if(progressBar.getVisibility() != View.GONE){
            progressBar.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    public void hideUncompletedChallengesTv() {
        uncompletedChallengesTv.setVisibility(android.view.View.GONE);
    }

    @Override
    public void hideCompletedChallengesTv() {
        completeChallengesTv.setVisibility(android.view.View.GONE);
    }

    @Override
    public void showCompletedChallengesTv() {
        completeChallengesTv.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    public void showUncompletedChallengesTv() {
        uncompletedChallengesTv.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    public void hideNoChallengesTv() {
        noChallengesTv.setVisibility(View.GONE);
    }
    @Override
    public void showNoChallengesTv() {
        noChallengesTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void startNotificationService(int player1childrenCount, int player2childrenCount) {
        //TODO : add timer if needed
        //TODO : remove this method and use firebase function instead

        Intent serviceIntent = new Intent(getActivity(), NotificationsService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//TODO :check this
            //getActivity().startForegroundService(serviceIntent); TODO : add this after solving its errors
        } else {
            getActivity().startService(serviceIntent);
        }
    }

    @Override
    public void onNoInternetConnection() {
        hideProgressBar();
        hideCompletedChallengesTv();
        hideUncompletedChallengesTv();
        hideNoChallengesTv();
        noInternetConnectionText.setVisibility(android.view.View.VISIBLE);
    }
}