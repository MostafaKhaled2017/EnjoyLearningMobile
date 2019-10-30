package com.mk.playAndLearn.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.MainViewPagerAdapter;
import com.mk.playAndLearn.fragment.ChallengesFragment;
import com.mk.playAndLearn.fragment.HomeFragment;
import com.mk.playAndLearn.fragment.LessonsFragment;
import com.mk.playAndLearn.fragment.ProfileFragment;
import com.mk.playAndLearn.service.NotificationsService;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.fireStore;
import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;
import static com.mk.playAndLearn.utils.Strings.adminEmail;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedDate;
import static com.mk.playAndLearn.utils.sharedPreference.setSavedDate;
import static com.mk.playAndLearn.utils.sharedPreference.setSavedTodayChallengesNo;
import static com.mk.playAndLearn.utils.sharedPreference.setSharedPreference;


public class MainActivity extends AppCompatActivity implements LessonsFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener,
        ChallengesFragment.OnFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {
    MainViewPagerAdapter adapter;
    BottomNavigationView navigation;

    int tabPosition = 1;
    boolean initialDataLoaded = false, isFABOpen;
    boolean transactionCalled = false;
    ArrayList list;
    String currentSubject;

    public SharedPreferences pref; // 0 - for private mode
    SharedPreferences.Editor editor;
    FirebaseAuth localAuth;
    String urlOfAppFromPlayStore = "https://play.google.com/store/apps/details?id=com.mk.playAndLearn";
    String currentVersion, latestVersion, localCurrentUserUid;
    Dialog dialog;
    FloatingActionButton mainFab, fabAddPost, fabAddChallenge;

    HomeFragment homeFragment = new HomeFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    ChallengesFragment challengesFragment = new ChallengesFragment();
    LessonsFragment lessonsFragment = new LessonsFragment();

    Intent serviceIntent;

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
        setContentView(R.layout.nav_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fabAddPost = findViewById(R.id.fabAddPost);
        fabAddChallenge = findViewById(R.id.fabAddChallenge);
        
        fabAddChallenge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ChallengeDetailsActivity.class));
            }
        });
        
        fabAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSpinnerDialog();
            }
        });

        mainFab = findViewById(R.id.floatingActionButton);
        mainFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });

        //getting bottom navigation view and attaching the listener
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            navigation.setSelectedItemId(R.id.navigation_home); // change to whichever id should be default
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        drawer.closeDrawer(Gravity.START);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

 /*       Spinner spinner = findViewById(R.id.spinnercourse);
        ArrayAdapter<CharSequence> adapterArray = ArrayAdapter.createFromResource(this,
                R.array.preparatory_subjects_array, android.R.layout.simple_spinner_item);
        adapterArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterArray);*/

        localAuth = FirebaseAuth.getInstance();

        pref = getSharedPreferences("MyPref", 0);
        editor = pref.edit();

        serviceIntent = new Intent(this, NotificationsService.class);

        adapter = new MainViewPagerAdapter(getSupportFragmentManager(), this);

        MobileAds.initialize(this, getString(R.string.ad_mob_live_id));

        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null && !initialDataLoaded) {
                    updateLastOnlineDateAndShowRewardsPage();
                    startNotificationService();
                    getCurrentVersion();
                    initialDataLoaded = true;
                }
            }
        });


          /*  try {
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

    public void showSpinnerDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(MainActivity.this);//TODO : check this
        final android.view.View view = layoutInflaterAndroid.inflate(R.layout.dialog_with_subject_spinner, null);

        final AlertDialog alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this)
                .setView(view)
                .setCancelable(false)
                .create();

        final EditText inputComment = view.findViewById(R.id.dialog_value);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        Spinner spinner = view.findViewById(R.id.subjectsSpinnerInDialog);
        dialogTitle.setText("إضافة منشور");
        inputComment.setHint("اكتب سؤالك هنا لتعرف إجابته");

        ImageView closeIcon = view.findViewById(R.id.closeIcon);
        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogBuilderUserInput.dismiss();
            }
        });

        final ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(MainActivity.this,
                R.array.preparatory_subjects_array_with_general_subjects_item, android.R.layout.simple_spinner_item);
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

        alertDialogBuilderUserInput.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialogInterface) {

                Button button = view.findViewById(R.id.addPostBtn);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String commentText = inputComment.getText().toString().trim();
                        if (TextUtils.isEmpty(commentText)) {
                            inputComment.setError("لا يمكنك ترك هذا الحقل فارغا");
                        } else if (currentSubject.equals("اختر المادة")) {
                            Toast.makeText(MainActivity.this, "قم باختيار المادة التى ينتمى لها هذا المنشور", Toast.LENGTH_SHORT).show();
                        } else {
                            if (homeFragment != null) {
                               homeFragment.presenter.addPost(commentText, currentSubject);
                            } else {
                                Toast.makeText(MainActivity.this, "فشلت إضافة المنشور برجاء المحاولة لاحقا", Toast.LENGTH_SHORT).show();
                            }
                            dialogInterface.dismiss();
                        }
                    }
                });
            }
        });

        alertDialogBuilderUserInput.show();

    }

    private void showFABMenu(){
        isFABOpen=true;
        fabAddPost.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fabAddChallenge.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        fabAddChallenge.animate().translationY(0);
        fabAddPost.animate().translationY(0);
    }

    @Override
    public void onBackPressed() {
        if(!isFABOpen){
            super.onBackPressed();
        }else{
            closeFABMenu();
        }
        navigation.setSelectedItemId(R.id.navigation_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        transactionCalled = false;
    }

    /*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }*/


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int menuGroupId = item.getGroupId();

        if (menuGroupId == R.id.main_menu_group) {
            // Handle navigation view item clicks here.
            switch (item.getItemId()) {
                case R.id.addQuestion:
                    startActivity(new Intent(MainActivity.this, AddQuestionActivity.class));
                    return true;
                case R.id.addLesson:
                    startActivity(new Intent(MainActivity.this, AddLessonActivity.class));
                    return true;
                case R.id.profile:
                    startActivity(new Intent(MainActivity.this, ProfileFragment.class));
                    return true;
                case R.id.contactUs:
                    startActivity(new Intent(MainActivity.this, ContactUsActivity.class));
                    return true;
                case R.id.leaderboard:
                    startActivity(new Intent(MainActivity.this, LeaderBoardActivity.class));
                    return true;
            /*case R.id.myAccount:
                //showHelp();
                return true;*/
                case R.id.appManagement:
                    startActivity(new Intent(this, AdminAppManagementActivity.class));
                    return true;
                case R.id.studyTips:
                    startActivity(new Intent(this, StudyTipsActivity.class));
                    return true;

                case R.id.generalChallenges:
                    startActivity(new Intent(this, GeneralChallengesActivity.class));
                    return true;

                case R.id.bestStudentsInGeneralChallenge:
                    startActivity(new Intent(this, BestStudentsInGeneralChallengeActivity.class));
                    return true;

                case R.id.bestStudentsInCompetition:
                    startActivity(new Intent(this, BestStudentsInCompetitionActivity.class));
                    return true;

                case R.id.chatBot:
                    startActivity(new Intent(this, ChatbotActivity.class));
                    return true;
            /*case R.id.editAccoutData:{
                Intent i=new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://myaccount.google.com/privacy"));
                startActivity(i);
                return true;}*/
                case R.id.signOut:
                    stopNotificationService();
                    setSharedPreference(this, null, null, null, null, null, null, null, -1, -1, null, null);
                    Intent i = new Intent(MainActivity.this, GeneralSignActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } else if (menuGroupId == R.id.navigation_menu_group) {
            Fragment fragment = new Fragment();

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = homeFragment;
                    break;
                case R.id.navigation_profile:
                    fragment = profileFragment;
                    break;
                case R.id.navigation_Challenges:
                    fragment = challengesFragment;
                    break;
                case R.id.navigation_Lessons:
                    fragment = lessonsFragment;
                    break;
            }

            return loadFragment(fragment);
        }

        return false;
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }


  /*  @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem appManagementItem = menu.findItem(R.id.appManagement);
        final MenuItem chatBotItem = menu.findItem(R.id.chatBot);

        localAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = localAuth.getCurrentUser();
                if (user != null) {
                    if (localAuth.getCurrentUser().getEmail().equals(adminEmail)) {
                        appManagementItem.setVisible(true);
                        chatBotItem.setVisible(true);
                    } else {
                        appManagementItem.setVisible(false);
                        chatBotItem.setVisible(false);
                    }
                }
            }
        });
        return true; TODO : Re add this
    }*/


    private void getCurrentVersion() {
        String localCurrentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Log.v("appVersionValidation", "getCurrentVersion called");
        PackageManager pm = this.getPackageManager();
        PackageInfo pInfo = null;

        try {
            pInfo = pm.getPackageInfo(this.getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        currentVersion = pInfo.versionName;

        new GetLatestVersion().execute();

    }

    //TODO : remove this method with all of its uses and find a better way to achieve the same thing

    public void updateLastOnlineDateAndShowRewardsPage() {
        localAuth = FirebaseAuth.getInstance();
        localCurrentUserUid = localAuth.getCurrentUser().getUid();

        Calendar calendar = Calendar.getInstance();
        // Move calendar to yesterday
        calendar.add(Calendar.DATE, -1);

        // Get current date of calendar which point to the yesterday now
        Date yesterday = calendar.getTime();

        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT+2"));

        final String todayDate = format.format(today);
        final String yesterdayDate = format.format(yesterday);
       final String oldSavedDate = getSavedDate(this);

        Log.v("dateLogging", "todayDate : " + todayDate
                + " , yesterdayDate : " + yesterdayDate
                + " , oldSavedDate : " + oldSavedDate);

        //update the saved date after storing it in a string
        setSavedDate(this, todayDate);

        final DocumentReference currentUserReference = fireStoreUsers.document(localCurrentUserUid);

        if (!todayDate.equals(oldSavedDate)) {
            if (oldSavedDate.equals(yesterdayDate)) {
                fireStore.runTransaction(new Transaction.Function<Long>() {
                    @Nullable
                    @Override
                    public Long apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot currentUserSnapshot = transaction.get(currentUserReference);

                        long newConsecutiveDays = currentUserSnapshot.getLong("consecutiveDays") + 1;

                        transaction.update(currentUserReference, "consecutiveDays", newConsecutiveDays);
                        transaction.update(currentUserReference, "lastOnlineDay", todayDate);

                        transaction.update(currentUserReference, "todayChallengesNo", 0);
                        transaction.update(currentUserReference, "todayUploadedQuestionNo", 0);

                        return newConsecutiveDays;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Long>() {
                    @Override
                    public void onSuccess(Long aLong) {
                        Log.v("transactionLogging", "transaction 1 called");
                        if(!transactionCalled) {
                            setSavedTodayChallengesNo(MainActivity.this, 0);
                            Intent i = new Intent(MainActivity.this, DailyRewardsActivity.class);
                            i.putExtra("consecutiveDays", aLong);
                            i.putExtra("userUid", localCurrentUserUid);
                            startActivity(i);
                            transactionCalled = true;
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MainActivity", "Transaction failure.", e);
                    }
                });
            } else {
                WriteBatch batch = fireStore.batch();
                batch.update(currentUserReference, "lastOnlineDay", todayDate);
                batch.update(currentUserReference, "consecutiveDays", 1);

                batch.update(currentUserReference, "todayChallengesNo", 0);
                batch.update(currentUserReference, "todayUploadedQuestionNo", 0);
                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!transactionCalled){
                        Intent i = new Intent(MainActivity.this, DailyRewardsActivity.class);
                        setSavedTodayChallengesNo(MainActivity.this, 0);
                        i.putExtra("consecutiveDays", (long) 1);
                        i.putExtra("userUid", localCurrentUserUid);
                        startActivity(i);
                        transactionCalled = true;
                    }
                        Log.v("transactionLogging", "transaction 2 called");
                    }
                });
            }
        }
    }

    public void startNotificationService() {
        //TODO : add timer if needed
        //TODO : remove this method and use firebase function instead
        Log.v("notificationsDebug", "Service started");

        startService(serviceIntent);
    }

    public void stopNotificationService() {
        //TODO : add timer if needed
        //TODO : remove this method and use firebase function instead
        Log.v("notificationsDebug", "Service stopped");
        stopService(serviceIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private class GetLatestVersion extends AsyncTask<String, String, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            Log.v("appVersionValidation", "getLatestVersion : doInBackGround");
            try {
//It retrieves the latest version by scraping the content of current version from play store at runtime
                Document doc = Jsoup.connect(urlOfAppFromPlayStore).get();
                latestVersion = doc.getElementsByClass("htlgb").get(6).text();
                Log.v("appVersionValidation", "try, latestVersion is : " + latestVersion);

            } catch (Exception e) {
                e.printStackTrace();
                Log.v("appVersionValidation", "catch, exception is : " + e.toString());

            }

            return new JSONObject();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (latestVersion != null) {
                String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                Log.v("appVersionValidation", "onPostExcute , currentVersion is : " + currentVersion
                        + " , latestVersion is : " + latestVersion
                        + " , currentUserEmail : " + currentUserEmail
                        + " , adminEmail : " + adminEmail);
                if (!currentVersion.equalsIgnoreCase(latestVersion) && !currentUserEmail.equals(adminEmail)) {
                    if (!isFinishing()) {//This would help to prevent Error : BinderProxy@45d459c0 is not valid; is your activity running? error
                        showUpdateDialog();
                    }
                }
            }

            super.onPostExecute(jsonObject);
        }
    }


    private void showUpdateDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("يوجد تحديث جديد للتطبيق")
                .setMessage("يجب عليك تحديث التطبيق حتى تستطيع استخدامه")
                .setPositiveButton("تحديث", null)
                .setCancelable(false)
                .create();


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.mk.playAndLearn")));
                    }
                });
            }
        });

        dialog.show();
    }
}
