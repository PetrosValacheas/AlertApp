package com.example.alertapp;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class EditDeleteActivity extends AppCompatActivity {
    SQLiteDatabase db;
    EditText Text,Text2,Text3;
    int id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_delete);
        //title bar name
        getSupportActionBar().setTitle(getString(R.string.save_or_delete));
        //stop scrollview from autofocus
        ScrollView view = (ScrollView)findViewById(R.id.scrollView);
        view.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
    }

    @Override
    public void onResume(){
        super.onResume();
        //create or open database
        db = openOrCreateDatabase("Alert", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Informations(user_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name VARCHAR, lastname VARCHAR, tel VARCHAR);");
        //retrive id that AddContact has send
        String my_id=getIntent().getStringExtra("id");
        id = Integer.valueOf(my_id);
        //find common button-row id
        Cursor cursor =db.rawQuery("SELECT * FROM Informations WHERE user_id='"+id+"'",null);
        //four editText
        Text = (EditText)findViewById(R.id.Text);
        Text2 = (EditText)findViewById(R.id.Text2);
        Text3 = (EditText)findViewById(R.id.Text3);

        //sets each editText value to each one of the columns of the specific database row
        if(cursor.getCount() >= 1){
            while(cursor.moveToNext()) {
                Text.setText(cursor.getString(1), TextView.BufferType.EDITABLE);
                Text2.setText(cursor.getString(2),TextView.BufferType.EDITABLE);
                Text3.setText(cursor.getString(3),TextView.BufferType.EDITABLE);

            }
        }
        else{
            //if something goes wrong
            showmessage(getString(R.string.sorry),getString(R.string.no_records));
        }

    }
    //delete's button function delay 1.5 sec and returns to ContactsActivity
    public void delete_contact(View view){
        db.execSQL("DELETE FROM Informations WHERE user_id='"+id+"'");
        showmessage(getString(R.string.success),getString(R.string.deleted));
        //some delay to show showmessage first and then return to ContactsActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(EditDeleteActivity.this, ContactsActivity.class);
                startActivity(intent);
            }
        }, 1500);
    }
    //edit's button function to update database with new values
    public void edit_contact(View view){
        String name=Text.getText().toString().trim();
        String lastname=Text2.getText().toString().trim();
        String tel=Text3.getText().toString().trim();
        if(name.matches("")){
            showmessage(getString(R.string.error),getString(R.string.fill_name));
        }
        else{
            db.execSQL("UPDATE Informations SET name = '"+name+"', lastname='"+lastname+"', tel='"+tel+"' WHERE user_id = '"+id+"'");
            showmessage(getString(R.string.success),getString(R.string.update));
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(EditDeleteActivity.this, ContactsActivity.class);
                startActivity(intent);
            }
        }, 1500);
    }
    //popup information window
    public void showmessage(String title,String text ){
        final AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setCancelable(true);
        builder.setMessage(text);
        builder.show();

    }
    //refresh Contacts when change/delete contacts Back button
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            finish();
            Intent intent = new Intent(EditDeleteActivity.this,
                    ContactsActivity.class);
            startActivity(intent);
            finishAfterTransition();
        }
        return super.onKeyDown(keyCode, event);
    }
}
