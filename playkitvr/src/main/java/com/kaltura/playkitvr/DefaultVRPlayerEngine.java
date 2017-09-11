package com.kaltura.playkitvr;

import android.content.Context;

import com.kaltura.playkit.PKError;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PlaybackInfo;
import com.kaltura.playkit.player.PKTracks;
import com.kaltura.playkit.player.PlayerView;
import com.kaltura.playkit.player.VRPlayerEngine;
import com.kaltura.playkit.player.metadata.PKMetadata;

import java.util.List;

/**
 * Created by anton.afanasiev on 29/08/2017.
 */

public class DefaultVRPlayerEngine implements VRPlayerEngine {

    PKLog log = PKLog.get(DefaultVRPlayerEngine.class.getSimpleName());
    VRView vrView;
    VRSurfaceView surfaceView;


    public DefaultVRPlayerEngine(Context context) {
        surfaceView = new VRSurfaceView(context);
        vrView = new VRView(context);
        vrView.addView(surfaceView);
    }

    @Override
    public void setVRModeEnabled(boolean enable) {

    }

    @Override
    public void load(SourceConfig sourceConfig) {
        surfaceView.load(sourceConfig.getUrl().toString());
    }

    @Override
    public PlayerView getView() {
        return vrView;
    }

    @Override
    public void play() {
        surfaceView.play();
    }

    @Override
    public void pause() {
        surfaceView.pause();
    }

    @Override
    public void replay() {

    }

    @Override
    public long getCurrentPosition() {
        return surfaceView.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return surfaceView.nativeGetDuration();
    }

    @Override
    public long getBufferedPosition() {
        return 0;
    }

    @Override
    public float getVolume() {
        return 0;
    }

    @Override
    public PKTracks getPKTracks() {
        return null;
    }

    @Override
    public void changeTrack(String uniqueId) {

    }

    @Override
    public void seekTo(long position) {
        surfaceView.seekTo(position);
    }

    @Override
    public void startFrom(long position) {

    }

    @Override
    public void setVolume(float volume) {

    }

    @Override
    public boolean isPlaying() {

        if (surfaceView != null && surfaceView.getState() == VRPlayerState.PLAYING) {
            return true;
        }
        return false;
    }

    @Override
    public void setEventListener(EventListener eventTrigger) {
        surfaceView.setEventListener(eventTrigger);
    }

    @Override
    public void setStateChangedListener(StateChangedListener stateChangedTrigger) {
        surfaceView.setStateChangedListener(stateChangedTrigger);
    }

    @Override
    public void release() {

    }

    @Override
    public void restore() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public PlaybackInfo getPlaybackInfo() {
        return null;
    }

    @Override
    public PKError getCurrentError() {
        return null;
    }

    @Override
    public void stop() {

    }

    @Override
    public List<PKMetadata> getMetadata() {
        return null;
    }

}
