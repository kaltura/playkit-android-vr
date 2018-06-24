package com.kaltura.playkitvr;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.View;

import com.asha.vrlib.MDVRLibrary;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.player.vr.VRInteractionMode;
import com.kaltura.playkit.player.vr.VRSettings;


public class VRControllerImpl implements VRController {

    private PKLog log = PKLog.get(VRControllerImpl.class.getSimpleName());

    private Context context;
    private MDVRLibrary vrLib;

    private View.OnClickListener surfaceClickListener;
    private boolean onApplicationPaused = false;

    public VRControllerImpl(Context context, MDVRLibrary vrLib) {
        this.context = context;
        this.vrLib = vrLib;
    }

    @Override
    public void enableVRMode(boolean shouldEnable) {
        maybeChangeDisplayMode(shouldEnable);
    }

    @Override
    public void setFlingEnabled(boolean shouldEnable) {
        maybeChangeFlingConfiguration(shouldEnable);

    }

    @Override
    public void setInteractionMode(VRInteractionMode mode) {
        maybeChangeInteractionMode(mode);
    }

    @Override
    public void setZoomWithPinchEnabled(boolean shouldEnable) {
        maybeChangeZoomWithPinchConfiguration(shouldEnable);

    }

    @Override
    public void setOnClickListener(View.OnClickListener onClickListener) {
        surfaceClickListener = onClickListener;
    }

    @Override
    public boolean isModeSupported(VRInteractionMode mode) {
        switch (mode) {
            case Touch:
                //Always supported
                return true;
            case Motion:
            case MotionWithTouch:
                SensorManager motionSensorManager = (SensorManager) context
                        .getSystemService(Context.SENSOR_SERVICE);
                return motionSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null;
            case CardboardMotion:
            case CardboardMotionWithTouch:
                SensorManager cardboardSensorManager = (SensorManager) context
                        .getSystemService(Context.SENSOR_SERVICE);
                Sensor accelerometerSensor = cardboardSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                Sensor gyroSensor = cardboardSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                return accelerometerSensor != null && gyroSensor != null;
            default:
                return true;
        }
    }

    @Override
    public boolean isVRModeEnabled() {
        return vrLib.getDisplayMode() == MDVRLibrary.DISPLAY_MODE_GLASS;
    }

    @Override
    public boolean isPinchEnabled() {
        return vrLib.isPinchEnabled();
    }

    @Override
    public boolean isFlingEnabled() {
        return vrLib.isFlingEnabled();
    }

    @Override
    public VRInteractionMode getInteractionMode() {
        return VRUtil.fromVRLibMode(vrLib.getInteractiveMode());
    }

    private void maybeChangeInteractionMode(VRInteractionMode interactionMode) {
        if (vrLib == null) {
            log.w("Trying to change VR interaction mode while VRLibrary not initialized yet");
            return;
        }

        int interactiveMode = VRUtil.fromVRInteractionMode(interactionMode);
        if (interactiveMode != vrLib.getInteractiveMode()) {
            vrLib.switchInteractiveMode(context, interactiveMode);
        }
    }

    private void maybeChangeDisplayMode(boolean vrModeEnabled) {
        if (vrLib == null) {
            log.w("Trying to change VR display mode while VRLibrary not initialized yet");
            return;
        }

        int requestedDisplayMode = vrModeEnabled ? MDVRLibrary.DISPLAY_MODE_GLASS : MDVRLibrary.DISPLAY_MODE_NORMAL;
        int currentDisplayMode = vrLib.getDisplayMode();
        if (requestedDisplayMode != currentDisplayMode) {
            vrLib.setAntiDistortionEnabled(requestedDisplayMode == MDVRLibrary.DISPLAY_MODE_GLASS);
            vrLib.switchDisplayMode(context, requestedDisplayMode);
        }
    }

    private void maybeChangeZoomWithPinchConfiguration(boolean shouldEnable) {
        if (vrLib == null) {
            log.w("Trying to change Zoom with pinch configuration while VRLibrary not initialized yet");
            return;
        }

        if (vrLib.isPinchEnabled() != shouldEnable) {
            vrLib.setPinchEnabled(shouldEnable);
        }
    }

    private void maybeChangeFlingConfiguration(boolean shouldEnable) {
        if (vrLib == null) {
            log.w("Trying to change Fling configuration while VRLibrary not initialized yet");
            return;
        }

        if (vrLib.isFlingEnabled() != shouldEnable) {
            vrLib.setFlingEnabled(shouldEnable);
        }
    }

    void onSurfaceClicked(View view) {
        if (surfaceClickListener == null) {
            return;
        }

        surfaceClickListener.onClick(view);
    }

    void load(VRSettings vrSettings) {
        if(!onApplicationPaused) {
            enableVRMode(vrSettings.isVrModeEnabled());
            setFlingEnabled(vrSettings.isFlingEnabled());
            setInteractionMode(vrSettings.getInteractionMode());
            setZoomWithPinchEnabled(vrSettings.isZoomWithPinchEnabled());
            onApplicationPaused = false;
        }
    }

    void release() {
        onApplicationPaused = true;
    }
}
