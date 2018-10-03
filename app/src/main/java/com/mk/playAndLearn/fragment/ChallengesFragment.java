package com.mk.playAndLearn.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.QuestionActivity;
import com.mk.playAndLearn.adapters.ChallengesAdapter;
import com.mk.playAndLearn.model.Challenge;
import com.mk.playAndLearn.model.Question;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChallengesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChallengesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChallengesFragment extends Fragment {

    //TODO : handle when firebase failed to get data

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    View view;
    Button startChallengeButton;

    FirebaseDatabase database;
    DatabaseReference questionsRefrence;
    ArrayList list = new ArrayList<>(), list2 = new ArrayList<>()
            , playerAnswersBooleansList = new ArrayList(), playerAnswersList = new ArrayList();
    ArrayList<Challenge> challengesList = new ArrayList<>();
    String currentSubject;

    Spinner spinner;
    ProgressBar progressBar;

    ChallengesAdapter recyclerAdapter;

    RecyclerView recyclerView;

    private OnFragmentInteractionListener mListener;

    public ChallengesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChallengesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChallengesFragment newInstance(String param1, String param2) {
        ChallengesFragment fragment = new ChallengesFragment();
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
                if(!list2.isEmpty())
                    list2.clear();
                Collections.shuffle(list);
                for(int i = 0; i < 5; i++) {
                    Question question = (Question) list.get(i);
                    list2.add(question);
                }

                Intent i = new Intent(getActivity(), QuestionActivity.class);

                i.putExtra("questionNo", 0);
                i.putExtra("score", 0);
                i.putExtra("subject", currentSubject);
                i.putParcelableArrayListExtra("player1AnswersBooleans", playerAnswersBooleansList);
                i.putParcelableArrayListExtra("player1Answers", playerAnswersList);
                i.putParcelableArrayListExtra("list", list2);
                //TODO : ensuring that the intent doesn't work until the data loaded for example if not show a dialog asking to connect to the internet
                startActivity(i);
            }
        });

        database = FirebaseDatabase.getInstance();
        questionsRefrence = database.getReference("questions");
        progressBar = view.findViewById(R.id.challengesProgressBar);
        recyclerView = view.findViewById(R.id.challengesRecyclerView);
        recyclerAdapter = new ChallengesAdapter(challengesList, getActivity());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
        
        spinner = view.findViewById(R.id.subjectsSpinnerInChallengesFragment);
        final ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.subjects_array, android.R.layout.simple_spinner_item);
        subjectsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(subjectsAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSubject = adapterView.getItemAtPosition(i).toString();
                if (!list.isEmpty())
                    list.clear();
                questionsRefrence.orderByChild("subject").equalTo(currentSubject).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (list.isEmpty()) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                Question question = new Question();
                                String questionText = dataSnapshot1.child("al question").getValue().toString();
                                String answer1 = dataSnapshot1.child("answer 1").getValue().toString();
                                String answer2 = dataSnapshot1.child("answer 2").getValue().toString();
                                String answer3 = dataSnapshot1.child("answer 3").getValue().toString();
                                String answer4 = dataSnapshot1.child("answer 4").getValue().toString();
                                String correctAnswer = dataSnapshot1.child("correctAnswer").getValue().toString();
                                String writerName = dataSnapshot1.child("writerName").getValue().toString();
                                boolean reviewed = ((boolean) dataSnapshot1.child("reviewed").getValue());
                                if (reviewed) {
                                    question.setAnswer1(answer1);
                                    question.setAnswer2(answer2);
                                    question.setAnswer3(answer3);
                                    question.setAnswer4(answer4);
                                    question.setCorrectAnswer(correctAnswer);
                                    question.setWriterName(writerName);
                                    question.setAlQuestion(questionText);

                                    list.add(question);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        DatabaseReference challengesRefrence = database.getReference("challenges");
        challengesRefrence.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Challenge challenge = new Challenge();
                String challengerName = dataSnapshot.child("player1Name").getValue().toString();
                String challengeDate = dataSnapshot.child("date").getValue().toString();//TODO : solve the date problem
                String challengeImage = dataSnapshot.child("player1Image").getValue().toString();
                String challengeSubject = dataSnapshot.child("subject").getValue().toString();
                String challengeState = dataSnapshot.child("state").getValue().toString();
                long challengeScore = (long)dataSnapshot.child("player1score").getValue();
                challenge.setChallengerName(challengerName);
                challenge.setDate(challengeDate);
                challenge.setImage(challengeImage);
                challenge.setScore(challengeScore);
                challenge.setSubject(challengeSubject);
                challenge.setState(challengeState);
                challengesList.add(0,challenge);
                recyclerAdapter.notifyDataSetChanged();
                if(progressBar.getVisibility() != View.GONE)
                    progressBar.setVisibility(View.GONE);
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
                //Toast.makeText(getActivity(), "فشل تحميل البيانات من فضلك تأكد من الاتصال بالانترنت", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                Log.v("Logging", "error loading data : " + databaseError);
            }
        });
        
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
}
