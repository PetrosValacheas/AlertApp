package com.example.alertapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

public class ContactsActivity extends AppCompatActivity implements View.OnClickListener {

    SQLiteDatabase db;
    public int id;
    Cursor cursor,c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        getSupportActionBar().setTitle(getString(R.string.title_contact));

        //create or opendatabase
        db = openOrCreateDatabase("Alert", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Informations(user_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name VARCHAR, lastname VARCHAR, tel VARCHAR);");

        //SearchBox
        EditText myTextBox = (EditText) findViewById(R.id.editText5);
        myTextBox.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                EditText editText5 = (EditText) findViewById(R.id.editText5);
                c =db.rawQuery("SELECT * FROM Informations ORDER BY name",null);
                Cursor cur =db.rawQuery("SELECT * FROM Informations WHERE name LIKE '"+editText5.getText().toString()+"%'  OR lastname LIKE '"+editText5.getText().toString()+"%' OR tel LIKE  '"+editText5.getText().toString()+"%'",null);
                if(cur.getCount()==0){
                    showmessage(getString(R.string.sorry),getString(R.string.sorry_no_contacts));
                }else {
                    remove_button(c);
                    print_buttons(cur);
                }
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        //prevent scrollview from autofocus on editexts
        ScrollView view = (ScrollView)findViewById(R.id.scrollView1);
        view.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);

    }

    @Override
    public void onResume (){
        super.onResume();
        //print buttons from database
        cursor =db.rawQuery("SELECT * FROM Informations ORDER BY name COLLATE NOCASE",null);
        print_buttons(cursor);
    }

    //every created button's id goes to editdeleteactivity to connect each button every time with database values
    @Override
    public void onClick(View v) {
        id=v.getId();
        Intent intent = new Intent(this, EditDeleteActivity.class);
        intent.putExtra("id",String.valueOf(id));
        startActivity(intent);
    }

    //button plus(+) to connect with AddContact activity
    public void add_contact(View view){
        Intent intent = new Intent(this, AddContact.class);
        startActivity(intent);
    }

    //popup information window
    public void showmessage(String title,String text ){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setCancelable(true);
        builder.setMessage(text);
        builder.show();
    }

    //function to delete all contacts
    public void delete_all(){
        db.execSQL("DROP TABLE IF EXISTS Informations");
        finish();
        startActivity(getIntent());
    }


    //print contacts
    public void print_buttons(Cursor cursor){

        if(cursor.getCount()==0){

            ConstraintLayout layout = (ConstraintLayout)findViewById(R.id.mainConstraint);
            //   showmessage("Sorry","We couldn't find any contacts");

        }
        else{

            ConstraintLayout layout = (ConstraintLayout)findViewById(R.id.mainConstraint);
            ConstraintSet set = new ConstraintSet();
            int margin=180;

            while(cursor.moveToNext()){

                String name=cursor.getString(1);
                // String subname = name.substring(1,15);
                String sublastname;
                String lastname=cursor.getString(2);
                if(name.length()>17 && lastname.length() > 3 ){
                    sublastname = lastname.substring(0, 3);
                }
                else{
                    sublastname=lastname;
                }

                Button btn = new Button(this);
                btn.setText(name+" "+sublastname);
                layout.addView(btn,0);
                btn.setId(cursor.getInt(0));
                btn.setBackgroundColor(Color.TRANSPARENT);
                btn.setAllCaps(false);
                btn.setTextSize(18);
                btn.setPadding(60,0,60,20);
                btn.setOnClickListener(ContactsActivity.this);

                set.clone(layout);
                set.connect(btn.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, margin);
                set.applyTo(layout);
                margin=margin+100;
            }
        }
    }


    //delete buttons to replace search results
    public void remove_button(Cursor cursor){

        ConstraintLayout layout = (ConstraintLayout)findViewById(R.id.mainConstraint);

        while(cursor.moveToNext()) {
            int id = Integer.parseInt(cursor.getString(0));
            View command = layout.findViewById(id);
            layout.removeView(command);

        }
    }

    //button on title bar with delete all contacts button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                delete_all();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), MainScreenActivity.class));
        finish();

    }
}



