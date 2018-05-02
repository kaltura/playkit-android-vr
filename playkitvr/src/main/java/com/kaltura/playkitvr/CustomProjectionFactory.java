package com.kaltura.playkitvr;

import com.asha.vrlib.common.MDDirection;
import com.asha.vrlib.strategy.projection.AbsProjectionStrategy;
import com.asha.vrlib.strategy.projection.IMDProjectionFactory;
import com.asha.vrlib.strategy.projection.MultiFishEyeProjection;

/**
 * Created by hzqiujiadi on 16/8/20.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class CustomProjectionFactory implements IMDProjectionFactory {

    private static final int CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL = 9611;

    @Override
    public AbsProjectionStrategy createStrategy(int mode) {
        switch (mode){
            case CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL:
                return new MultiFishEyeProjection(0.745f, MDDirection.VERTICAL);
            default:return null;
        }
    }
}
