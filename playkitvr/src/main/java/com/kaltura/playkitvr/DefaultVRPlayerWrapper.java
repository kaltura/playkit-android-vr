package com.kaltura.playkitvr;

import android.content.Context;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
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
import com.kaltura.playkit.player.vr.VRInteractionMode;
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

    private View.OnClickListener surfaceClickListener;
    private VRController vrController = initVRController();

    DefaultVRPlayerWrapper(final Context context, PlayerEngine player) {
        this.context = context;
        this.player = player;
        vrLib = createVRLibrary();
        vrLib.onResume(context);
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
                        String tip = mode == MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH
                                ? "onNotSupport:MOTION" : "onNotSupport:" + String.valueOf(mode);
                        Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
                    }
                })
                .listenGesture(new MDVRLibrary.IGestureListener() {
                    @Override
                    public void onClick(MotionEvent e) {
                        if (surfaceClickListener != null) {
                            surfaceClickListener.onClick(player.getView());
                        }
                    }
                })
                .projectionFactory(new CustomProjectionFactory())
                .barrelDistortionConfig(new BarrelDistortionConfig().setDefaultEnabled(false).setScale(0.95f))
                .build(((VRView) player.getView()).getGlSurface());
    }

    @Override
    public void load(PKMediaSourceConfig sourceConfig) {
        VRSettings vrSettings = sourceConfig.getVrSettings();
        if (vrSettings != null) {
            maybeChangeDisplayMode(vrSettings.isVrModeEnabled());
            maybeChangeFlingConfiguration(vrSettings.isFlingEnabled());
            maybeChangeInteractionMode(vrSettings.getInteractionMode());
            maybeChangeZoomWithPinchConfiguration(vrSettings.isZoomWithPinchEnabled());
        }

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
        surfaceClickListener = null;
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
            return (T) vrController;
        }
        return null;
    }

    @Override
    public void onOrientationChanged() {
        vrLib.onOrientationChanged(context);
    }

    private VRController initVRController() {
        return new VRController() {
            @Override
            public void enableVRMode(boolean shouldEnable) {
                maybeChangeDisplayMode(shouldEnable);
            }

            @Override
            public void setOnClickListener(View.OnClickListener onClickListener) {
                surfaceClickListener = onClickListener;
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
            public void setFlingEnabled(boolean shouldEnable) {
                maybeChangeFlingConfiguration(shouldEnable);
            }
        };
    }

    private void maybeChangeInteractionMode(VRInteractionMode interactionMode) {
        if (vrLib == null) {
            log.w("Trying to change VR interaction mode while VRLibrary not initialized yet");
            return;
        }

        int interactiveMode = getInteractionMode(interactionMode);
        if (interactiveMode != vrLib.getInteractiveMode()) {
            vrLib.switchInteractiveMode(context, getInteractionMode(interactionMode));
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

    private int getInteractionMode(VRInteractionMode mode) {
        switch (mode) {
            case Motion:
                return MDVRLibrary.INTERACTIVE_MODE_MOTION;
            case Touch:
                return MDVRLibrary.INTERACTIVE_MODE_TOUCH;
            case MotionWithTouch:
                return MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH;
            case CardboardMotion:
                return MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION;
            case CardboardMotionWithTouch:
                return MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION_WITH_TOUCH;
            default:
                return MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH;
        }
    }

}

