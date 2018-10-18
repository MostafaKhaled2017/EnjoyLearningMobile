package com.mk.playAndLearn.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.ToxicBakery.viewpager.transforms.TabletTransformer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.ViewPagerAdapter;
import com.mk.playAndLearn.fragment.ChallengesFragment;
import com.mk.playAndLearn.fragment.HomeFragment;
import com.mk.playAndLearn.fragment.LearnFragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.mk.playAndLearn.utils.Firebase.auth;
import static com.mk.playAndLearn.utils.Firebase.database;
import static com.mk.playAndLearn.utils.Firebase.usersReference;
import static com.mk.playAndLearn.utils.Strings.currentUserEmail;
import static com.mk.playAndLearn.utils.Strings.currentUserImage;
import static com.mk.playAndLearn.utils.Strings.currentUserName;

public class MainActivity extends AppCompatActivity implements LearnFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener, ChallengesFragment.OnFragmentInteractionListener {
    ViewPagerAdapter adapter;
    private ViewPager mViewPager;
    TabLayout tabLayout;

    int tabPosition = 1;

    //TODO : read all the TODOs in all the app well
    //TODO : at a begging support mobiles support mobiles from 4 to 6 inches only but later support the rest
    //TODO : think about testing the app on multiple screens again
    //TODO : handle bad or no internet connection in all screens of the app specially challenge screens
    //TODO : adjust the app bars titles in all activities and fragments of the app.
    //TODO : instead of using the app bar multiple times include it to all screens and update it using java
    //TODO : note : it is better to never makes the app works in landscape mode because changing the orientation during work will cause a lot of errors and if I decided to do that then try changing the orientation in all the activities
    //TODO : think about add facebook login from the app because it will need alot of work from me and becauseif I will need to pay to use google adsens when using it but I don't think so
    //TODO : think about adding icons for sign in and sign up
    //TODO : think about adding signUp and sign in using facebook and google
    //TODO : use android arsenal and other libraries to add designs to the app
    //TODO : add text writer and other things to the general sign and sign screens because they are the first thing that the user will see
    //TODO : later determine the year of study in the name of the app
    //TODO : make sure that the app runs on tablets well and try on different screen sizes
    //TODO : handle the app rotation or make it using landscape mode only
    //TODO : handle that the app is working well when the phone is arabic and english
    //TODO : think about adding to subject "other lessons" and if selected hide the questionNo things
    //TODO : add alert dialog before open add question or add lesson warning that any bas thing added will lead to deleting the account
    //TODO : test the app well
    //TODO : Test in multiple devices
    //TODO : revise errors
    //TODO : think about decrease the size of icon in splash screen
    //TODO : think about add external sources to lessons from wikipidea and youtube for example
    //TODO : add back arrow to all activities except the main activity
    //TODO : increase learn icon size
    //TODO : upload the project to github or gitlab
    //TODO : think about revising all the code of the app and add it to methods
    //TODO : improve the style of the app after the design class in the course and before it to
    //TODO : edit fire base permissions to write in the database
    //TODO : after uploading the app think about adding that people can follow other people.
    //TODO : remove un used things from the app
    //TODO : try the app on different screen sizes
    //TODO : test all the app well after doing the TODOs and exactly before generating the signed apk
    //TODO : make users able to delete (their) posts and comments(this won't need long time)
    //TODO : in an update think about making users able to edit their posts and comment
    //TODO : in an update make a user profile page that contains his activity(challenges) and his posts in a view pager
    //TODO : push new attributes contains 2 users UIDs with each challenge and make a loop to get only 5 random question from the database
    //TODO : think about learn a specific design pattern and set the code to it step by step
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);

        mViewPager = findViewById(R.id.viewpager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), this);
        tabLayout = findViewById(R.id.tablayout);
        mViewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(mViewPager);

       /*//TODO : comment this part
        //start editing in database

        usersReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
               // DataSnapshot subjectSnapshot = dataSnapshot.child("subject");
                String id = dataSnapshot.getKey();
                usersReference.child(id).child("admin").setValue(false);

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

            }
        });


        //end editing in database*/

        mViewPager.setCurrentItem(1);//TODO : think about edit the page transformer
        mViewPager.setPageTransformer(false, new TabletTransformer());

        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setText("تعلم");
        tabOne.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.learn, 0, 0);
        tabLayout.getTabAt(0).setCustomView(tabOne);

        //TODO : make a custom tab style for small screens
        //TODO : edit this to الصفحة الرئيسية and ask my friends is it better to remove text and let images only or it is better to keep text
        //TODO : think about changing home page name to المنشورات and take my friends opinion
        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setText("الرئيسية");
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.home, 0, 0);
        tabLayout.getTabAt(1).setCustomView(tabTwo);

        TextView tabThree = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabThree.setText("العب");
        tabThree.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.challenges, 0, 0);
        tabLayout.getTabAt(2).setCustomView(tabThree);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPosition = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


            /*try {
                for (UserInfo user : mAuth.getCurrentUser().getProviderData()) {
                    if (user.getProviderId().equals("google.com")) {
                        Toast.makeText(this, "User is signed in with google , the provider data is : " + mAuth.getCurrentUser().getProviderData(), Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(this, "User is not signed in with google , the provider data is : " + mAuth.getCurrentUser().getProviderData(), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }*/
        }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addQuestion:
                startActivity(new Intent(MainActivity.this, AddQuestionActivity.class));
                return true;
            case R.id.aboutApp:
                startActivity(new Intent(MainActivity.this, ContactUsActivity.class));
                return true;
            case R.id.bestStudents:
                startActivity(new Intent(MainActivity.this, BestStudentsActivity.class));
                return true;
            case R.id.addLesson:
                startActivity(new Intent(MainActivity.this, AddLessonActivity.class));
                return true;
            /*case R.id.myAccount:
                //showHelp();
                return true;*/
            case R.id.appManagement:
                startActivity(new Intent(this, AppManagementActivity.class));
                return true;
            case R.id.editAccoutData:{
                Intent i=new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://myaccount.google.com/privacy"));
                startActivity(i);
                return true;}
            case R.id.signOut:
                auth.signOut();
                Intent i = new Intent(MainActivity.this, GeneralSignActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem appManagementItem = menu.findItem(R.id.appManagement);
        if (auth.getCurrentUser().getEmail().equals("mostafakhaled835@gmail.com")) {
            appManagementItem.setVisible(true);
        } else {
            appManagementItem.setVisible(false);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (tabPosition != 1) {
            mViewPager.setCurrentItem(1);
        } else {
            //super.onBackPressed();
            this.finishAffinity();
            // System.exit(0);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    //TODO : remove this method with all of its uses and find a better way to achieve the same thing
    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

}
