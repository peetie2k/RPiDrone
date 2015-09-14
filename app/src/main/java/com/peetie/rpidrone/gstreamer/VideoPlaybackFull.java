package com.peetie.rpidrone.gstreamer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.peetie.rpidrone.HUDOverlayView;
import com.peetie.rpidrone.R;
import com.peetie.rpidrone.gstreamer.util.SystemUiHider;

import org.freedesktop.gstreamer.GStreamer;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class VideoPlaybackFull extends Activity implements SurfaceHolder.Callback {
    /*GStreamer Stuff */
    private String message = new String();
    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeConfigPipeline(String pipeline);
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativeStart();     // Constructs PIPELINE
    private native void nativeStop();     // Destroys PIPELINE
    private native void nativePlay();     // Set pipeline to PLAYING
    private native void nativePause();    // Set pipeline to PAUSED
    private native void nativeSurfaceInit(Object surface);
    private native void nativeSurfaceFinalize();
    private native static boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private long native_custom_data;      // Native code will use this to keep private data
    private String ip;
    private String port;





    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;


    private void initializeGstreamer(){
        // Initialize GStreamer and warn if it fails
        try {
            GStreamer.init(this);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

    }



    private void setPipeline( String myPort){

        String pipeline = "udpsrc port="+myPort+" caps=\"application/x-rtp, media=(string)video, clock-rate=(int)90000, encoding-name=(string)H264\" ! rtph264depay  ! avdec_h264 ! videoconvert ! autovideosink sync=false";
        nativeConfigPipeline(pipeline);

    }


    private void setupHUD(){
       /* SurfaceView hud = (SurfaceView) this.findViewById(R.id.hud_overlay);
        hud.getHolder().setFormat(PixelFormat.TRANSPARENT);
        hud.setZOrderMediaOverlay(true);*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setupHUD();


        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);






        Bundle b = getIntent().getExtras();
        port = b.getString("port");
        ip = b.getString("ip");


        this.initializeGstreamer();

        //bind view controls
        setContentView(R.layout.activity_video_playback_full);


        SurfaceView sv = (SurfaceView) this.findViewById(R.id.surface_video);
        SurfaceHolder sh = sv.getHolder();
        sh.addCallback(this);


        final TextView tv = (TextView) this.findViewById(R.id.textview_message);
        tv.setVisibility(View.INVISIBLE);





        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        //final View contentView = findViewById(R.id.fullscreen_content);
        final View contentView = findViewById(R.id.surface_video);



        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    public void onVisibilityChange(boolean visible) {

                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        this.setPipeline("9000");
        nativeInit();
        updateUI();
        nativeStart();
        //nativePlay();




        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_layout);
        HUDOverlayView hud = (HUDOverlayView) findViewById(R.id.hud_overlay);
        frameLayout.removeView(hud);
        hud.setZOrderMediaOverlay(true);
        frameLayout.addView(hud);


        //HUDOverlayView hud = new HUDOverlayView(getApplicationContext());
        //hud.setZOrderMediaOverlay(true);
        //this.addContentView(hud,Layo);


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);

    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized () {
        Log.i("GStreamer", "onGStreamerInitialized");
        nativePlay();
        updateUI();

    }
    protected void onSaveInstanceState (Bundle outState) {
        Log.d ("GStreamer", "onSaveInstanceState");
    }

    protected void onDestroy() {
        nativeFinalize();
        super.onDestroy();
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("videoPlayback");
        nativeClassInit();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("GStreamer", "Surface changed to format " + format + " width "
                + width + " height " + height);
        nativeSurfaceInit(holder.getSurface());
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface created: " + holder.getSurface());
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface destroyed");
        nativeSurfaceFinalize ();
    }

    private void clearMsg(int delay) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                message = "";
                updateUI();
            }
        }, delay);
    }

    private void _updateUI() {
        final TextView tv = (TextView) this.findViewById(R.id.textview_message);
        if (message.length()>0) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(message);
            //clearMsg(3500);
        } else {
            tv.setVisibility(View.INVISIBLE);
        }

    }

    private void updateUI() {

        runOnUiThread (new Runnable() {
            public void run() { _updateUI(); }
        });
    }
    private void setMessage(final String _message) {
        message = _message;
        updateUI();
    }

    private void setError(final int type, final String _message) {
        message = _message;
        updateUI();
    }

    private void notifyState(final int _state) {
        Log.d("STATE","STATE "+_state);
        updateUI();
    }


}
