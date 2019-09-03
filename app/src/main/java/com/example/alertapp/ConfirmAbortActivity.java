package com.example.alertapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class ConfirmAbortActivity extends AppCompatActivity{
    SQLiteDatabase db;
    SharedPreferences sharedpreferences;
    EditText editText1, editText2;
    String name,pass;
    int flag;
    Button button;
    Cursor curs_sos,curs1;
    int count_wrong=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_abort);

        //open database
        db = openOrCreateDatabase("Alert", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Accelerometer(acc_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + "acc_text TEXT, acc_time DATETIME);");
        //create or open table for emergency contacts
        db.execSQL("CREATE TABLE IF NOT EXISTS Informations(user_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name VARCHAR, lastname VARCHAR, tel VARCHAR);");
        TextView textView = (TextView) findViewById (R.id.textView2);
        editText1 = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);

        sharedpreferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        button = (Button)findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {@Override
        public void onClick(View v) {
            //get from sharedpreferences password username and flag to determine whether or not to send abort sms
            name=sharedpreferences.getString("username", null);
            pass=sharedpreferences.getString("pass", null);
            flag=sharedpreferences.getInt("flag",0);
                //checks mistakes that have been made
                if (count_wrong < 3) {
                    if (editText1.getText().toString().trim().equals(name) && editText2.getText().toString().trim().equals(pass)) {
                        if(flag==1) {
                            //doesnt send sms to anyone
                            Toast.makeText(getApplicationContext(), getString(R.string.cancellation), Toast.LENGTH_LONG).show();
                        }
                        else{
                            //send abort sms to all contacts
                            curs1 = db.rawQuery("SELECT * FROM Informations", null);
                            if (curs1.getCount() != 0) {
                                while (curs1.moveToNext()) {
                                    sendSMS(curs1.getString(3), getString(R.string.cancel_the_alarm));
                                    Toast.makeText(getApplicationContext(), getString(R.string.abort_sms), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        //add event to database
                        Date currentTime = Calendar.getInstance().getTime();
                        db.execSQL("INSERT INTO Accelerometer(acc_text, acc_time) VALUES('" + false + "','" + currentTime + "')");
                        Intent intent = new Intent(getApplicationContext(), MainScreenActivity.class);
                        finish();
                        startActivity(intent);

                    } else {
                        count_wrong++;
                        Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_LONG).show();
                    }

                } else {
                    //if there are more that three mistakes sends another message to all contacts that someone is trying to access the device
                    curs_sos = db.rawQuery("SELECT * FROM Informations", null);
                    if (curs_sos.getCount() != 0) {
                        while (curs_sos.moveToNext()) {
                            String phone = curs_sos.getString(3);
                            sendSMS(phone, getString(R.string.access_phone));
                        }
                        Toast.makeText(getApplicationContext(), getString(R.string.help_soon_arrive), Toast.LENGTH_LONG).show();
                    }
                    //add event to database
                    Date currentTime = Calendar.getInstance().getTime();
                    db.execSQL("INSERT INTO Accelerometer(acc_text, acc_time) VALUES( '" + true + "','" + currentTime + "')");
                    Intent intent = new Intent(getApplicationContext(), MainScreenActivity.class);
                    startActivity(intent);
                    finish();
                }

            }

        });

    }
    //function to send sms
    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(),getString(R.string.sms_failed), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
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
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


}
