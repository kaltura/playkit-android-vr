package com.kaltura.playkitvr;

/**
 * Created by anton.afanasiev on 23/07/2017.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

import com.kaltura.playkit.player.PlayerView;

import fi.finwe.orion360.sdk.pro.OrionScene;
import fi.finwe.orion360.sdk.pro.OrionViewport;
import fi.finwe.orion360.sdk.pro.item.OrionCamera;
import fi.finwe.orion360.sdk.pro.view.OrionView;

public class VRView extends PlayerView {

    public VRView(Context context) {
        this(context, null);
    }

    public VRView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    public void setVideoSurfaceVisibility(int visibilityState) {

    }

    @Override
    public void setVideoSubtitlesVisibility(int visibilityState) {

    }

    public VRView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}

