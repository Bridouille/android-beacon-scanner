package com.bridou_n.beaconscanner.dagger.modules;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.support.v4.content.ContextCompat;

import com.bridou_n.beaconscanner.R;
import com.bridou_n.beaconscanner.dagger.PerActivity;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * Created by bridou_n on 05/04/2017.
 */

@Module
public class AnimationModule {

    @Provides @PerActivity @Named("play_to_pause")
    public AnimatedVectorDrawable providesPlayToStop(Context ctx) {
        return (AnimatedVectorDrawable) ContextCompat.getDrawable(ctx, R.drawable.play_to_pause_animation);
    }

    @Provides @PerActivity @Named("pause_to_play")
    public AnimatedVectorDrawable providesStopToPlay(Context ctx) {
        return (AnimatedVectorDrawable) ContextCompat.getDrawable(ctx, R.drawable.pause_to_play_animation);
    }
}
