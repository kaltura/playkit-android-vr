package com.kaltura.playkitvr;

import android.content.Context;

import com.kaltura.playkit.player.BaseExoplayerView;
import com.kaltura.playkit.player.PlayerEngine;
import com.kaltura.playkit.player.vr.VRPlayerFactory;

/**
 * Created by anton.afanasiev on 23/07/2017.
 */

public class DefaultVRPlayerFactory implements VRPlayerFactory {

    @Override
    public PlayerEngine newInstance(Context context, PlayerEngine player) {
        return new DefaultVRPlayerWrapper(context, player);
    }

    @Override
    public BaseExoplayerView newVRViewInstance(Context context) {
        return new VRView(context);
    }
}
