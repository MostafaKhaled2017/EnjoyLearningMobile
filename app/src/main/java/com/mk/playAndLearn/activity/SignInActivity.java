package com.mk.playAndLearn.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.mk.enjoylearning.R;

import butterknife.OnClick;

//TODO : Add gravity right to edit text and solve the roblem of gaps

public class SignInActivity extends AppCompatActivity {
    EditText etMail;
    EditText etPassword;
    FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    TextView forgotPassword;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        etMail = findViewById(R.id.etEmailSignIn);
        context = this;
        etPassword = findViewById(R.id.etPasswordSignIn);
        forgotPassword = findViewById(R.id.tvForgotPassword);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText input = new EditText(context);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

               AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setTitle("إعادة تعيين كلمة المرور")
                .setMessage("اكتب حسابك")
                .setView(input)
                .setPositiveButton("تم", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO : add functionality or change the sign way
                    }
                })
                .setNegativeButton("إلغاء", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
               alertDialog.show();
            }
        });
    }


    void signIn(View view) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("جاري التحميل");
        progressDialog.setMessage("جاري التحميل...");
        progressDialog.show();
        String email = etMail.getText().toString();
        String password = etPassword.getText().toString();

        //TODO : move this to the presenter if this is the right thing
        if (TextUtils.isEmpty(email)) {// TODO : think about changing errors text here and in the sign up
            etMail.setError("هذا الحقل مطلوب");
            progressDialog.dismiss();
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("هذا الحقل مطلوب");
            progressDialog.dismiss();
        }
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                progressDialog.dismiss();
                                Intent i = new Intent(SignInActivity.this, MainActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();

                            } else {
                                assert task.getException() != null;
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Toast.makeText(SignInActivity.this, "رجاء إعادة المحاولة في وقت لاحق والتأكد من الاتصال بالانترنت",
                                            Toast.LENGTH_SHORT).show();
                                }
                                // If sign in fails, display a message to the user.
                                progressDialog.dismiss();

                            }
                        }
                    });
        }
    }


}
