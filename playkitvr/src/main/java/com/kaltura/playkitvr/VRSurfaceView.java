package com.kaltura.playkitvr;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;

import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.PlayerState;
import com.kaltura.playkit.player.PlayerEngine;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by anton.afanasiev on 29/08/2017.
 */

public class VRSurfaceView extends GLSurfaceView {

    PKLog log = PKLog.get(VRSurfaceView.class.getSimpleName());

    private PlayerState currentState = PlayerState.IDLE;
    private PlayerState previousState;
    private PlayerEvent.Type currentEvent;

    private int vrPlayerState = VRPlayerState.IDLE;

    private PlayerEngine.EventListener eventListener;
    private PlayerEngine.StateChangedListener stateChangedListener;

    private boolean isSeeking = false;
    private boolean isLoading = false;
    private long requestedSeekPosition = 0;
    private long lastKnownPosition = 0;

    Renderer renderer = null;

    float x = 0, y = 0, z = 0, w = 1.0f;
    float x1, x2, y1, y2, dx, dy;

    static {
        System.loadLibrary("native-lib");
    }

    private boolean shouldPauseAfterPlay = false;
    private boolean playOnLoadRequested = false;
    private boolean isSurfaceCreated = false;
    private boolean isFirstReady = true;


    public VRSurfaceView(Context context) {
        super(context);
        setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setEGLContextClientVersion(3);
        setPreserveEGLContextOnPause(true);

        initializeNative(this, (Activity) context, context.getAssets());
    }

    private void updatePlayerState() {

        if (vrPlayerState == getState() && !isSeeking) {
            return;
        }

        vrPlayerState = getState();

        switch (getState()) {
            case VRPlayerState.INVALID:
                log.e("state INVALID");
                break;
            case VRPlayerState.IDLE:
                log.e("state IDLE");
                changeState(PlayerState.IDLE);
                break;
            case VRPlayerState.LOADING:
                log.e("state Loading");
                isLoading = true;
                isFirstReady = true;
                changeState(PlayerState.LOADING);
                break;
            case VRPlayerState.PAUSE:

                if (playOnLoadRequested) {
                    play();
                    playOnLoadRequested = false;
                }

                break;
            case VRPlayerState.BUFFERING:
                log.e("state buffering");
                if (isLoading) {
                    sendDistinctEvent(PlayerEvent.Type.LOADED_METADATA);
                    sendDistinctEvent(PlayerEvent.Type.DURATION_CHANGE);
                    isLoading = false;
                }
                changeState(PlayerState.BUFFERING);
                break;
            case VRPlayerState.PLAYING:

                if (isFirstReady) {
                    sendDistinctEvent(PlayerEvent.Type.CAN_PLAY);
                    isFirstReady = false;
                }

                if (!isSeeking) {
                    sendDistinctEvent(PlayerEvent.Type.PLAYING);
                }

                if (handleSeek()) {
                    changeState(PlayerState.READY);
                }

                break;
            case VRPlayerState.END_OF_FILE:
                sendEvent(PlayerEvent.Type.ENDED);
                changeState(PlayerState.IDLE);
                break;
            case VRPlayerState.CONNECTION_ERROR:

                break;
            case VRPlayerState.STREAM_ERROR:
                break;
        }
    }

    private boolean handleSeek() {
        if (!isSeeking) {
            return true;
        }

        if (approximatelySame(nativeGetCurrentPosition(), lastKnownPosition)) {
            sendEvent(PlayerEvent.Type.SEEKED);
            sendDistinctEvent(PlayerEvent.Type.CAN_PLAY);
            if (shouldPauseAfterPlay) {
                nativePause();
                shouldPauseAfterPlay = false;
            } else {
                sendDistinctEvent(PlayerEvent.Type.PLAYING);
            }

            isSeeking = false;
            return true;
        }

        return false;
    }

    private boolean approximatelySame(long currentPosition, long lastKnownPosition) {
        if (currentPosition == lastKnownPosition) {
            return false;
        }

        if (currentPosition < lastKnownPosition + 1000 && currentPosition > lastKnownPosition) {
            return false;
        }

        return true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        onAttachWindowNative();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onDetachWindowNative();
    }

    @Override
    public void onPause() {
        queueEvent(new Runnable() {
            public void run() {
                onSuspendOZONative();
            }
        });
        onPauseNative();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        onResumeNative();
    }

    //the following are called from main thread..
    private native void initializeNative(GLSurfaceView view, Activity activity, AssetManager assets);

    private native void onAttachWindowNative();

    private native void onDetachWindowNative();

    private native void onPauseNative();

    private native void onResumeNative();

    //the rest from GL thread. (dispatched accordingly)
    private native void onSurfaceCreatedNative(String videoPath); // call from GL thread

    private native void onSurfaceChangedNative(int width, int height); // call from GL thread

    private native void onDrawFrameNative(float x, float y, float z, float w);    // call from GL thread

    private native void onSuspendOZONative();   // call from GL thread


    native int getState();

    native void nativePlay();

    native void nativePause();

    native long nativeGetDuration();

    native long nativeGetCurrentPosition();

    native int nativeSeekTo(long position);

    public void setEventListener(PlayerEngine.EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void setStateChangedListener(PlayerEngine.StateChangedListener stateChangedListener) {
        this.stateChangedListener = stateChangedListener;
    }

    private void changeState(PlayerState newState) {
        previousState = currentState;
        if (newState.equals(currentState)) {
            return;
        }
        this.currentState = newState;
        if (stateChangedListener != null) {
            stateChangedListener.onStateChanged(previousState, currentState);
        }
    }

    private void sendDistinctEvent(PlayerEvent.Type newEvent) {
        if (newEvent.equals(currentEvent)) {
            return;
        }

        sendEvent(newEvent);
    }

    private void sendEvent(PlayerEvent.Type event) {
        currentEvent = event;
        if (eventListener != null) {
            log.i("Event sent: " + event.name());
            eventListener.onEvent(currentEvent);
        }
    }

    public void load(final String url) {
        renderer = new Renderer() {

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                isSurfaceCreated = true;
                onSurfaceCreatedNative(url);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                onSurfaceChangedNative(width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                updatePlayerState();
                onDrawFrameNative(x, y, z, w);
            }
        };

        setRenderer(renderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);


        String direction = "";
        switch (event.getAction()) {
            case (MotionEvent.ACTION_DOWN):
                x1 = event.getX();
                y1 = event.getY();
                break;

            case (MotionEvent.ACTION_MOVE): {
                x2 = event.getX();
                y2 = event.getY();
                dx = x2 - x1;
                dy = y2 - y1;

                // Use dx and dy to determine the direction
                //if(Math.abs(dx) > Math.abs(dy)) {
                if (dx > 0) {
                    x += 0.003;
                    z += 0.003;
                    direction = "right";
                } else {
                    z -= 0.003;
                    x -= 0.003;
                    direction = "left";
                }
                //    } else {
                if (dy > 0) {
                    y += 0.003;
                    direction = "down";
                } else {
                    y -= 0.003;
                    direction = "up";
                }
            }
        }

        return true;
    }

    public void seekTo(long position) {
        isSeeking = true;
        requestedSeekPosition = position;
        lastKnownPosition = nativeGetCurrentPosition();
        if (getState() == VRPlayerState.PAUSE) {
            nativePlay();
            shouldPauseAfterPlay = true;
        }
        nativeSeekTo(position);
        sendEvent(PlayerEvent.Type.SEEKING);

    }

    long getCurrentPosition() {
        if (isSeeking) {
            return requestedSeekPosition;
        }

        return nativeGetCurrentPosition();
    }

    public void pause() {
        nativePause();
        sendDistinctEvent(PlayerEvent.Type.PAUSE);
    }

    public void play() {
        sendDistinctEvent(PlayerEvent.Type.PLAY);
        if (!isSurfaceCreated) {
            playOnLoadRequested = true;
            return;
        }
        nativePlay();
        shouldPauseAfterPlay = false;
    }
}


