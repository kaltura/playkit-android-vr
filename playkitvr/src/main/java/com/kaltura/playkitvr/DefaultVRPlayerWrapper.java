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
import com.kaltura.playkit.player.vr.VRDistortionConfig;
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
    private BarrelDistortionConfig barrelDistortionConfig;

    DefaultVRPlayerWrapper(final Context context, PlayerEngine player, @Nullable VRSettings vrSettings) {
        this.context = context;
        this.playerEngine = player;
        barrelDistortionConfig = extractBarrelDistortionConfig(vrSettings);
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
                .barrelDistortionConfig(barrelDistortionConfig)
                .build(((VRView) playerEngine.getView()).getGlSurface());
    }

    @Nonnull
    private BarrelDistortionConfig extractBarrelDistortionConfig(@Nullable VRSettings vrSettings) {
        if (vrSettings != null) {
            VRDistortionConfig vrDistortionConfig = vrSettings.getVrDistortionConfig();
            if (vrDistortionConfig != null) {
                return new BarrelDistortionConfig()
                        .setParamA(isDistortionParamInRange(vrDistortionConfig.getParamA()) ?
                                vrDistortionConfig.getParamA() :
                                VRDistortionConfig.DEFAULT_PARAM_A)

                        .setParamB(isDistortionParamInRange(vrDistortionConfig.getParamB()) ?
                                vrDistortionConfig.getParamB() :
                                VRDistortionConfig.DEFAULT_PARAM_B)

                        .setParamC(isDistortionParamInRange(vrDistortionConfig.getParamC()) ?
                                vrDistortionConfig.getParamC() :
                                VRDistortionConfig.DEFAULT_PARAM_C)

                        .setDefaultEnabled(vrDistortionConfig.getDefaultEnabled())
                        .setScale(getDistortionScale(vrDistortionConfig.getScale()));
            }
        }

        return new BarrelDistortionConfig().setDefaultEnabled(false).setScale(VRDistortionConfig.DEFAULT_BARREL_DISTORTION_SCALE);
    }

    /**
     * Checks the distortion scale between 0.10 to 1.0
     * @param scale config scale
     * @return if valid then return it else returns the default value
     *         {#{@link VRDistortionConfig#DEFAULT_BARREL_DISTORTION_SCALE}}
     */
    private float getDistortionScale(Float scale) {
        if (scale == null || (scale < 0.10 || scale > 1.0)) {
            return VRDistortionConfig.DEFAULT_BARREL_DISTORTION_SCALE;
        }
        return scale;
    }

    /**
     * Checks if the distortion config param is in the range of
     * -1.0 to +1.0
     *
     * @param param config param
     * @return if range or not
     */
    private boolean isDistortionParamInRange(Double param) {
        return param != null && (!(param < -1.0) && !(param > 1.0));
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