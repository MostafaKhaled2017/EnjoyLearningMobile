package com.mk.playAndLearn.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.LessonsAdapter;
import com.mk.playAndLearn.presenter.LessonsFragmentPresenter;
import com.mk.playAndLearn.utils.WrapContentLinearLayoutManager;

import java.util.ArrayList;

import static com.mk.playAndLearn.utils.sharedPreference.getSavedGrade;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LessonsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class LessonsFragment extends Fragment implements LessonsFragmentPresenter.View {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    ProgressBar progressBar;

    // TODO: Rename and change types of parameters

    private OnFragmentInteractionListener mListener;

    LessonsFragmentPresenter presenter;

    LessonsAdapter recyclerAdapter;
    TextView noLessonsText, noInternetConnectionText;

    RecyclerView recyclerView;
    Spinner spinner;

    String currentSubject = "", internalCurrentSubject = "";

    public LessonsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new LessonsFragmentPresenter(this, getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View myView = inflater.inflate(R.layout.fragment_lessons, container, false);
        recyclerView = myView.findViewById(R.id.lessonsRecyclerView);
        progressBar = myView.findViewById(R.id.lessonsProgressBar);
        noLessonsText = myView.findViewById(R.id.noLessonsText);

        spinner = myView.findViewById(R.id.subjectsSpinnerInLessonsFragment);

        Log.v("gradeLogging", "saved grade is : " + getSavedGrade(getActivity()));

        ArrayAdapter<CharSequence> subjectsAdapter;

        if(getSavedGrade(getActivity()).equals("الصف الأول الإعدادى")) {
            subjectsAdapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.first_preparatory_subjects_array, R.layout.simple_spinner_item);
        } else {
            subjectsAdapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.preparatory_subjects_array, R.layout.simple_spinner_item);
        }


        subjectsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(subjectsAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSubject = adapterView.getItemAtPosition(i).toString();
                FirebaseAuth localAuth = FirebaseAuth.getInstance();
                localAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        if(firebaseAuth.getCurrentUser() != null && !currentSubject.equals(internalCurrentSubject)){
                            loadData();
                            internalCurrentSubject = currentSubject;
                        }
                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        noInternetConnectionText = myView.findViewById(R.id.noInternetConnectionText);
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryConnection();
            }
        });

        return myView;

    }

    public void loadData() {
        presenter.startAsynkTask(currentSubject);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showProgressBar() {
        if (progressBar.getVisibility() != View.VISIBLE) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void retryConnection() {
        noInternetConnectionText.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        loadData();
    }

    @Override
    public void startRecyclerAdapter(final ArrayList list) {
        RecyclerView.LayoutManager layoutManager = new WrapContentLinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerAdapter = new LessonsAdapter(recyclerView, list, getActivity(), this, FirebaseAuth.getInstance().getCurrentUser().getUid());

        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onNoInternetConnection() {
        progressBar.setVisibility(android.view.View.GONE);
        noInternetConnectionText.setVisibility(android.view.View.VISIBLE);
        noLessonsText.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showToast(String value) {
        Toast.makeText(getActivity(), value, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notifyAdapter() {
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void setOnScrollListener(RecyclerView.OnScrollListener onScrollListener) {
        recyclerView.addOnScrollListener(onScrollListener);
    }

    @Override
    public void onDataFound() {
        hideProgressBar();
        noLessonsText.setVisibility(android.view.View.GONE);
        noInternetConnectionText.setVisibility(android.view.View.GONE);
    }

    @Override
    public void hideProgressBar() {
        if (progressBar.getVisibility() != android.view.View.GONE)
            progressBar.setVisibility(android.view.View.GONE);
    }

    @Override
    public void onNoLessonsExists() {
        progressBar.setVisibility(android.view.View.GONE);
        noLessonsText.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    public void hideNoLessonsText() {
        noLessonsText.setVisibility(View.GONE);
    }
}
