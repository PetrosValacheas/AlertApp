package com.example.alertapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;

public class AddContact extends AppCompatActivity {

    EditText editText1, editText2, editText3;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        //create or open database
        db = openOrCreateDatabase("Alert", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Informations(user_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name VARCHAR, lastname VARCHAR, tel VARCHAR);");
        //title bar name
        getSupportActionBar().setTitle(getString(R.string.add_contacts));
        //stop scrollview from autofocus
        ScrollView view = (ScrollView)findViewById(R.id.scrollView2);
        view.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        //four editText
        editText1 = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);
        editText3 = (EditText) findViewById(R.id.editText3);

    }
    //insert values to database
    public void insert(View view) {
        String edit1=editText1.getText().toString().trim();
        // String edit2=editText2.getText().toString();
         String edit3=editText3.getText().toString().trim();


        if(edit1.matches("")||edit3.matches("")){
            showmessage(getString(R.string.error),getString(R.string.fill_name_number));
        }
        else {
            db.execSQL("INSERT INTO Informations (name,lastname,tel) VALUES ('" + editText1.getText().toString().trim() + "','" + editText2.getText().toString().trim() + "','" + editText3.getText().toString().trim() + "')");
            showmessage(getString(R.string.success),getString(R.string.record_added));
            ((EditText) findViewById(R.id.editText)).getText().clear();
            ((EditText) findViewById(R.id.editText2)).getText().clear();
            ((EditText) findViewById(R.id.editText3)).getText().clear();
           //some delay to show showmessage first and then return to ContactsActivity
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(AddContact.this, ContactsActivity.class);
                    startActivity(intent);
                }
            }, 1500);
        }
    }
    //popup information window
    public void showmessage(String title,String text ){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setCancelable(true);
        builder.setMessage(text);
        builder.show();
    }
}
