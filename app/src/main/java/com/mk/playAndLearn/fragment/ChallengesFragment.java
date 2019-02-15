package com.mk.playAndLearn.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
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

import com.google.firebase.auth.FirebaseAuth;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.ChallengersActivity;
import com.mk.playAndLearn.adapters.ChallengesAdapter;
import com.mk.playAndLearn.presenter.ChallengesFragmentPresenter;
import com.mk.playAndLearn.utils.WrapContentLinearLayoutManager;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.mk.playAndLearn.activity.MainActivity.deleteCache;
import static com.mk.playAndLearn.utils.Integers.dailyChallengesNumber;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedTodayChallengesNo;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChallengesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class ChallengesFragment extends Fragment implements ChallengesFragmentPresenter.View {

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

    boolean initialDataLoaded = false;

    private OnFragmentInteractionListener mListener;

    public ChallengesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deleteCache(getActivity());
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

        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinner);

            // Set popupWindow height to 850px
            popupWindow.setHeight(850);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        final ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.secondary_subjects_array, android.R.layout.simple_spinner_item);
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

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        presenter = new ChallengesFragmentPresenter(this, getActivity());
        if (isVisibleToUser) {
            FirebaseAuth localAuth = FirebaseAuth.getInstance();
            localAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if(firebaseAuth.getCurrentUser() != null && !initialDataLoaded){
                        presenter.startAsynkTask();
                        initialDataLoaded = true;
                    }
                }
            });
        }else {
            initialDataLoaded = false;
        }
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
    }

    @Override
    public void navigate() {
        if(currentSubject.equals("كل المواد")){
            Toast.makeText(getActivity(), "برجاء اختيار المادة التى تريدها", Toast.LENGTH_SHORT).show();
        } else if(dailyChallengesNumber - getSavedTodayChallengesNo(getActivity()) < 1){
            Toast.makeText(getActivity(), "لقد أنهيت عدد التحديات المسموح لك اليوم يمكنك العودة غدا للعب تحديات أخرى", Toast.LENGTH_LONG).show();
        } else {
            Intent i = new Intent(getActivity(), ChallengersActivity.class);
            i.putExtra("subject", currentSubject);
            startActivity(i);
        }
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
        RecyclerView.LayoutManager uncompletedChallengesLayoutManager = new WrapContentLinearLayoutManager(getActivity());
        uncompletedChallengesRecyclerView.setLayoutManager(uncompletedChallengesLayoutManager);
        uncompletedChallengesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        uncompletedChallengesRecyclerView.setNestedScrollingEnabled(false);
        uncompletedChallengesRecyclerView.setAdapter(uncompletedChallengeRecyclerAdapter);
    }

    @Override
    public void startCompletedChallengesAdapter(ArrayList completedChallengesList) {
        completedChallengeRecyclerAdapter = new ChallengesAdapter(completedChallengesList, getActivity());
        RecyclerView.LayoutManager completedChallengesLayoutManager = new WrapContentLinearLayoutManager(getActivity());
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
        if (progressBar.getVisibility() != View.GONE) {
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
    public void onNoInternetConnection() {
        //TODO : check what these things do
        completedChallengesRecyclerView.removeAllViews();
        uncompletedChallengesRecyclerView.removeAllViews();
        hideProgressBar();
        hideMainViews();
        hideNoChallengesTv();
        noInternetConnectionText.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    public void hideMainViews() {
        completedChallengesRecyclerView.setVisibility(View.GONE);
        uncompletedChallengesRecyclerView.setVisibility(View.GONE);
        completeChallengesTv.setVisibility(View.GONE);
        uncompletedChallengesTv.setVisibility(View.GONE);
    }

    @Override
    public void showMainViews() {
        completedChallengesRecyclerView.setVisibility(View.VISIBLE);
        uncompletedChallengesRecyclerView.setVisibility(View.VISIBLE);
        completeChallengesTv.setVisibility(View.VISIBLE);
        uncompletedChallengesTv.setVisibility(View.VISIBLE);
    }
}