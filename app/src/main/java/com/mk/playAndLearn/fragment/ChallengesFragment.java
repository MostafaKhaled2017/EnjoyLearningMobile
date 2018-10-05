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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.ChallengersActivity;
import com.mk.playAndLearn.adapters.ChallengesAdapter;
import com.mk.playAndLearn.model.Challenge;
import com.mk.playAndLearn.model.Question;

import java.util.ArrayList;
import java.util.List;

import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;
import static com.mk.playAndLearn.utils.Strings.uncompletedChallengeText;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChallengesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChallengesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChallengesFragment extends Fragment {

    //TODO : handle when fire base failed to get data
    //TODO : handle when the second player doesn't complete the challenge or connection problem happens to him
    //TODO : ensure that the two users of the challenge solve the same questions

    // TODO : Rename parameter arguments, choose names that match
    // TODO : think about making when an image clicked it opens in a full activity like other programs
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    View view;
    Button startChallengeButton;
    FirebaseAuth auth;

    FirebaseDatabase database;
    ArrayList<Challenge> completedChallengesList = new ArrayList<>(), uncompletedChallengesList = new ArrayList<>();
    String currentSubject, currentUserUid;
    int currentPlayer;

    Spinner spinner;
    ProgressBar progressBar;

    ChallengesAdapter completedChallengeRecyclerAdapter, uncompletedChallengeRecyclerAdapter;

    RecyclerView completedChallengesRecyclerView, uncompletedChallengesRecyclerView;
    TextView completeChallengesTv, uncompletedChallengesTv, loadingTv;

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
        auth = FirebaseAuth.getInstance();
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
                Intent i = new Intent(getActivity(), ChallengersActivity.class);
                i.putExtra("subject", currentSubject);
                startActivity(i);
            }
        });

        database = FirebaseDatabase.getInstance();
        currentUserUid = auth.getCurrentUser().getUid();

        progressBar = view.findViewById(R.id.challengesProgressBar);
        loadingTv = view.findViewById(R.id.loadingText);
        completeChallengesTv = view.findViewById(R.id.completedChallengesText);
        uncompletedChallengesTv = view.findViewById(R.id.uncompletedChallengesText);
        completedChallengesRecyclerView = view.findViewById(R.id.completedChallengesRecyclerView);
        uncompletedChallengesRecyclerView = view.findViewById(R.id.uncompletedChallengesRecyclerView);
        completedChallengeRecyclerAdapter = new ChallengesAdapter(completedChallengesList, getActivity());
        uncompletedChallengeRecyclerAdapter = new ChallengesAdapter(uncompletedChallengesList, getActivity());
        RecyclerView.LayoutManager completedChallengesLayoutManager = new LinearLayoutManager(getActivity());
        RecyclerView.LayoutManager uncompletedChallengesLayoutManager = new LinearLayoutManager(getActivity());
        completedChallengesRecyclerView.setLayoutManager(completedChallengesLayoutManager);
        completedChallengesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        completedChallengesRecyclerView.setAdapter(completedChallengeRecyclerAdapter);
        uncompletedChallengesRecyclerView.setLayoutManager(uncompletedChallengesLayoutManager);
        uncompletedChallengesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        uncompletedChallengesRecyclerView.setAdapter(uncompletedChallengeRecyclerAdapter);

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
        DatabaseReference challengesReference = database.getReference("challenges");
        challengesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!completedChallengesList.isEmpty()) {
                    completedChallengesList.clear();
                    completedChallengeRecyclerAdapter.notifyDataSetChanged();
                }
                if (!uncompletedChallengesList.isEmpty()) {
                    uncompletedChallengesList.clear();
                    uncompletedChallengeRecyclerAdapter.notifyDataSetChanged();
                }
                if(dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        GenericTypeIndicator<List<Question>> t = new GenericTypeIndicator<List<Question>>() {
                        };
                        Challenge challenge = new Challenge();
                        String challengeDate = dataSnapshot1.child("date").getValue().toString();//TODO : solve the date problem
                        String challengeSubject = dataSnapshot1.child("subject").getValue().toString();
                        String challengeState = dataSnapshot1.child("state").getValue().toString();
                        String challengeId = dataSnapshot1.getKey();
                        ArrayList challengeQuestionsList = (ArrayList) dataSnapshot1.child("questionsList").getValue(t);
                        long player1Score = (long) dataSnapshot1.child("player1score").getValue();
                        long player2Score = (long) dataSnapshot1.child("player2score").getValue();

                        String player1Name = dataSnapshot1.child("player1Name").getValue().toString();
                        String player1Image = dataSnapshot1.child("player1Image").getValue().toString();
                        String player1Uid = dataSnapshot1.child("player1Uid").getValue().toString();
                        String player2Name = dataSnapshot1.child("player2Name").getValue().toString();
                        String player2Image = dataSnapshot1.child("player2Image").getValue().toString();
                        String player2Uid = dataSnapshot1.child("player2Uid").getValue().toString();

                        String challengerName, challengerImage;

                        if (player1Uid.equals(currentUserUid)) {
                            currentPlayer = 1;
                            challengerName = player2Name;
                            challengerImage = player2Image;
                            challenge.setSecondChallengerUid(player2Uid);//second means that it is not the player who starts the challenge

                        } else {
                            currentPlayer = 2;
                            challengerName = player1Name;
                            challengerImage = player1Image;
                            challenge.setSecondChallengerUid(player1Uid);//second means that it is not the player who starts the challenge
                        }
                        challenge.setCurrentPlayer(currentPlayer);
                        challenge.setChallengerName(challengerName);
                        challenge.setDate(challengeDate);
                        challenge.setImage(challengerImage);
                        challenge.setSubject(challengeSubject);
                        challenge.setState(challengeState);
                        challenge.setId(challengeId);
                        challenge.setQuestionsList(challengeQuestionsList);
                        //TODO : find a better way to do that as this will load all the data and this will take time
                        if (player1Uid.equals(currentUserUid) || player2Uid.equals(currentUserUid)) {
                            String score;
                            if (currentPlayer == 1) {
                                score = player2Score + " : " + player1Score;
                            } else {
                                score = player1Score + " : " + player2Score;
                            }
                            challenge.setScore(score);
                            if (challenge.getState().equals("اكتمل")) {
                                completedChallengesList.add(0, challenge);
                                completedChallengeRecyclerAdapter.notifyDataSetChanged();
                            } else if (challenge.getState().equals(refusedChallengeText)) {
                                completedChallengesList.add(0, challenge);
                                completedChallengeRecyclerAdapter.notifyDataSetChanged();
                            } else if (challenge.getState().equals(uncompletedChallengeText)) {
                                uncompletedChallengesTv.setVisibility(View.VISIBLE);
                                uncompletedChallengesList.add(0, challenge);
                                uncompletedChallengeRecyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
                if (progressBar.getVisibility() != View.GONE)
                    progressBar.setVisibility(View.GONE);
                if (completedChallengesList.size() > 0) {
                    completeChallengesTv.setVisibility(View.VISIBLE);
                }
                else {
                    completeChallengesTv.setVisibility(View.GONE);
                }

                if (uncompletedChallengesList.size() > 0) {
                    uncompletedChallengesTv.setVisibility(View.VISIBLE);
                }
                else {
                    uncompletedChallengesTv.setVisibility(View.GONE);
                }
                if(completedChallengesList.size() == 0 && uncompletedChallengesList.size() == 0)
                    loadingTv.setVisibility(View.VISIBLE);
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
