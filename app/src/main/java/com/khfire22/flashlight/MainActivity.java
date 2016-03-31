package com.khfire22.flashlight;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

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
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isAutoOnEnabled;
    private boolean isStayOnEnabled;
    private boolean isShortCutEnabled;
    private boolean isFlashlightOn;
    private Switch slowSwitch;
    private Switch fastSwitch;
    private Switch medSwitch;
    private Thread slowThread;
    private Thread mediumThread;
    private Thread fastThread;
    private TextView timerTV;
    private TextView increaseButton;
    private TextView decreaseButton;
    private Button timerButton;
    private static int timer;
    private CountDownTimer lightTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //UI Declarations///////////////////////////////////////////////////////////
        timerTV = (TextView) findViewById(R.id.tv_timer);
        increaseButton = (TextView) findViewById(R.id.tv_increase);
        decreaseButton = (TextView) findViewById(R.id.tv_decrease);
        timerButton = (Button) findViewById(R.id.button_start);
        slowSwitch = (Switch) findViewById(R.id.slow_switch);
        medSwitch = (Switch) findViewById(R.id.medium_switch);
        fastSwitch = (Switch) findViewById(R.id.fast_switch);
        ///////////////////////////////////////////////////////////////////////////

        timer = 1;

        // Call method to check for flashlight capabillity
        hasFlashlightCapability();

        // Instantiate the shared prefs for the main activity
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        isAutoOnEnabled = sharedPreferences.getBoolean(autoOnBooleanPref, false);
        isStayOnEnabled = sharedPreferences.getBoolean(stayOnBooleanPref, false);
        isShortCutEnabled = sharedPreferences.getBoolean(shortcutBooleanPref, false);

        autoStartSwitch.setChecked(isAutoOnEnabled);
        stayOnSwitch.setChecked(isStayOnEnabled);

        slowSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    if (mediumThread != null) {
                        mediumThread.interrupt();
                    }
                    if (fastThread != null) {
                        fastThread.interrupt();
                    }

                    slowBlink(500, 1000);
                    medSwitch.setChecked(false);
                    fastSwitch.setChecked(false);
                } else {
                    slowThread.interrupt();
                    turnLightOff();
                }
            }
        });

        medSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    if (slowThread != null) {
                        slowThread.interrupt();
                    }
                    if (fastThread != null) {
                        fastThread.interrupt();
                    }

                    mediumBlink(250, 1000);
                    slowSwitch.setChecked(false);
                    fastSwitch.setChecked(false);
                } else {
                    mediumThread.interrupt();
                    turnLightOff();

                }
            }
        });

        fastSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    if (slowThread != null) {
                        slowThread.interrupt();
                    }
                    if (mediumThread != null) {
                        mediumThread.interrupt();
                    }

                    fastBlink(1, 1000);
                    slowSwitch.setChecked(false);
                    medSwitch.setChecked(false);
                } else {
                    fastThread.interrupt();
                    turnLightOff();

                }
            }
        });

        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // If timer button is currently "Stop", then turn off the light and set text to start when pressed
                if (timerButton.getText().equals("Stop")) {
                    timerButton.setText("Start");
                    lightTimer.cancel();
                    turnLightOff();
                } else {
                    // If timer button is currently "Start", then turn on light with current timer settings
                    startTimer(timer);
                    timerButton.setText("Stop");

                }
            }
        });


        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (lightTimer != null) {
                    lightTimer.cancel();
                }

                if (timer > 1) {
                    timer -= 1;

                    if (timer == 1) {
                        timerTV.setText(timer + " minute");
                    } else {
                        timerTV.setText(timer + " minutes");
                    }

                    // When decrease is pressed, turn light off and prime the start button
                    timerButton.setText("Start");
                    turnLightOff();
                }

            }
        });

        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (lightTimer != null) {
                    lightTimer.cancel();
                }

                timer += 1;

                if (timer == 1) {
                    timerTV.setText(timer + " minute");

                } else {
                    timerTV.setText(timer + " minutes");
                }

                // When increase is pressed, turn light off and prime the start button
                timerButton.setText("Start");
                turnLightOff();
            }
        });
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
            if (!hasFlash || params == null || camera == null) {
                return;
            }

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashlightOn = true;
            Log.d(TAG, "Light is on");
        }
    }

    // Turn off the flashlight
    public void turnLightOff() {

        if (isFlashlightOn) {
            getCamera();

            if (!hasFlash || params == null || camera == null) {
                return;
            }

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            camera.setPreviewCallback(null);
            isFlashlightOn = false;
            Log.d(TAG, "Light is off");


        }
    }

    public void slowBlink(final int delay, final int times) {

        slowThread = new Thread() {
            public void run() {
                try {
                    for (int i = 0; i < times; i++) {
                        if (isFlashlightOn) {
                            turnLightOff();
                        } else {
                            turnLightOn();
                        }
                        sleep(delay);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "run: Error = " + e);
                }
            }
        };
        slowThread.start();
    }

    public void mediumBlink(final int delay, final int times) {

        mediumThread = new Thread() {
            public void run() {
                try {
                    for (int i = 0; i < times; i++) {
                        if (isFlashlightOn) {
                            turnLightOff();
                        } else {
                            turnLightOn();
                        }
                        sleep(delay);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mediumThread.start();

    }

    public void fastBlink(final int delay, final int times) {

        fastThread = new Thread() {
            public void run() {
                try {
                    for (int i = 0; i < times; i++) {
                        if (isFlashlightOn) {
                            turnLightOff();
                        } else {
                            turnLightOn();
                        }
                        sleep(delay);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        fastThread.start();
    }

    // Take static timer time(milli) passed from the user, and multiply it by 6000 to get minutes
    // Toggle the light and start timer
    // When timer ends, set TV to "Done" and toggle the light, and reset timer to 1 minute.
    public void startTimer(int time) {

        // Multiply time by 60000 to get time in mili
        time *= 60000;

        // Turn light on
        turnLightOn();

        // Create timer with given parameters, set onTick to fire every minute
        lightTimer = new CountDownTimer(time, 60000) {

            // Update the textview every minute with the current time
            public void onTick(long millisUntilFinished) {

                Log.d(TAG, "onTick: millis left" + millisUntilFinished);

                if (millisUntilFinished <= 119999) {
                    timerTV.setText(millisUntilFinished / 1000 / 60 + " minute");

                } else {
                    timerTV.setText(millisUntilFinished / 1000 / 60 + " minutes");
                }
            }

            // Reset time to 1 once timer is done
            public void onFinish() {
                timerTV.setText("  Done!");
                turnLightOff();
                timer = 1;
            }
        }.start();
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

    @Override
    protected void onResume() {
        super.onResume();

        if (isAutoOnEnabled) {
            turnLightOn();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // on pause turn off the flash if stay on is set to false
        if (!isStayOnEnabled) {
            turnLightOff();

            if (camera != null) {
                camera.release();
            }

        }
    }
}
