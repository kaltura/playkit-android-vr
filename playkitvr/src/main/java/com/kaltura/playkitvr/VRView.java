package com.kaltura.playkitvr;

/**
 * Created by anton.afanasiev on 23/07/2017.
 */

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.ViewGroup;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.kaltura.playkit.player.BaseExoplayerView;


public class VRView extends BaseExoplayerView {

    private GLSurfaceView surface;
    private SimpleExoPlayer player;
    private Surface videoSurface;

    public VRView(Context context) {
        this(context, null);
    }

    public VRView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VRView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        surface = new GLSurfaceView(context);
        surface.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(surface);
    }

    @Override
    public void setPlayer(SimpleExoPlayer player, boolean useTextureView, boolean isSurfaceSecured, boolean hideVideoViews) {
        this.player = player;
        if (this.player != null && videoSurface != null) {
            this.player.setVideoSurface(videoSurface);
        }
    }

    void setSurface(Surface surface) {
        videoSurface = surface;
        if (player != null) {
            player.setVideoSurface(surface);
        }
    }

    @Override
    public void setVideoSurfaceProperties(boolean useTextureView, boolean isSurfaceSecured, boolean hideVideoViews) {

    }

    @Override
    public SubtitleView getSubtitleView() {
        //Not implemented yet
        return null;
    }

    @Override
    public void hideVideoSurface() {
        if(surface != null) {
            surface.setVisibility(GONE);
        }
    }

    @Override
    public void showVideoSurface() {
        if(surface != null) {
            surface.setVisibility(VISIBLE);
        }
    }

    @Override
    public void hideVideoSubtitles() {

    }

    @Override
    public void showVideoSubtitles() {

    }

    GLSurfaceView getGlSurface() {
        return surface;
    }
}
