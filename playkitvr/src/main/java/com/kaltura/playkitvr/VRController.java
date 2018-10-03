package com.kaltura.playkitvr;

import android.view.View;

import com.kaltura.playkit.PKController;
import com.kaltura.playkit.player.vr.VRInteractionMode;

/**
 * Created by anton.afanasiev on 30/04/2018.
 */

public interface VRController extends PKController {


    /**
     * When set to true, will split screen in a way that fit to cardboard devices.
     * When set to false, content will be displayed in an ordinary 360 way.
     * @param shouldEnable - if enable VRMode.
     */
    void enableVRMode(boolean shouldEnable);

    /**
     * When set to true, will allow some continiues motion effect when surface is swiped.
     * If disabled, motion will stop whenever MotionEvent.ACTION_UP/MotionEvent.ACTION_CANCEL detected.
     * @param shouldEnable - if enable this.
     */
    void setFlingEnabled(boolean shouldEnable);

    /**
     * Set the Interaction mode with 360/VR surface.
     * Before applying, application must check if requested mode is supported by the device.
     * @param mode - requested mode.
     */
    void setInteractionMode(VRInteractionMode mode);

    /**
     * Allows to enable zooming of the surface with pinch motion.
     * @param shouldEnable - if this should be enabled.
     */
    void setZoomWithPinchEnabled(boolean shouldEnable);

    /**
     * Set clickListener on the 360/VR surface. When application wants to detect
     * click input on the 360/VR surface it is required to pass it here instead of
     * just attaching it to the containerView.
     * @param onClickListener - View.ClickListener
     */
    void setOnClickListener(View.OnClickListener onClickListener);

    /**
     * @return - true if in vr mode. otherwise false.
     */
    boolean isVRModeEnabled();

    /**
     * @return - true if pinch is enabled.
     */
    boolean isPinchEnabled();

    /**
     * @return - true if enabled.
     */
    boolean isFlingEnabled();

    /**
     *
     * @return - current interaction mode.
     */
    VRInteractionMode getInteractionMode();

}
