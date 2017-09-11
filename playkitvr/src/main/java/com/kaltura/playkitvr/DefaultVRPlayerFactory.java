package com.kaltura.playkitvr;

import android.content.Context;

import com.kaltura.playkit.player.PlayerEngine;
import com.kaltura.playkit.player.VRPlayerEngine;
import com.kaltura.playkit.player.VRPlayerFactory;

/**
 * Created by anton.afanasiev on 23/07/2017.
 */

public class DefaultVRPlayerFactory implements VRPlayerFactory {

    @Override
    public VRPlayerEngine newInstance(Context context, PlayerEngine player) {
        return new DefaultVRPlayerEngine(context);
    }
}
