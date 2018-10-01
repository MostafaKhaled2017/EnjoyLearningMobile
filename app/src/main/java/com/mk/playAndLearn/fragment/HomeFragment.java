package com.mk.playAndLearn.fragment;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.PostsAdapter;
import com.mk.playAndLearn.model.Post;

import java.util.ArrayList;

import static com.mk.playAndLearn.activity.MainActivity.addPostBtn;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    ProgressBar progressBar;

    String userName = "", userImage = "", userEmail = "";
    SharedPreferences sharedPreferences;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    EditText etAddPost;
    Button addPostButton;

    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList list = new ArrayList();
    PostsAdapter recyclerAdapter;

    RecyclerView recyclerView;
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        myRef = database.getReference("posts");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View myView = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = myView.findViewById(R.id.postsRecyclerView);
        progressBar = myView.findViewById(R.id.postsProgressBar);
        etAddPost = myView.findViewById(R.id.etAddPost);
        addPostButton = myView.findViewById(R.id.addPostBtn);
        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                if(sharedPreferences != null) {
                    if (sharedPreferences.contains("userName")){
                        userName = sharedPreferences.getString("userName", "");
                    }
                    if (sharedPreferences.contains("userImage")){
                        userImage = sharedPreferences.getString("userImage", "");
                    }
                    if (sharedPreferences.contains("userEmail")){
                        userEmail = sharedPreferences.getString("userEmail", "");
                    }
                }
                addPostBtn(myView, userName, userEmail, userImage);
            }
        });
        recyclerAdapter = new PostsAdapter(list, getActivity());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);

      /*  DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                connected = snapshot.getValue(Boolean.class);
                if (connected) {
                } else {//TODO : handle this problem or find an alternative solution
                   // Toast.makeText(getActivity(), "أنت غير متصل بالانترنت", Toast.LENGTH_SHORT).show();
                   // progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });*/
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post post = new Post();
                String postContent = dataSnapshot.child("content").getValue().toString();
                String postDate = dataSnapshot.child("date").getValue().toString();//TODO : solve the date problem
                String postWriter = dataSnapshot.child("writer").getValue().toString();
                String postImage = dataSnapshot.child("image").getValue().toString();
                String postId = dataSnapshot.getKey();
                post.setContent(postContent);
                post.setDate(postDate);
                post.setWriter(postWriter);
                post.setImage(postImage);
                post.setId(postId);
                list.add(0,post);
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
        return myView;
         }

// TODO: Rename method, update argument and hook method into UI event
public void onButtonPressed(Uri uri){
        if(mListener!=null){
        mListener.onFragmentInteraction(uri);
        }
        }

@Override
public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener){
        mListener=(OnFragmentInteractionListener)context;
        }else{
        throw new RuntimeException(context.toString()
        +" must implement OnFragmentInteractionListener");
        }
        }


@Override
public void onDetach(){
        super.onDetach();
        mListener=null;
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
