package com.faisal.hiddencameradetector;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.faisal.hiddencameradetector.R;
import com.github.anastr.speedviewlib.AwesomeSpeedometer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class MagneticRadiation extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    TextView Mtext,magnetDetecTextView;
    int magnitude = 0,x,y,z;
    int max = 0;
    MediaPlayer beep;
    AwesomeSpeedometer awesomeSpeedometer;
    public InterstitialAd mInterstitialAd;
    public AdView mAdView;

    public static final String PREFS_NAME = "MyPrefsFile1";
    public CheckBox dontShowAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radiation_main);

        //admob
        MobileAds.initialize(this,"ca-app-pub-7747740414798372~6605206806");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7747740414798372/5713908919");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        //admob
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.v("MagneticRadiation", "onloaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.v("MagneticRadiation", "loadfailed");

            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
                Log.v("MagneticRadiation", "adOpened");

            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                Log.v("MagneticRadiation", "adClicked");

            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.v("MagneticRadiation", "adLeftApp");

            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the interstitial ad is closed.
                Log.v("MagneticRadiation", "adClosed");

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        awesomeSpeedometer= (AwesomeSpeedometer) findViewById(R.id.awesomeSpeedometer);
        awesomeSpeedometer.speedTo(max);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Mtext = findViewById(R.id.m_textView);
        mSensorManager.registerListener(this, mSensor, mSensorManager.SENSOR_DELAY_NORMAL);
        magnetDetecTextView = findViewById(R.id.magnet_detect_textView);
        beep = MediaPlayer.create(MagneticRadiation.this, R.raw.beep);

        awesomeSpeedometer.setUnit("");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater adbInflater = LayoutInflater.from(this);
        View eulaLayout = adbInflater.inflate(R.layout.checkbox, null);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String skipMessage = settings.getString("skipMessage", "NOT checked");
        dontShowAgain = (CheckBox) eulaLayout.findViewById(R.id.skip);
        alertDialogBuilder.setView(eulaLayout);
        // set title
        alertDialogBuilder.setTitle(R.string.Important);
        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.MagneticRadiationMessage)
                .setCancelable(false)
                .setPositiveButton(R.string.OK,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        String checkBoxResult = "NOT checked";

                        if (dontShowAgain.isChecked()) {
                            checkBoxResult = "checked";
                        }

                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();

                        editor.putString("skipMessage", checkBoxResult);
                        editor.apply();
                        editor.commit();

                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        if (!skipMessage.equals("checked")) {
            alertDialog.show();
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        x = (int) event.values[0];
        y = (int) event.values[1];
        z = (int )event.values[2];

        magnitude = (int) Math.sqrt(x*x+y*y+z*z);
        awesomeSpeedometer.speedTo(magnitude/10);
        if(magnitude>=90 && magnitude <=120) {
            magnetDetecTextView.setText("CAMERA DETECTED!");
            beep.start();
        }
        else if(magnitude>120) {
            magnetDetecTextView.setText("POTENTIAL CAMERA / SMALL SPEAKER DETECTED");
            beep.start();
        }
        else magnetDetecTextView.setText("");
        Mtext.setText(String.valueOf(magnitude)+"µF");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void onBackPressed() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
        beep.stop();
        super.onBackPressed();

    }

}
