package com.khfire22.flashlight;

import android.hardware.Camera;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class MainActivityTest {

    private MainActivity mainActivity;

    @Before
    public void setUp() throws Exception {
        mainActivity = new MainActivity();
    }


    // I don think this is testing anything...
    @Test
    public void cameraTest() throws Exception {

        Camera camera = mainActivity.getCamera();
        Assert.assertNotNull(camera);
    }


    // How can I test something with empty parameters and no return value???
    @Test
    public void isFlashLightOn() throws Exception {

        boolean result = mainActivity.toggleFlashlight();
        Assert.assertTrue(result);


    }
}