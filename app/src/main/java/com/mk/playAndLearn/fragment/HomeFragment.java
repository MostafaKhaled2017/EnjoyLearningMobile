package com.mk.playAndLearn.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.PostsAdapter;
import com.mk.playAndLearn.presenter.HomeFragmentPresenter;
import com.mk.playAndLearn.utils.OnLoadMoreListener;
import com.mk.playAndLearn.utils.WrapContentLinearLayoutManager;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.mk.playAndLearn.activity.MainActivity.deleteCache;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements HomeFragmentPresenter.View {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    ProgressBar progressBar;

    // TODO: Rename and change types of parameters

    private OnFragmentInteractionListener mListener;

    HomeFragmentPresenter presenter;

    PostsAdapter recyclerAdapter;
    TextView noPostsText, noInternetConnectionText;

    RecyclerView recyclerView;
    Spinner spinner;

    String currentSubject = "";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deleteCache(getActivity());
        presenter = new HomeFragmentPresenter(this, getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View myView = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = myView.findViewById(R.id.postsRecyclerView);
        progressBar = myView.findViewById(R.id.postsProgressBar);
        noPostsText = myView.findViewById(R.id.noPostsText);

        spinner = myView.findViewById(R.id.subjectsSpinnerInHomeFragment);

        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinner);

            // Set popupWindow height to 850px
            popupWindow.setHeight(850);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        final ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.subjects_array_with_all_subjects_item, android.R.layout.simple_spinner_item);
        subjectsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(subjectsAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSubject = adapterView.getItemAtPosition(i).toString();
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        noInternetConnectionText = myView.findViewById(R.id.noInternetConnectionText);
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryConnection();
            }
        });

        FloatingActionButton fab = myView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSpinnerDialog();
            }
        });

        return myView;

    }

    public void loadData() {
        presenter.startAsynkTask(currentSubject);
    }

    public void showSpinnerDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity());//TODO : check this
        android.view.View view = layoutInflaterAndroid.inflate(R.layout.dialog_with_spinner, null);

        final AlertDialog alertDialogBuilderUserInput = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("إلغاء", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("إضافة", null)
                .create();

        final EditText inputComment = view.findViewById(R.id.dialog_value);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        Spinner spinner = view.findViewById(R.id.subjectsSpinnerInDialog);
        dialogTitle.setText("إضافة منشور");
        inputComment.setHint("اكتب سؤالك هنا لتعرف إجابته");


        final ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.subjects_array_with_general_subjects_item, android.R.layout.simple_spinner_item);
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

                Button button = alertDialogBuilderUserInput.getButton(AlertDialog.BUTTON_NEGATIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String commentText = inputComment.getText().toString().trim();
                        if (TextUtils.isEmpty(commentText)) {
                            inputComment.setError("لا يمكنك ترك هذا الحقل فارغا");
                        } else if (currentSubject.equals("اختر المادة")) {
                            Toast.makeText(getActivity(), "قم باختيار المادة التى ينتمى لها هذا المنشور", Toast.LENGTH_SHORT).show();
                        } else {
                            presenter.addPost(commentText, currentSubject);
                            dialogInterface.dismiss();
                        }
                    }
                });
            }
        });

        alertDialogBuilderUserInput.show();

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

    @Override
    public void onResume() {
        super.onResume();
        deleteCache(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showProgressBar() {
        if (progressBar.getVisibility() != View.VISIBLE) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void retryConnection() {
        noInternetConnectionText.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        loadData();
    }

    @Override
    public void startRecyclerAdapter(final ArrayList list) {
        RecyclerView.LayoutManager layoutManager = new WrapContentLinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerAdapter = new PostsAdapter(recyclerView, list, getActivity(), this, FirebaseAuth.getInstance().getCurrentUser().getUid());

        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onNoInternetConnection() {
        progressBar.setVisibility(android.view.View.GONE);
        noInternetConnectionText.setVisibility(android.view.View.VISIBLE);
        noPostsText.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showToast(String value) {
        Toast.makeText(getActivity(), value, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notifyAdapter() {
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void setOnScrollListener(RecyclerView.OnScrollListener onScrollListener) {
        recyclerView.addOnScrollListener(onScrollListener);
    }

    @Override
    public void onDataFound() {
        hideProgressBar();
        noPostsText.setVisibility(android.view.View.GONE);
        noInternetConnectionText.setVisibility(android.view.View.GONE);
    }

    @Override
    public void hideProgressBar() {
        if (progressBar.getVisibility() != android.view.View.GONE)
            progressBar.setVisibility(android.view.View.GONE);
    }

    @Override
    public void onNoPostsExists() {
        progressBar.setVisibility(android.view.View.GONE);
        noPostsText.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    public void hideNoPostsText() {
        noPostsText.setVisibility(View.GONE);
    }
}
