package com.kaltura.playkitvr;

import android.content.Context;
import android.view.Surface;
import android.widget.Toast;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.kaltura.playkit.PKController;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PlayerEngineWrapper;
import com.kaltura.playkit.player.PKMediaSourceConfig;
import com.kaltura.playkit.player.PlayerEngine;
import com.kaltura.playkit.player.PlayerView;
import com.kaltura.playkit.player.vr.VRInteractionMode;
import com.kaltura.playkit.player.vr.VRSettings;
import com.kaltura.playkit.utils.Consts;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class DefaultVRPlayerWrapper extends PlayerEngineWrapper {

    private PKLog log = PKLog.get(DefaultVRPlayerWrapper.class.getSimpleName());

    private Context context;
    private MDVRLibrary vrLib;
    private Surface videoSurface;
    private VRControllerImpl vrController;
    private float barrelDistortionScale;

    DefaultVRPlayerWrapper(final Context context, PlayerEngine player, @Nullable VRSettings vrSettings) {
        this.context = context;
        this.playerEngine = player;
        barrelDistortionScale = extractBarrelDistortionScale(vrSettings);
        vrLib = createVRLibrary();
        vrLib.onResume(context);
        this.vrController = new VRControllerImpl(context, vrLib);
    }

    private MDVRLibrary createVRLibrary() {
        return MDVRLibrary.with(context)
                .asVideo(surface -> {
                    videoSurface = surface;
                    if (playerEngine != null) {
                        final PlayerView view = playerEngine.getView();
                        if (view != null && videoSurface != null) {
                            view.post(() -> ((VRView) view).setSurface(videoSurface));
                        }
                    }
                })
                .ifNotSupport(mode -> {
                    String errorMessage = ("Selected mode " + mode + " is not supported by the device");
                    log.e(errorMessage);
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                })
                .listenGesture(e -> {
                    if (vrController != null && playerEngine != null) {
                        vrController.onSurfaceClicked(playerEngine.getView());
                    }
                })
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_TOUCH)
                .barrelDistortionConfig(new BarrelDistortionConfig().setDefaultEnabled(false).setScale(barrelDistortionScale))
                .build(((VRView) playerEngine.getView()).getGlSurface());
    }

    @Nonnull
    private Float extractBarrelDistortionScale(@Nullable VRSettings vrSettings) {
        if (vrSettings != null) {
            return vrSettings.getBarrelDistortionScale();
        }
        return VRSettings.DEFAULT_BARREL_DISTORTION_SCALE;
    }

    @Override
    public void load(PKMediaSourceConfig sourceConfig) {
        VRSettings vrSettings = sourceConfig.getVrSettings();
        if (vrSettings != null && !VRUtil.isModeSupported(context, vrSettings.getInteractionMode())) {
            //In case when mode is not supported we switch to supported mode.
            String vrInteractionModeName = vrSettings.getInteractionMode() != null ? vrSettings.getInteractionMode().name() : "unknown";
            vrSettings.setInteractionMode(VRInteractionMode.Touch);
            String errorMessage = ("load: VR interactionMode: " + vrInteractionModeName + " is not supported by the device using VRInteractionMode.Touch instead");
            log.e(errorMessage);
        }

        vrController.load(vrSettings);
        playerEngine.load(sourceConfig);
    }

    @Override
    public void release() {
        vrController.release();
        playerEngine.release();
        vrLib.onPause(context);
    }

    @Override
    public void restore() {
        playerEngine.restore();
        vrLib.onResume(context);
        ((VRView) playerEngine.getView()).setSurface(videoSurface);
    }

    @Override
    public void destroy() {
        playerEngine.destroy();
        vrLib.onDestroy();
        vrController = null;
    }

    @Override
    public float getPlaybackRate() {
        if (playerEngine != null) {
            return playerEngine.getPlaybackRate();
        }
        return Consts.DEFAULT_PLAYBACK_RATE_SPEED;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends PKController> T getController(Class<T> type) {
        if (type == VRController.class && vrController != null) {
            return (T) vrController;
        }
        return super.getController(type);
    }

    @Override
    public void onOrientationChanged() {
        vrLib.onOrientationChanged(context);
    }
}