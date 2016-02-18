package com.khfire22.flashlight;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;


// Allow the user to turn on their flashlight simply

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.main_fab)
    FloatingActionButton myFab;
    @Bind(R.id.switch_auto_start)
    Switch autoStartSwitch;
    @Bind(R.id.switch_stay_on)
    Switch stayOnSwitch;

    private static final String TAG = "MainActivity";
    private static final String autoOnBooleanPref = "autoOnBooleanPref";
    private String stayOnBooleanPref = "stayOnBooleanPref";
    private String shortcutBooleanPref = "shortcutBooleanPref";

    private boolean hasFlash;
    private Camera camera;
    private Camera.Parameters params;
    private boolean isFlashlightOn;
    public String myMorseString;
    private int sleepTime;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isAutoOnEnabled;
    private boolean isStayOnEnabled;
    private boolean isShortCutEnabled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Call method to check for flashlight capabillity
        hasFlashlightCapability();

        // Instantiate the shared prefs for the main activity
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        sharedPreferences.edit();
        editor = sharedPreferences.edit();

        isAutoOnEnabled = sharedPreferences.getBoolean(autoOnBooleanPref, false);
        isStayOnEnabled = sharedPreferences.getBoolean(stayOnBooleanPref, false);
        isShortCutEnabled = sharedPreferences.getBoolean(shortcutBooleanPref, false);

        autoStartSwitch.setChecked(isAutoOnEnabled);
        stayOnSwitch.setChecked(isStayOnEnabled);
    }

    public void turnOnLightIfSetToAuto() {

        if (isAutoOnEnabled && hasFlash) {
            turnLightOn();
        }
    }

    // First check if device is supporting flashlight or not
    public void hasFlashlightCapability() {
        hasFlash = getApplicationContext()
                .getPackageManager()
                .hasSystemFeature(PackageManager
                        .FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Error");
            builder.setMessage("Sorry, your device doesn't support flash light!");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.show();
        }
    }

    // Get the camera parameters
    public Camera getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.d(TAG, "Camera Error. Failed to Open. Error: " + e);
            }
        }
        return camera;
    }

    // Toggle the flashlight on or off
    @OnClick(R.id.main_fab)
    public void toggleFlashlight() {
        if (isFlashlightOn) {
            // turn off flash
            turnLightOff();

        } else {
            // turn on flash
            turnLightOn();
        }
    }


    // Activate the flashlight
    public void turnLightOn() {
        if (!isFlashlightOn) {
            getCamera();
            if (camera == null || params == null && hasFlash) {
                return;
            }

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashlightOn = true;
        }
    }

    // Turn off the flashlight
    public void turnLightOff() {
        if (isFlashlightOn) {
            if (camera == null || params == null) {
                return;
            }

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashlightOn = false;
        }
    }


    // Switches////////////////////////////////////////////////////////////////////////////

    // Auto start switch action
    @OnCheckedChanged(R.id.switch_auto_start)
    public void autoOn(boolean isChecked) {
        if (isChecked) {
            editor.putBoolean(autoOnBooleanPref, true);
            editor.commit();

        } else {
            editor.putBoolean(autoOnBooleanPref, false);
            editor.commit();
        }
    }

    // Stay on switch action
    @OnCheckedChanged(R.id.switch_stay_on)
    public void stayOn(boolean isChecked) {
        if (isChecked) {
            editor.putBoolean(stayOnBooleanPref, true);
            editor.commit();

        } else {
            editor.putBoolean(stayOnBooleanPref, false);
            editor.commit();
        }
    }


    @OnClick(R.id.sos_button)
    public void SOS() {
        Toast.makeText(MainActivity.this, "S.O.S.", Toast.LENGTH_SHORT).show();

        myMorseString = "111000111";

        new Thread() {
            public void run() {
                if (myMorseString != null) {
                    for (int x = 0; x < myMorseString.length(); x++) {
                        if (myMorseString.charAt(x) == '2') {
                            camera = Camera.open();
                            sleepTime = 500;
                            Camera.Parameters p = camera.getParameters();
                            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            camera.setParameters(p);
                            camera.startPreview();
                            try {
                                Thread.sleep(sleepTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            // power off after signal
                            camera.stopPreview();
                            camera.release();
                            camera = null;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if (myMorseString.charAt(x) == '1') {
                            camera = Camera.open();
                            sleepTime = 250;
                            Camera.Parameters p = camera.getParameters();
                            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            camera.setParameters(p);
                            camera.startPreview();
                            try {
                                Thread.sleep(sleepTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            // power off after signal
                            camera.stopPreview();
                            camera.release();
                            camera = null;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if (myMorseString.charAt(x) == '0') {
                            camera = Camera.open();
                            sleepTime = 250;
                            Camera.Parameters p = camera.getParameters();
                            camera.setParameters(p);
                            //cam.startPreview();
                            camera.stopPreview();
                            camera.release();
                            camera = null;

                            try {
                                Thread.sleep(sleepTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // on pause turn off the flash if stay on is set to false
        if (!isStayOnEnabled) {
            turnLightOff();
            try {
                camera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (!isStayOnEnabled) {

        if (isAutoOnEnabled) {
            getCamera();
            turnLightOn();
        }
//        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
