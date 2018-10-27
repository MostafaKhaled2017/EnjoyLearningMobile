package com.mk.playAndLearn.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.LessonsAdapter;
import com.mk.playAndLearn.model.Lesson;
import com.mk.playAndLearn.presenter.LearnFragmentPresenter;
import com.mk.playAndLearn.utils.GridAutofitLayoutManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import static com.mk.playAndLearn.activity.MainActivity.deleteCache;
import static com.mk.playAndLearn.utils.Firebase.lessonsReference;

public class LearnFragment extends Fragment implements LearnFragmentPresenter.View {
    //TODO : adjust colors of fonts and colors of the app
    //TODO : think about remove fragments presenter

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    View view;

    LessonsAdapter recyclerAdapter;
    String currentSubject;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    LearnFragmentPresenter presenter;

    private OnFragmentInteractionListener mListener;

    Spinner subjectsSpinner;
    TextView noLessonsTextView, noInternetConnectionText;

    public LearnFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deleteCache(getActivity());
        presenter = new LearnFragmentPresenter(this);//TODO : check that this is correct

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_learn, container, false);
        subjectsSpinner = view.findViewById(R.id.subjectsSpinnerInLearnFragment);
        recyclerView = view.findViewById(R.id.lessonsRecyclerView);
        progressBar = view.findViewById(R.id.lessonsProgressBar);
        noLessonsTextView = view.findViewById(R.id.noLessonsText);
        noInternetConnectionText = view.findViewById(R.id.noInternetConnectionText);
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryConnection();
            }
        });

        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(subjectsSpinner);

            // Set popupWindow height to 850px
            popupWindow.setHeight(850);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        final ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.subjects_array, android.R.layout.simple_spinner_item);
        subjectsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        subjectsSpinner.setAdapter(subjectsAdapter);

        subjectsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSubject = adapterView.getItemAtPosition(i).toString();
                hideNoLessonsTextView();
                presenter.startAsynkTask(currentSubject);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
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
        presenter.removeListeners();
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
        presenter.startAsynkTask(currentSubject);
    }

    @Override
    public void hideNoLessonsTextView() {
        noLessonsTextView.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        deleteCache(getActivity());
    }

    @Override
    public void onNoInternetConnection() {
        noInternetConnectionText.setVisibility(android.view.View.VISIBLE);
        hideNoLessonsTextView();
        hideProgressBar();
    }

    @Override
    public void notifyAdapter() {
        recyclerView.removeAllViews();
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoadingData() {
        progressBar.setVisibility(android.view.View.VISIBLE);
        noInternetConnectionText.setVisibility(android.view.View.GONE);
        hideNoLessonsTextView();
    }

    @Override
    public void onDataFound() {
        hideProgressBar();
        noLessonsTextView.setVisibility(android.view.View.GONE);
        noInternetConnectionText.setVisibility(android.view.View.GONE);
    }

    @Override
    public void hideProgressBar() {
        if (progressBar.getVisibility() != android.view.View.GONE)
            progressBar.setVisibility(android.view.View.GONE);
    }

    @Override
    public void showNoLessonsTextView() {
        noLessonsTextView.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    public void startRecyclerAdapter(ArrayList list) {
        recyclerAdapter = new LessonsAdapter(list, getActivity());
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
    }
}
