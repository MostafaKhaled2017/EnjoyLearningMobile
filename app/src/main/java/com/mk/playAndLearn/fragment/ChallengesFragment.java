package com.mk.playAndLearn.fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.ChallengersActivity;
import com.mk.playAndLearn.activity.MainActivity;
import com.mk.playAndLearn.adapters.ChallengesAdapter;
import com.mk.playAndLearn.model.Challenge;
import com.mk.playAndLearn.model.Question;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
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
    //TODO : think about adding  if the challenger has won or lose or drawn

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
    MainActivity mainActivity;

    FirebaseDatabase database;
    ArrayList<Challenge> completedChallengesList = new ArrayList<>(), uncompletedChallengesList = new ArrayList<>();
    String currentSubject, currentUserUid;
    int currentPlayer, previousCompetedChallengeListSize = -1, previousUnCompetedChallengeListSize = -1;
    boolean initialDataLoaded = false;

    Spinner spinner;
    ProgressBar progressBar;
    DatabaseReference challengesReference;

    ChallengesAdapter completedChallengeRecyclerAdapter, uncompletedChallengeRecyclerAdapter;

    RecyclerView completedChallengesRecyclerView, uncompletedChallengesRecyclerView;
    TextView completeChallengesTv, uncompletedChallengesTv, loadingTv, noInternetConnectionText;

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
        challengesReference = database.getReference("challenges");
        currentUserUid = auth.getCurrentUser().getUid();
        mainActivity = new MainActivity();

        progressBar = view.findViewById(R.id.challengesProgressBar);
        loadingTv = view.findViewById(R.id.loadingText);

        noInternetConnectionText = view.findViewById(R.id.noInternetConnectionText);
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noInternetConnectionText.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                startAsynkTask();
            }
        });

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

    public void getChallengeData(DataSnapshot dataSnapshot){
        GenericTypeIndicator<List<Question>> t = new GenericTypeIndicator<List<Question>>() {
        };
        Challenge challenge = new Challenge();
        String challengeDate = dataSnapshot.child("date").getValue().toString();
        String challengeSubject = dataSnapshot.child("subject").getValue().toString();
        String challengeState = dataSnapshot.child("state").getValue().toString();
        String challengeId = dataSnapshot.getKey();
        ArrayList challengeQuestionsList = (ArrayList) dataSnapshot.child("questionsList").getValue(t);
        long player1Score = (long) dataSnapshot.child("player1score").getValue();
        long player2Score = (long) dataSnapshot.child("player2score").getValue();

        String player1Name = dataSnapshot.child("player1Name").getValue().toString();
        String player1Image = dataSnapshot.child("player1Image").getValue().toString();
        String player1Uid = dataSnapshot.child("player1Uid").getValue().toString();
        String player2Name = dataSnapshot.child("player2Name").getValue().toString();
        String player2Image = dataSnapshot.child("player2Image").getValue().toString();
        String player2Uid = dataSnapshot.child("player2Uid").getValue().toString();

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


        if (initialDataLoaded && previousCompetedChallengeListSize < completedChallengesList.size() && currentPlayer == 1) {
            //TODO : think about changing the text
            ((MainActivity) getActivity()).showNotification("اكتمل التحدى", "لديك تحدي مكتمل جديد");
        }

        if (initialDataLoaded && previousUnCompetedChallengeListSize < uncompletedChallengesList.size() && currentPlayer == 2) {
            //TODO : think about changing the text
            ((MainActivity) getActivity()).showNotification("لديك تحدى", "لديك تحدي جديد");
        }

        previousCompetedChallengeListSize = completedChallengesList.size();
        previousUnCompetedChallengeListSize = uncompletedChallengesList.size();
    }

    public void startAsynkTask(){
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
                    //this code gives data where current user is player 1
                    challengesReference.orderByChild("player1Uid").equalTo(currentUserUid).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            getChallengeData(dataSnapshot);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            //TODO : note that the only changing handled is when challenge moves from uncompleted to completed state
                            getChallengeData(dataSnapshot);
                            for (int i = 0; i < uncompletedChallengesList.size(); i++) {
                                if (uncompletedChallengesList.get(i).getId().equals(dataSnapshot.getKey())) {
                                    uncompletedChallengesList.remove(i);
                                    uncompletedChallengeRecyclerAdapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                            if(uncompletedChallengesList.size() == 0){
                                uncompletedChallengesTv.setVisibility(View.GONE);
                            }
                            if(completedChallengesList.size() == 0){
                                completeChallengesTv.setVisibility(View.GONE);
                            }
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

                    //this code gives data where current user is player 2
                    challengesReference.orderByChild("player2Uid").equalTo(currentUserUid).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            getChallengeData(dataSnapshot);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            //TODO : note that the only changing handled is when challenge moves from uncompleted to completed state
                            getChallengeData(dataSnapshot);
                            for (int i = 0; i < uncompletedChallengesList.size(); i++) {
                                if (uncompletedChallengesList.get(i).getId().equals(dataSnapshot.getKey())) {
                                    uncompletedChallengesList.remove(i);
                                    uncompletedChallengeRecyclerAdapter.notifyDataSetChanged();
                                    break;
                                }

                            }
                            if(uncompletedChallengesList.size() == 0){
                                uncompletedChallengesTv.setVisibility(View.GONE);
                            }
                            if(completedChallengesList.size() == 0){
                                completeChallengesTv.setVisibility(View.GONE);
                            }
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


                    challengesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            initialDataLoaded = true;
                            if (progressBar.getVisibility() != View.GONE)
                                progressBar.setVisibility(View.GONE);
                            if (completedChallengesList.size() > 0) {
                                completeChallengesTv.setVisibility(View.VISIBLE);
                            } else {
                                completeChallengesTv.setVisibility(View.GONE);
                            }

                            if (uncompletedChallengesList.size() > 0) {
                                uncompletedChallengesTv.setVisibility(View.VISIBLE);
                            } else {
                                uncompletedChallengesTv.setVisibility(View.GONE);
                            }
                            if (completedChallengesList.size() == 0 && uncompletedChallengesList.size() == 0)
                                loadingTv.setVisibility(View.VISIBLE);
                            else
                                loadingTv.setVisibility(View.GONE);

                            previousCompetedChallengeListSize = completedChallengesList.size();
                            previousUnCompetedChallengeListSize = uncompletedChallengesList.size();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    progressBar.setVisibility(View.GONE);
                    noInternetConnectionText.setVisibility(View.VISIBLE);
                }
            }
        };

        asyncTask.execute();
    }

}
