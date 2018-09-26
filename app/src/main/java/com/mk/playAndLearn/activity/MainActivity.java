package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.ToxicBakery.viewpager.transforms.TabletTransformer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.ViewPagerAdapter;
import com.mk.playAndLearn.fragment.ChallengesFragment;
import com.mk.playAndLearn.fragment.HomeFragment;
import com.mk.playAndLearn.fragment.LearnFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LearnFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener, ChallengesFragment.OnFragmentInteractionListener{
    ViewPagerAdapter adapter;
    private ViewPager mViewPager;
    TabLayout tabLayout;
    int tabPosition = 1;
    FirebaseAuth mAuth;
    static DatabaseReference myRef;
    FirebaseDatabase database;
    ///////////////TODO : think about removing facebook login from the app because if I will need to pay to use google adsens when using it but I don't think so
    //TODO : think about adding icons for sign in and sign up
    //TODO : think about adding signUp and sign in using facebook and google
    //TODO : use android arsenal and other libraries to add designs to the app
    //TODO : add text writer and other things to the general sign and sign screens because they are the first thing that the user will see
    //TODO : later determine the year of study in the name of the app
    //TODO : make sure that the app runs on tablets well and try on different screen sizes
    //TODO : handle the app rotation or make it using landscape mode only
    //TODO : handle that the app is working well when the phone is arabic and english
    //TODO : think about adding to subject "other lessons" and if selected hide the position things
    //TODO : add alert dialog before open add question or add lesson warning that any bas thing added will lead to deleting the account
    //TODO : test the app well
    //TODO : Test in multiple devices
    //TODO : revise errors
    //TODO : think about decrease the size of icon in splash screen
    //TODO : think about add external sources to lessons from wikipidea and youtube for example
    //TODO : add back arrow to all activities except the main activity
    //TODO : increase learn icon size
    //TODO : upload the project to github or gitlab
    //TODO : improve the style of the app after the design class in the course and before it to
    //TODO : edit firebase permissions to write in the database
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("posts");

        mViewPager = findViewById(R.id.viewpager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager(),this);
        tabLayout = findViewById(R.id.tablayout);
        mViewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.setCurrentItem(1);//TODO : think about edit the page transformer
        mViewPager.setPageTransformer(false, new TabletTransformer());

        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setText("تعلم");
        tabOne.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.learn, 0, 0);
        tabLayout.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setText("الصفحة الرئيسية");
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
                //showHelp();
                return true;
            case R.id.bestStudents:
                //showHelp();
                return true;
                case R.id.addLesson:startActivity(new Intent(MainActivity.this, AddLessonActivity.class)                  );
                return true;
            case R.id.myAccount:
                //showHelp();
                return true;
            case R.id.signOut:
                mAuth.signOut();
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
    public void onBackPressed() {
        super.onBackPressed();
        if (tabPosition != 1) {
            mViewPager.setCurrentItem(1);
        } else {
            this.finishAffinity();
            System.exit(0);
        }
    }





    // home fragment button
   public static void addPostBtn(View view){
        EditText etAddPost = view.findViewById(R.id.etAddPost);
        String postText = etAddPost.getText().toString();
       Date today = new Date();
       SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());//TODO : check that the date changes at 12 p.m exactly
       String date = format.format(today);
        if(TextUtils.isEmpty(postText)){
            etAddPost.setError("لا يمكنك ترك هذا الحقل فارغا");
        }
        else {
            Map<String, String> map = new HashMap<>();
            map.put("postContent", postText);//Todo : Add post date and post witer
            map.put("postDate", date);//Todo : Add post image and post writer
            map.put("postWriter", "Mostafa Khaled");
            myRef.push().setValue(map);
            etAddPost.setText("");
            Toast.makeText(view.getContext(), "تم إضافة المنشور بنجاح", Toast.LENGTH_SHORT).show();

        }
    }
    //end of home fragment buttons

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
