package com.mk.playAndLearn.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mk.enjoylearning.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnboardingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OnboardingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OnboardingFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    TextView titleTv, topicTv;
    int position;
    //Test Comment

    final String[] titles = {
            "تدرب على الدروس بطريقة تفاعلية"
            ,"راجع علي الدروس القديمة باستمرار"
            , "قم بجمع أكبر قدر من النقاط"
            , "تعلم ونافس لتحصل علي جوائز قيمة"
            , "قم بفتح التطبيق يوميا"
            , "شارك فى مسابقة سفراء البرنامج"
            , "لا تتردد فى التواصل معنا"
    };
    final String[] topics = {
            "بدلا من طرق الحل التقليدية قم بالتدرب علي الدروس بدخول تحدي جديد من أيكونة (+) في الدرس الذي تريده ضد طالب اخر فى البرنامج"
            ,"قم بدخول تحديات فى الدروس القديمة فى منهجك باستمرار لتتذكرها بسهولة أثناء المراجعة قبل الامتحانات"
            , "عند كل تحدي تفوز به أو تتعادل فيه وعلي كل صديق تقوم بدعوته للتطبيق تحصل علي نقاط(XP)"
            , "فى نهاية العام الدراسى يكون هناك جائزة لأفضل الطلبة فى التطبيق الذين حصلوا علي أكبر عدد من النقاط"
            , "فى كل يوم جديد تفتح به التطبيق تحصل على نقاط(XP) ويزداد عدد هذه النقاط تدريجيا عند استخدامك للتطبيق يوميا دون انقطاع"
            , "تواصل معنا عن طريق البريد الالكتروني للاشتراك فى مسابقتنا وليكون لك فرصة لربح احد جوائزنا فى نهاية العام الدراسى"
            , "إذا واجهتك أي مشاكل أو شئ غير مفهوم وتريد معرفته قم بسؤالنا باستخدام البيانات الموجودة فى صفحة تواصل معنا"
    };

    public OnboardingFragment(int position) {
        this.position = position;
    }
    public OnboardingFragment(){

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OnboardingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OnboardingFragment newInstance(String param1, String param2) {
        OnboardingFragment fragment = new OnboardingFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding, container, false);
        titleTv = view.findViewById(R.id.titleText);
        topicTv = view.findViewById(R.id.topicText);

        titleTv.setText(titles[position]);
        topicTv.setText(topics[position]);

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

    public void setTile(String title){
        titleTv.setText(title);
    }

    public void setTopic(String topic){
        topicTv.setText(topic);
    }
}
