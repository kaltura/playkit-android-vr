package com.kaltura.playkitvr;

import android.content.Context;
import android.view.MotionEvent;
import android.view.Surface;
import android.widget.Toast;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.kaltura.playkit.PKController;
import com.kaltura.playkit.PKError;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PlaybackInfo;
import com.kaltura.playkit.player.BaseTrack;
import com.kaltura.playkit.player.PKMediaSourceConfig;
import com.kaltura.playkit.player.PKTracks;
import com.kaltura.playkit.player.PlayerEngine;
import com.kaltura.playkit.player.PlayerView;
import com.kaltura.playkit.player.metadata.PKMetadata;
import com.kaltura.playkit.player.vr.VRSettings;
import com.kaltura.playkit.utils.Consts;

import java.util.List;


/**
 * Created by anton.afanasiev on 29/08/2017.
 */

class DefaultVRPlayerWrapper implements PlayerEngine {

    private PKLog log = PKLog.get(DefaultVRPlayerWrapper.class.getSimpleName());

    private Context context;
    private MDVRLibrary vrLib;
    private PlayerEngine player;
    private Surface videoSurface;
    private VRControllerImpl vrController;

    DefaultVRPlayerWrapper(final Context context, PlayerEngine player) {
        this.context = context;
        this.player = player;
        vrLib = createVRLibrary();
        vrLib.onResume(context);
        this.vrController = new VRControllerImpl(context, vrLib);
    }

    private MDVRLibrary createVRLibrary() {
        return MDVRLibrary.with(context)
                .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        videoSurface = surface;
                        ((VRView) player.getView()).setSurface(videoSurface);
                    }
                })
                .ifNotSupport(new MDVRLibrary.INotSupportCallback() {
                    @Override
                    public void onNotSupport(int mode) {
                        String errorMessage = ("Selected mode " + String.valueOf(mode) + " is not supported by the device");
                        if (BuildConfig.DEBUG) {
                            throw new IllegalStateException(errorMessage);
                        }
                        log.e(errorMessage);
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                })
                .listenGesture(new MDVRLibrary.IGestureListener() {
                    @Override
                    public void onClick(MotionEvent e) {
                        if (player == null || player.getView() == null) {
                            return;
                        }
                        vrController.onSurfaceClicked(player.getView());
                    }
                })
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_TOUCH)
                .barrelDistortionConfig(new BarrelDistortionConfig().setDefaultEnabled(false).setScale(0.95f))
                .build(((VRView) player.getView()).getGlSurface());
    }

    @Override
    public void load(PKMediaSourceConfig sourceConfig) {
        vrController.load(sourceConfig.getVrSettings());
        player.load(sourceConfig);
    }

    @Override
    public PlayerView getView() {
        return player.getView();
    }

    @Override
    public void play() {
        player.play();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void replay() {
        player.replay();
    }

    @Override
    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return player.getDuration();
    }

    @Override
    public long getBufferedPosition() {
        return player.getBufferedPosition();
    }

    @Override
    public float getVolume() {
        return player.getVolume();
    }

    @Override
    public PKTracks getPKTracks() {
        return player.getPKTracks();
    }

    @Override
    public void changeTrack(String uniqueId) {
        player.changeTrack(uniqueId);
    }

    @Override
    public void seekTo(long position) {
        player.seekTo(position);
    }

    @Override
    public void startFrom(long position) {
        player.startFrom(position);
    }

    @Override
    public void setVolume(float volume) {
        player.setVolume(volume);
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void setEventListener(EventListener eventTrigger) {
        player.setEventListener(eventTrigger);
    }

    @Override
    public void setStateChangedListener(StateChangedListener stateChangedTrigger) {
        player.setStateChangedListener(stateChangedTrigger);
    }

    @Override
    public void release() {
        vrController.release();
        player.release();
        vrLib.onPause(context);
    }

    @Override
    public void restore() {
        player.restore();
        vrLib.onResume(context);
        ((VRView) player.getView()).setSurface(videoSurface);
    }

    @Override
    public void destroy() {
        player.destroy();
        vrLib.onDestroy();
        vrController = null;
    }

    @Override
    public PlaybackInfo getPlaybackInfo() {
        return player.getPlaybackInfo();
    }

    @Override
    public PKError getCurrentError() {
        return player.getCurrentError();
    }

    @Override
    public void stop() {
        player.stop();
    }

    @Override
    public List<PKMetadata> getMetadata() {
        return player.getMetadata();
    }

    @Override
    public BaseTrack getLastSelectedTrack(int renderType) {
        return player.getLastSelectedTrack(renderType);
    }

    @Override
    public boolean isLiveStream() {
        return player.isLiveStream();
    }

    @Override
    public void setPlaybackRate(float rate) {
        player.setPlaybackRate(rate);
    }

    @Override
    public float getPlaybackRate() {
        if (player != null) {
            return player.getPlaybackRate();
        }
        return Consts.DEFAULT_PLAYBACK_RATE_SPEED;
    }

    @Override
    public <T extends PKController> T getController(Class<T> type) {
        if (type == VRController.class && vrController != null) {
            return (T) this.vrController;
        }
        return null;
    }

    @Override
    public void onOrientationChanged() {
        vrLib.onOrientationChanged(context);
    }
}

