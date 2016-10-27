/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is collection of files collectively known as Open Camera.

The Initial Developer of the Original Code is Almalence Inc.
Portions created by Initial Developer are Copyright (C) 2013 
by Almalence Inc. All Rights Reserved.
 */

package com.almalence.plugins.capture.standard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CaptureResult;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.almalence.opencam.ApplicationInterface;
import com.almalence.opencam.ApplicationScreen;
import com.almalence.opencam.PluginCapture;
import com.almalence.opencam.PluginManager;
import com.almalence.opencam.R;
import com.almalence.opencam.cameracontroller.CameraController;
import com.almalence.opencam.cameracontroller.CameraController.Size;
import com.almalence.ui.Switch.Switch;

import java.util.ArrayList;
import java.util.Locale;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/* <!-- +++
 import com.almalence.opencam_plus.cameracontroller.CameraController;
 import com.almalence.opencam_plus.cameracontroller.CameraController.Size;
 import com.almalence.opencam_plus.ApplicationInterface;
 import com.almalence.opencam_plus.ApplicationScreen;
 import com.almalence.opencam_plus.PluginCapture;
 import com.almalence.opencam_plus.PluginManager;
 import com.almalence.opencam_plus.R;
 +++ --> */
// <!-- -+-
//-+- -->
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;

/***
 * Implements standard capture plugin - capture single image and save it in
 * shared memory
 ***/



public class CapturePlugin extends PluginCapture {
    private Boolean already_ready = false;
    private static String ModePreference;        // 0=DRO On
    // 1=DRO Off
    private Switch modeSwitcher;
    private int singleModeEV;
    private static int timeToListen = 2;        //time in seconds it needs silence
    private SpeechRecognizer sr;
    private TextToSpeech t1;
    private static final int TTS_CHECK_CODE = 101;

    final Handler handler = new Handler();
    public CapturePlugin( ) {
        super("com.almalence.plugins.capture", 0, 0, 0, null);
    }
//    public CapturePlugin() {
//        super("com.almalence.plugins.capture", 0, 0, 0, null);
//    }

    void UpdateEv(boolean isDro, int ev) {
        if (isDro) {
            // for still-image DRO - set Ev just a bit lower (-0.5Ev or less)
            // than for standard shot
            float expStep = CameraController.getExposureCompensationStep();
            int diff = (int) Math.floor(0.5 / expStep);
            if (diff < 1)
                diff = 1;

            ev -= diff;
        }

        int minValue = CameraController.getMinExposureCompensation();
        if (ev >= minValue) {
            CameraController.setCameraExposureCompensation(ev);
            ApplicationScreen.instance.setEVPref(ev);
        }
    }

    @Override
    public void onCreate() {

        LayoutInflater inflator = ApplicationScreen.instance.getLayoutInflater();
        modeSwitcher = (Switch) inflator.inflate(R.layout.plugin_capture_standard_modeswitcher, null, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationScreen.getMainContext());
        ModePreference = prefs.getString("modeStandardPref", "1");
        singleModeEV = ApplicationScreen.instance.getEVPref();
        modeSwitcher.setTextOn("DRO On");
        modeSwitcher.setTextOff("DRO Off");
        modeSwitcher.setChecked(ModePreference.compareTo("0") == 0 ? true : false);
        modeSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isDro) {

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationScreen.getMainContext());

                if (isDro) {
                    singleModeEV = ApplicationScreen.instance.getEVPref();

                    ModePreference = "0";
                    ApplicationScreen.setCaptureFormat(CameraController.YUV);
                } else {
                    ModePreference = "1";
                    ApplicationScreen.setCaptureFormat(CameraController.JPEG);
                }

                UpdateEv(isDro, singleModeEV);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("modeStandardPref", ModePreference);
                editor.commit();

                ApplicationScreen.instance.relaunchCamera();

                if (ModePreference.compareTo("0") == 0)
                    ApplicationScreen.getGUIManager().showHelp(ApplicationScreen.instance.getString(R.string.Dro_Help_Header),
                            ApplicationScreen.getAppResources().getString(R.string.Dro_Help),
                            R.drawable.plugin_help_dro, "droShowHelp");
            }
        });
        //speech recognizer stuff
        sr = SpeechRecognizer.createSpeechRecognizer(ApplicationScreen.getMainContext());
        sr.setRecognitionListener(new listener());
        Log.d("RL", "Created Listener");

        //voice feedback stuff
//        Intent checkIntent = new Intent();
//        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//        startActivityForResult(checkIntent, TTS_CHECK_CODE);
//        startActivityForResult( getApplicationContext(),checkIntent);
        t1=new TextToSpeech(ApplicationScreen.getMainContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
    }

    @Override
    public void onCameraParametersSetup() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationScreen.getMainContext());
        if (ModePreference.equals("0")) {
            // FixMe: why not setting exposure if we are in dro-off mode?
            UpdateEv(true, singleModeEV);
        }

        if (CameraController.isRemoteCamera()) {
            Size imageSize = CameraController.getCameraImageSize();
            CameraController.setPictureSize(imageSize.getWidth(), imageSize.getHeight());
        }
    }

    @Override
    public void onStart() {
        // Get the xml/preferences.xml preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationScreen.getMainContext());
        ModePreference = prefs.getString("modeStandardPref", "1");

        captureRAW = prefs.getBoolean(ApplicationScreen.sCaptureRAWPref, false);
        PluginManager.getInstance().setSwitchModeType(true);
    }

    @Override
    public void onResume() {
        inCapture = false;
        aboutToTakePicture = false;

        isAllImagesTaken = false;
        isAllCaptureResultsCompleted = true;

        if (ModePreference.compareTo("0") == 0)
            ApplicationScreen.setCaptureFormat(CameraController.YUV);
        else {
            if (captureRAW && CameraController.isRAWCaptureSupported())
                ApplicationScreen.setCaptureFormat(CameraController.RAW);
            else {
                captureRAW = false;
                ApplicationScreen.setCaptureFormat(CameraController.JPEG);
            }
        }
    }

    @Override
    public void onPause() {
        if (ModePreference.contains("0")) {
            UpdateEv(false, singleModeEV);
        }

        //voice feedback stuff
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
        //voice feedback stuff

    }

    @Override
    public void onGUICreate() {
        ApplicationScreen.getGUIManager().removeViews(modeSwitcher, R.id.specialPluginsLayout3);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        if (!CameraController.isRemoteCamera()) {
            ((RelativeLayout) ApplicationScreen.instance.findViewById(R.id.specialPluginsLayout3)).addView(this.modeSwitcher,
                    params);
        }

        this.modeSwitcher.setLayoutParams(params);

        if (ModePreference.compareTo("0") == 0)
            ApplicationScreen.getGUIManager().showHelp("Dro help",
                    ApplicationScreen.getAppResources().getString(R.string.Dro_Help), R.drawable.plugin_help_dro,
                    "droShowHelp");
    }

    @Override
    public void onStop() {
        if (!CameraController.isRemoteCamera()) {
            ApplicationScreen.getGUIManager().removeViews(modeSwitcher, R.id.specialPluginsLayout3);
        }
    }

    @Override
    public void onDefaultsSelect() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationScreen.getMainContext());
        ModePreference = prefs.getString("modeStandardPref", "1");
    }

    @Override
    public void onShowPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationScreen.getMainContext());
        ModePreference = prefs.getString("modeStandardPref", "1");
    }

    protected int framesCaptured = 0;
    protected int resultCompleted = 0;

    @Override
    public void takePicture() {
        String TAG = "RL";
        Log.i(TAG, "Entered takePicture");
        Log.i(TAG, "Button clicked ; already ready value : " + already_ready);


        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, timeToListen  * 1000);
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Waiting for command");
        i.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,"");

//                startActivityForResult(i, VOICE_COMMAND);
        sr.startListening(i);


/*
        framesCaptured = 0;
        resultCompleted = 0;
        createRequestIDList(captureRAW ? 2 : 1);
        if (ModePreference.compareTo("0") == 0)
            CameraController.captureImagesWithParams(1, CameraController.YUV, null, null, null, null, false, true, true);
        else if (captureRAW)
            CameraController.captureImagesWithParams(1, CameraController.RAW, null, null, null, null, false, true, true);
        else
            CameraController.captureImagesWithParams(1, CameraController.JPEG, null, null, null, null, false, true, true);
*/
    }


    @Override
    public void onImageTaken(int frame, byte[] frameData, int frame_len, int format) {
        framesCaptured++;

        String TAG = "ImageTaken";
        Log.d(TAG, "entered onImageTaken  : " + frameData.length + " : " + frame_len);
        Bitmap bmp= BitmapFactory.decodeByteArray(frameData, 0, frameData.length);
        //Mat tmp = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC1);
        Log.d(TAG, "entered onImageTaken  :  Message 2 " );


        boolean isRAW = (format == CameraController.RAW);
        PluginManager.getInstance().addToSharedMem("frame" + framesCaptured + SessionID, String.valueOf(frame));
        PluginManager.getInstance().addToSharedMem("framelen" + framesCaptured + SessionID, String.valueOf(frame_len));

        PluginManager.getInstance().addToSharedMem("frameisraw" + framesCaptured + SessionID, String.valueOf(isRAW));


        PluginManager.getInstance().addToSharedMem("frameorientation" + framesCaptured + SessionID,
                String.valueOf(ApplicationScreen.getGUIManager().getImageDataOrientation()));
        PluginManager.getInstance().addToSharedMem("framemirrored" + framesCaptured + SessionID,
                String.valueOf(CameraController.isFrontCamera()));

        PluginManager.getInstance().addToSharedMem("amountofcapturedframes" + SessionID, String.valueOf(framesCaptured));
        if (isRAW)
            PluginManager.getInstance().addToSharedMem("amountofcapturedrawframes" + SessionID, "1");

        PluginManager.getInstance().addToSharedMem("isdroprocessing" + SessionID, ModePreference);

        if ((captureRAW && framesCaptured == 2) //if capturing raw (raw and jpeg should be saved)
                || !captureRAW || ModePreference.compareTo("0") == 0) //if dro or single shot without raw - only 1 image should be called
        {
            PluginManager.getInstance().sendMessage(ApplicationInterface.MSG_CAPTURE_FINISHED, String.valueOf(SessionID));
            inCapture = false;
            framesCaptured = 0;
            resultCompleted = 0;
        }
    }

    @TargetApi(21)
    @Override
    public void onCaptureCompleted(CaptureResult result) {
        int requestID = requestIDArray[resultCompleted];
        resultCompleted++;
        if (result.getSequenceId() == requestID) {
            PluginManager.getInstance().addToSharedMemExifTagsFromCaptureResult(result, SessionID, resultCompleted);
        }

        if (captureRAW) {
            Log.e("CapturePlugin", "onCaptureCompleted. resultCompleted = " + resultCompleted);
            PluginManager.getInstance().addRAWCaptureResultToSharedMem("captureResult" + resultCompleted + SessionID, result);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data) {
    }

    public boolean delayedCaptureSupported() {
        return true;
    }

    public boolean photoTimeLapseCaptureSupported() {
        return true;
    }

    class listener implements RecognitionListener {
        private static final String TAG = "RL";

        public void onReadyForSpeech(Bundle params) {
            if(already_ready){
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, timeToListen  * 1000);
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Waiting for command");
                i.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, "");
//                startActivityForResult(i, VOICE_COMMAND);
                sr.startListening(i);
            }
            String toSpeak = "ready";
            if(!t1.isSpeaking())t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            Log.d(TAG, "I just said ready!");

            Log.d(TAG, "onReadyForSpeech "+ already_ready.toString());
            already_ready = true;

        }

        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        public void onRmsChanged(float rmsdB) {
            //Log.d(TAG, "onRmsChanged");
;        }

        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
        }

        public void onError(int error) {
            Log.d(TAG,  "error " +  error);
//            mText.setText("error " + error);
            if(error == 7) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, timeToListen * 1000);
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Waiting for command");
                i.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, "");
//                startActivityForResult(i, VOICE_COMMAND);
                already_ready = false;
                sr.startListening(i);
            }
            if(error == 6 || error == 8) {
//                Long time = System.currentTimeMillis();
//                String TAG = "RL";
//                Log.i(TAG, "before sleep" + time.toString());
//                android.os.SystemClock.sleep(3000);      //wait in milliseconds
//                time = System.currentTimeMillis();
//                Log.i(TAG, "after sleep" + time.toString());
//                takePicture();

                handler.postDelayed(new Runnable(){
                    @Override
                            public void run(){
                        takePicture();
                    }
                },1500);
            }

        }

        public void onResults(Bundle results) {
            String str = new String();
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            Boolean ShootDetected = false;
            for (int i = 0; !ShootDetected && i < data.size(); i++) {
                if (data.get(i).toString().equals("click")) {
                    ShootDetected = true;
                    Log.d(TAG, "ShootDetected");
//                    showToast("Taking picture...");
                    String toSpeak = "detected";
                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                    Log.d(TAG, "I spoke LALALALALA!!!");

                }
            }
            if (ShootDetected) {
                shoot();
//                showToast("SHOOTING!!!");
            }

            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, timeToListen * 1000);
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Waiting for command");
            i.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,"");
//                startActivityForResult(i, VOICE_COMMAND);
            already_ready = false;
            sr.startListening(i);

//            mText.setText("results: "+String.valueOf(data.size()));


        }

        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults");
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
    }

    public void shoot(){
        framesCaptured = 0;
        resultCompleted = 0;
        createRequestIDList(captureRAW ? 2 : 1);
        if (ModePreference.compareTo("0") == 0)
            CameraController.captureImagesWithParams(1, CameraController.YUV, null, null, null, null, false, true, true);
        else if (captureRAW)
            CameraController.captureImagesWithParams(1, CameraController.RAW, null, null, null, null, false, true, true);
        else
            CameraController.captureImagesWithParams(1, CameraController.JPEG, null, null, null, null, false, true, true);
    }
    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
//        final Activity activity = getActivity();
        final Activity activity = (Activity) ApplicationScreen.getMainContext();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
//    public void showToast(final String toast)
//    {
//        runOnUiThread(new Runnable() {
//            public void run()
//            {
//                Toast.makeText(MyActivity.this, toast, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//    private Runnable show_toast = new Runnable()
//    {
//        public void run()
//        {
////            Toast.makeText(Autoamtion.this, "My Toast message", Toast.LENGTH_SHORT)
////                    .show();
//            Toast.makeText(activity, "Hello", Toast.LENGTH_SHORT).show();
//        }
//    };
}
