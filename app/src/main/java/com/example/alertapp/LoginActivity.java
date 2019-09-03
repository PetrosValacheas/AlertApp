package com.example.alertapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    SharedPreferences sharedpreferences;
    EditText editText1, editText2;
    Toast toast;
    String name,pass;
    TextView textView;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //sets textviews and buttons
        TextView textView = (TextView) findViewById (R.id.textView2);
        editText1 = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);
        button = (Button)findViewById(R.id.button2);
        sharedpreferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
       //checks if username and password exist
        if(sharedpreferences.contains("username") && sharedpreferences.contains("pass")){
            textView.setText(getString(R.string.verify));
            button.setText(getString(R.string.login));
        }
        else{
            //if username and password havent been given yet then the user must register
            textView.setText(getString(R.string.register_text));
            button.setText(getString(R.string.register));
        }

           editText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(editText2.getText().toString().contains(" ")){
                    //you cant put space in password
                    editText2.setText(editText2.getText().toString().replaceAll(" " , ""));
                    }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {@Override
        public void onClick(View v) {
            //checks name and passoword if exist and are correct
            if(sharedpreferences.contains("username") && sharedpreferences.contains("pass")){
                name=sharedpreferences.getString("username", null);
                pass=sharedpreferences.getString("pass", null);
                if(editText1.getText().toString().trim().equals(name) && editText2.getText().toString().trim().equals(pass)){
                    go_to_activity();
                }
                else{
                    Toast.makeText(getApplicationContext(),getString(R.string.try_again), Toast.LENGTH_LONG).show();
                }
            }
            else{
                if(editText1.getText().toString().matches("") || editText2.getText().toString().matches("")  ){
                     Toast.makeText(getApplicationContext(),getString(R.string.fill), Toast.LENGTH_LONG).show();
                }
                else{
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("username", editText1.getText().toString().trim());
                    editor.commit();
                    editor.putString("pass", editText2.getText().toString().trim());
                    editor.commit();
                    go_to_activity();
                }

            }
        }
        });

    }

    @Override
    public void onResume(){
        super.onResume();

        //----------checkbox:checks if checkbox is checked or not and hide/shows password------------
        CheckBox check = findViewById(R.id.checkBox);
        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {
                    editText2.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    editText2.setInputType(129);
                }

            }
        });

    }


    public void go_to_activity(){
        Intent intent = new Intent(this, MainScreenActivity.class);
        startActivity(intent);
        finish();
    }
}
