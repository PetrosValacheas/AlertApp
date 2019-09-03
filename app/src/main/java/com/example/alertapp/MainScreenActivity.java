package com.example.alertapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

import static android.location.LocationManager.GPS_PROVIDER;


public class MainScreenActivity extends AppCompatActivity implements SensorEventListener, LocationListener {
    SQLiteDatabase db;
    SensorManager sensorManager;
    Sensor accelerometersensor;
    Sensor lightsensor;
    TextView text;
    MyTTS myTTS;
    Button abort,contacts,sos,records;
    public CountDownTimer timer;
    public MediaPlayer mediaPlayer;
    Cursor curs,curs_sos;
    public int active;
    LocationManager locationManager;
    String latitude;
    String longitude;
    int PERMISSION_ALL = 1;
    SharedPreferences sharedpreferences;
    @Override
    protected void onCreate (Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //preferences flag is the variable that checks if abort message should be sent or not
        sharedpreferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("flag", 0);
        editor.apply();
        //set permissions in array
        String[] PERMISSIONS = {Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_NETWORK_STATE};
        //create database
        db = openOrCreateDatabase("Alert", Context.MODE_PRIVATE, null);
        //create table for light values and time they were detected
        db.execSQL("CREATE TABLE IF NOT EXISTS Light(light_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,light_value REAL,light_time DATETIME);");
        //create table for emergency contacts
        db.execSQL("CREATE TABLE IF NOT EXISTS Informations(user_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name VARCHAR, lastname VARCHAR, tel VARCHAR);");
        //create table for accelerometer values and time they were detected
        db.execSQL("CREATE TABLE IF NOT EXISTS Accelerometer(acc_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, acc_text TEXT, acc_time DATETIME);");
        db.execSQL("CREATE TABLE IF NOT EXISTS Informations(user_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name VARCHAR, lastname VARCHAR, tel VARCHAR);");

        //textview for countdown 30 seconds
        text = (TextView)findViewById(R.id.textView);
        //assigning sensors for light and accelerometer
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometersensor = (sensorManager).getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightsensor=(sensorManager).getDefaultSensor(Sensor.TYPE_LIGHT);
        //register sensors
        sensorManager.registerListener(this, lightsensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometersensor, SensorManager.SENSOR_DELAY_NORMAL);
        //object for Text To Sound from another class
        myTTS=new MyTTS(getApplicationContext());
        //set sound stored in folder res/raw/..
        mediaPlayer = MediaPlayer.create(this, R.raw.ticking);
        //contacts button
        contacts = (Button) findViewById(R.id.button3);
        records = (Button) findViewById(R.id.button5);

        //check and ask for multiple permissions
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        //for gps
        locationManager = (LocationManager)
                getSystemService(LOCATION_SERVICE);

        //ask the user to turn the GPS on
        if ( !locationManager.isProviderEnabled( GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }

        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        //For 3G check
        boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnectedOrConnecting();
        //For WiFi Check
        boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnectedOrConnecting();
        if (!is3g && !isWifi)
        {
            Toast.makeText(getApplicationContext(),getString(R.string.network_on),Toast.LENGTH_LONG).show();
        }
        //call function for coordinated
        gpson();
        //button to navigate to contacts list
        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ContactsActivity.class);
                finish();
                startActivity(intent);

            }
        });

        //abort button
        abort = (Button) findViewById(R.id.button2);
        abort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if timer is active stop everything and set flag 1 to not send sms
                if(active==1){
                    timer.cancel();
                    if (mediaPlayer.isPlaying()){
                        mediaPlayer.stop();
                        }
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt("flag", 1);
                    editor.apply();
                }
                else{
                    //if button is pressed or timer has finished
                    myTTS.stop_speak();
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt("flag", 0);
                    editor.apply();
                }
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(), ConfirmAbortActivity.class);
                        finish();
                        startActivity(intent);

                    }
                }, 3000);

            }
        });
        //sos button
        sos=(Button)findViewById(R.id.button);
        sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(MainScreenActivity.this);
                //set which buttons are active and which aren't
                sos.setVisibility(View.GONE);
                abort.setVisibility(View.VISIBLE);
                contacts.setVisibility(View.GONE);
                records.setVisibility(View.GONE);
                active=0;
                //check if coordinates null
                if(latitude==null|| longitude==null){
                    Toast.makeText(getApplicationContext(), getString(R.string.coordinates_network), Toast.LENGTH_LONG).show();
                    networkon();
                }
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //set proper speaking language according to application language
                        int language=sharedpreferences.getInt("language",0);
                        if(language==1){
                            myTTS.speak(getString(R.string.be_calm),1);
                        }
                        else if(language==2){
                            myTTS.speak(getString(R.string.be_calm),2);
                        }
                        else{
                            myTTS.speak(getString(R.string.be_calm),3);
                        }
                        //select contacts
                        curs_sos = db.rawQuery("SELECT * FROM Informations", null);
                        //if there are emergency contacts send to everyone sms with coordinates
                        if (curs_sos.getCount() != 0) {
                            while (curs_sos.moveToNext()) {
                                String phone = curs_sos.getString(3);
                                sendSMS(phone, getString(R.string.longitude) +" "+ String.valueOf(longitude) +" "+
                                        getString(R.string.latitude)+" "+ String.valueOf(latitude) +" "+ getString(R.string.help));
                            }
                            Toast.makeText(getApplicationContext(), getString(R.string.help_send), Toast.LENGTH_LONG).show();
                        }
                        Date currentTime = Calendar.getInstance().getTime();
                        db.execSQL("INSERT INTO Accelerometer(acc_text, acc_time) VALUES( '" + true + "','" + currentTime + "')");
                    }
                }, 3000);


            }
        });

        //button records: shows sos and light records
        records.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cur1=db.rawQuery("SELECT *FROM Light",null);
                Cursor cur2=db.rawQuery("SELECT *FROM Accelerometer",null);

                StringBuffer buffer=new StringBuffer();
                if(cur1.getCount()!=0||cur2.getCount()!=0){
                    while(cur1.moveToNext()){
                        buffer.append("------------"+getString(R.string.light)+"------------\n");
                        buffer.append(cur1.getString(0)+". ");
                        buffer.append(getString(R.string.light_value)+cur1.getString(1)+"\n");
                        buffer.append(getString(R.string.date_time)+cur1.getString(2)+"\n");
                    }
                    while(cur2.moveToNext()){
                        buffer.append("---------"+getString(R.string.sos)+"--------\n");
                        buffer.append(cur2.getString(0)+". ");
                        buffer.append(getString(R.string.state)+cur2.getString(1)+"\n");
                        buffer.append(getString(R.string.date_time)+cur2.getString(2)+" \n");
                    }
                    showmessage(getString(R.string.records),buffer.toString());
                }
                else{
                    Toast.makeText(getApplicationContext(), getString(R.string.no_records), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //function for show message box
    public void showmessage(String title, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(text);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sensorManager.registerListener(MainScreenActivity.this, lightsensor, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(MainScreenActivity.this, accelerometersensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        });
        builder.show();
    }

    //when detect changes on sensor
    @Override
    public void onSensorChanged(SensorEvent event) {
        double loX = event.values[0];
        double loY = event.values[1];
        double loZ = event.values[2];
        //if light value is over 10000
        if(loX>10000){
            //every sensor unregisted
            sensorManager.unregisterListener(this);
            //we get the time and date
            Date currentTime = Calendar.getInstance().getTime();
            //add record to database table
            db.execSQL("INSERT INTO Light(light_value,light_time) VALUES ('" + event.values[0] + "','" + currentTime+ "')");
            showmessage(getString(R.string.be_careful),getString(R.string.high_brightness));
        }

        double loAccelerationReader= Math.sqrt(Math.pow(loX, 2) + Math.pow(loY, 2) + Math.pow(loZ, 2));
        //if phone falls
        if (loAccelerationReader < 2.0) {
            //coordinates from network if from gps are null
            if(latitude==null|| longitude==null){
                Toast.makeText(getApplicationContext(), getString(R.string.coordinates_network), Toast.LENGTH_LONG).show();
                networkon();
            }
            sensorManager.unregisterListener(this);
            active=1;
            //set only abort button visible
            sos.setVisibility(View.GONE);
            abort.setVisibility(View.VISIBLE);
            contacts.setVisibility(View.GONE);
            records.setVisibility(View.GONE);
            //start music countdown
            mediaPlayer.start();
            Toast.makeText(this, getString(R.string.fall_detect), Toast.LENGTH_LONG).show();

            timer=new CountDownTimer(30000, 1000) {
                //countdown
                public void onTick(long millisUntilFinished) {
                    active=1;
                    text.setText(String.valueOf(millisUntilFinished / 1000));
                }
                //after countdown has stopped
                public void onFinish() {
                    active=0;
                    text.setText(getString(R.string.help_send));
                    curs =db.rawQuery("SELECT * FROM Informations",null);
                    //sends sms to contacts
                    if(curs.getCount()!=0){
                    while(curs.moveToNext()){
                        String phone = curs.getString( 3 );
                                sendSMS(phone, getString(R.string.longitude) +" "+ String.valueOf(longitude) +" "+
                                getString(R.string.latitude)+" "+ String.valueOf(latitude) +" "+ getString(R.string.help));
                        }
                    }
                    //add record to database
                    Date currentTime = Calendar.getInstance().getTime();
                    db.execSQL("INSERT INTO Accelerometer(acc_text, acc_time) VALUES( '" + true + "','" + currentTime + "')");

                    }
            }.start();

        }

    }


    //function to sent sms
    public void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
        }catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.sms_failed),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //gets values for latitude and longitude
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    //function for gps messagebox to access phone and open gps when closed by the user
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.gps))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    //function for gps coordinates
    public void gpson(){
        //check if permission is given to get gps coordinates
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        }
        else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 20);
    }
    //function for network coordinates
    public void networkon(){
        //check if permission is given to get gps coordinates
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
             locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, this);
        }
        else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 20);
    }
    //function for permissions
    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    //if button back is pressed we navigate to home screen of our device
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}