package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mk.enjoylearning.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class GeneralSignActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_sign);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
    }

    @OnClick(R.id.btnSignUp)
    void click(View view){
        Intent i = new Intent(this,SignUpActivity.class);
        startActivity(i);
    }
    @OnClick(R.id.btnSignIn)
    void click2(View view){
        Intent i = new Intent(this,SignInActivity.class);
        startActivity(i);
    }
    @Override
    public void onStart() {
        super.onStart();//TODO
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, MainActivity.class));
        }
        // updateUI(currentUser);
    }

}
