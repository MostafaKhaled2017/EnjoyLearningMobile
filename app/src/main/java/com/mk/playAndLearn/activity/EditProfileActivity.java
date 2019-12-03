package com.mk.playAndLearn.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.spinnercustom.CustomAdapter;

public class EditProfileActivity extends AppCompatActivity {

    int  selectedItem =  -1;
    Spinner  spinnercity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        spinnercity  = findViewById(R.id.spinnercity);


        seCountrySpinner(R.array.governorates);


    }


    void seCountrySpinner(int array) {

        //Change the xml array to java array
        String[] subjects = this.getResources().getStringArray(array);

        ArrayAdapter<String> customAdapter=new ArrayAdapter<String>(EditProfileActivity.this,R.layout.testactiv,subjects){


            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View v = null;
                v = super.getDropDownView(position, null, parent);
                // If this is the selected item position
                if (position == selectedItem) {
                    v.setBackgroundColor(getResources().getColor(R.color.blue_white));

                    TextView tv = (TextView) v.findViewById(R.id.textView);

                    // Set the text color of spinner item
                    tv.setTextColor(Color.WHITE);


                } else {
                    // for other views
                    v.setBackgroundColor(Color.WHITE);

                }
                return v;
            }
        };
        spinnercity.setAdapter(customAdapter);






   /* ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(this,
                array, R.layout.simple_spinner_item);
        subjectsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        subjectsSpinner.setAdapter(subjectsAdapter);*/

        spinnercity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                selectedItem  = i;

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    public void finishch(View view) {

        finish();
    }
}
