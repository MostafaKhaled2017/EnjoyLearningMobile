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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LearnFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LearnFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LearnFragment extends Fragment {
    // TODO : adjust colors of fonts and colors of the app

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    View view;

    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList list = new ArrayList();
    LessonsAdapter recyclerAdapter;
    String currentSubject;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    Spinner subjectsSpinner;
    TextView noLessonsTextView, noInternetConnectionText;

    public LearnFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LearnFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LearnFragment newInstance(String param1, String param2) {
        LearnFragment fragment = new LearnFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("lessons");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_learn, container, false);
        subjectsSpinner = view.findViewById(R.id.subjectsSpinnerInLearnFragment);
        recyclerView = view.findViewById(R.id.lessonsRecyclerView);
        progressBar = view.findViewById(R.id.lessonsProgressBar);
        noLessonsTextView = view.findViewById(R.id.noLessonsText);
        recyclerAdapter = new LessonsAdapter(list, getActivity());
        noInternetConnectionText = view.findViewById(R.id.noInternetConnectionText);
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noInternetConnectionText.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                startAsynkTask();
            }
        });
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);

        final ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.subjects_array, android.R.layout.simple_spinner_item);
        subjectsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        subjectsSpinner.setAdapter(subjectsAdapter);

        subjectsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                noLessonsTextView.setVisibility(View.GONE);
                currentSubject = adapterView.getItemAtPosition(i).toString();
                startAsynkTask();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        startAsynkTask();

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

    public void startAsynkTask() {
        //TODO : search for a solution to this error
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Boolean doInBackground(Object[] objects) {
                try {
                    Socket sock = new Socket();
                    sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);
                    sock.close();
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Object o) {
                if ((boolean) o) {
                    getLessons(currentSubject);
                } else {
                    progressBar.setVisibility(View.GONE);
                    noInternetConnectionText.setVisibility(View.VISIBLE);
                    noLessonsTextView.setVisibility(View.INVISIBLE);
                }
            }
        };

        asyncTask.execute();
    }

    public void getLessons(String currentSubject){
        if(!list.isEmpty())
            list.clear();
        progressBar.setVisibility(View.VISIBLE);
        noInternetConnectionText.setVisibility(View.GONE);
        recyclerAdapter.notifyDataSetChanged();
        myRef.orderByChild("subject").equalTo(currentSubject).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                noLessonsTextView.setVisibility(View.GONE);
                Lesson value = dataSnapshot.getValue(Lesson.class);
                Lesson lesson = new Lesson();
                boolean reviewed =(boolean) dataSnapshot.child("reviewed").getValue();
                if(reviewed) {
                    String title = value.getTitle();
                    String content = value.getContent();
                    String arabicPosition = value.getArabicPosition();
                    lesson.setTitle(title);
                    lesson.setContent(content);
                    lesson.setArabicPosition(arabicPosition);
                    list.add(lesson);
                }
                if(progressBar.getVisibility() != View.GONE)
                    progressBar.setVisibility(View.GONE);
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Toast.makeText(getActivity(), "فشل تحميل البينات من فضلك تأكد من الاتصال بالإنترنت", Toast.LENGTH_SHORT).show();
                Log.v("Logging", "database error : " + databaseError);
                progressBar.setVisibility(View.GONE);
            }
        });

        myRef.orderByChild("subject").equalTo(currentSubject).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                if(dataSnapshot.getChildrenCount() == 0) {
                    noLessonsTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
