package com.kaltura.playkitvr;

import android.view.View;

import com.kaltura.playkit.PKController;
import com.kaltura.playkit.player.vr.VRInteractionMode;

/**
 * Created by anton.afanasiev on 30/04/2018.
 */

public interface VRController extends PKController {


    void enableVRMode(boolean shouldEnable);

    void setFlingEnabled(boolean shouldEnable);

    void setInteractionMode(VRInteractionMode mode);

    void setZoomWithPinchEnabled(boolean shouldEnable);

    void setOnClickListener(View.OnClickListener onClickListener);
}
