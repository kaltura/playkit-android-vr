package com.kaltura.playkitvr;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.asha.vrlib.MDVRLibrary;
import com.kaltura.playkit.player.vr.VRInteractionMode;

import java.util.HashMap;
import java.util.Map;

public class VRUtil {

    private static final Map<VRInteractionMode, Integer> interactionModesMap = createMap();

    private static Map<VRInteractionMode, Integer> createMap() {

        HashMap<VRInteractionMode, Integer> interactionMap = new HashMap<>();
        interactionMap.put(VRInteractionMode.Motion, MDVRLibrary.INTERACTIVE_MODE_MOTION);
        interactionMap.put(VRInteractionMode.Touch, MDVRLibrary.INTERACTIVE_MODE_TOUCH);
        interactionMap.put(VRInteractionMode.MotionWithTouch, MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH);
        interactionMap.put(VRInteractionMode.CardboardMotion, MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION);
        interactionMap.put(VRInteractionMode.CardboardMotionWithTouch, MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION_WITH_TOUCH);

        return interactionMap;
    }

    static final int fromVRInteractionMode(VRInteractionMode vrInteractionMode) {
        return interactionModesMap.get(vrInteractionMode);
    }

    static final VRInteractionMode fromVRLibMode(int vrLibMode) {
        for (Map.Entry<VRInteractionMode, Integer> entry : interactionModesMap.entrySet()) {
            if (entry.getValue().equals(vrLibMode)) {
                return entry.getKey();
            }
        }
        //If for some reason(probably after updating MDVRLib to newer version) requested mode does not present in the interactionModeMap
        // it means that this mode was not mapped to VRInteractionMde enum and we should add it.
        throw new IllegalArgumentException("VRLibMode " + vrLibMode + " is not mapped to the corresponding VRInteractionMode");
    }

    /**
     * This method must be called before any change of InteractionMode applied.
     *
     * @param mode - requested mode
     * @return - true if this mode supported by the device. false otherwise.
     */
    public static boolean isModeSupported(Context context, VRInteractionMode mode) {
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
}
