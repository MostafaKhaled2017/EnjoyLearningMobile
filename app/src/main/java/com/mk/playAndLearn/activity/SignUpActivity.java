package com.mk.playAndLearn.activity;

import android.accounts.NetworkErrorException;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mk.enjoylearning.R;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    EditText etName, etEmail, etPassword, etRepassword;
    ProgressDialog progressDialog;
    FirebaseDatabase database;
    DatabaseReference myRef;
    //TODO : add image to the user signUp and the user chooses from default images
    //TODO : Add gravity right to edit text and solve the problem of gaps
    //TODO : note that email written in edit text can't be verefies
    //TODO : it's much better to make sign uo in the app with gmail and facebook only
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRepassword = findViewById(R.id.etRePassword);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");

        ButterKnife.bind(this);
    }

    @OnClick(R.id.signUpBtn)
    void signUp(View view) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("جاري التحميل");
        progressDialog.setMessage("جاري التحميل...");
        progressDialog.show();
        final String name = etName.getText().toString();
        final String email = etEmail.getText().toString();
        final String password = etPassword.getText().toString();
        String rePassword = etRepassword.getText().toString();
        //TODO : move this to the presenter
        if (TextUtils.isEmpty(name)) {
            etName.setError("هذا الحقل مطلوب");
            progressDialog.dismiss();
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("هذا الحقل مطلوب");
            progressDialog.dismiss();
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("هذا الحقل مطلوب");
            progressDialog.dismiss();
        }
        if (TextUtils.isEmpty(rePassword)) {
            etRepassword.setError("هذا الحقل مطلوب");
            progressDialog.dismiss();
        }
        if (password.length() < 6) {
            etPassword.setError("كلمة المرور يجب ألا تقل عن 6 حروف");
            progressDialog.dismiss();
        } else if (!password.equals(rePassword) && !TextUtils.isEmpty(rePassword)) {
            etRepassword.setError("تأكيد كلمة المرور مختلف عن كلمة المرور");
            progressDialog.dismiss();
        } else if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(rePassword)) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                Map<String, String> map = new HashMap<>();
                                map.put("name", name);
                                map.put("email", email);
                                //map.put("password", password);
                                myRef.push().setValue(map);
                                // Sign in success, update UI with the signed-in user's information
                                Intent i = new Intent(SignUpActivity.this, MainActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();
                            } else {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    etPassword.setError("كلمة المرور يجب أن تحتوي علي حرف ورقم علي الأقل");
                                    etPassword.requestFocus();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    Toast.makeText(SignUpActivity.this, "هذا الحساب تم استخدامه قبل ذلك",
                                            Toast.LENGTH_SHORT).show();
                                }catch (Exception e) {
                                    Toast.makeText(SignUpActivity.this, "رجاء إعادة المحاولة في وقت لاحق والتأكد من الاتصال بالانترنت",
                                            Toast.LENGTH_SHORT).show();
                                }
                                progressDialog.dismiss();
                            }

                        }
                    });
        }
    }

}
