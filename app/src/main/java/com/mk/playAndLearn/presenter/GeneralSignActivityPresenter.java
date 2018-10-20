package com.mk.playAndLearn.presenter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.GeneralSignActivity;
import com.mk.playAndLearn.activity.MainActivity;
import com.mk.playAndLearn.utils.Firebase;

import java.util.HashMap;
import java.util.Map;

public class GeneralSignActivityPresenter {
    View view;
    private final static int RC_SIGN_IN = 2;

    //TODO : try to find a way to edit this use
    FirebaseUser currentUser;

    GoogleApiClient  mGoogleApiClient;

    public GeneralSignActivityPresenter(View view){
        this.view = view;
    }

    public void signInWithGoogle(String requestIdToken) {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(requestIdToken)
                .requestEmail()
                .build();
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected()){
             mGoogleApiClient = new GoogleApiClient.Builder((Context) view)//TODO : check this context, TODO : check why this variable isn't used
                .enableAutoManage((FragmentActivity) view, new GoogleApiClient.OnConnectionFailedListener() {//TODO : check this casting
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        view.onGoogleConnectionFailed();
                        Log.v("Logging", "connection result is : " + connectionResult.toString());
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        }
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient((Context) view, gso);//TODO : check this context

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        view.startSignIntent(signInIntent);
    }

    public void navigate(final String userType) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserUid = currentUser.getUid();
        final DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("users");
        final DatabaseReference currentUserReference =  usersReference.child(currentUserUid);
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(currentUser.getUid()).exists()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userName", currentUser.getDisplayName());
                    map.put("userImage", currentUser.getPhotoUrl());
                    map.put("userEmail", currentUser.getEmail());
                    map.put("points", 0);
                    map.put("acceptedQuestions", 0);
                    map.put("refusedQuestions", 0);
                    map.put("acceptedLessons", 0);
                    map.put("refusedLessons", 0);
                    map.put("userType", userType);
                    usersReference.child(currentUser.getUid()).setValue(map);
                }
                else {
                    
                }
                view.goToMainActivity();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkCurrentUser(){
        if (currentUser != null) {
           view.goToMainActivity();
        }
    }

    public interface View {
        void onGoogleConnectionFailed();

        void goToMainActivity();

        void startSignIntent(Intent signInIntent);
    }
}
