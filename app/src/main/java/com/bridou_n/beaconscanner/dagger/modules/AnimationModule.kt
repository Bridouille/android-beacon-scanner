package com.bridou_n.beaconscanner.dagger.modules

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.support.v4.content.ContextCompat

import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.dagger.PerActivity

import javax.inject.Named

import dagger.Module
import dagger.Provides

/**
 * Created by bridou_n on 05/04/2017.
 */

@Module
class AnimationModule {

    @Provides
    @PerActivity
    @Named("play_to_pause")
    fun providesPlayToStop(ctx: Context): AnimatedVectorDrawable {
        return ContextCompat.getDrawable(ctx, R.drawable.play_to_pause_animation) as AnimatedVectorDrawable
    }

    @Provides
    @PerActivity
    @Named("pause_to_play")
    fun providesStopToPlay(ctx: Context): AnimatedVectorDrawable {
        return ContextCompat.getDrawable(ctx, R.drawable.pause_to_play_animation) as AnimatedVectorDrawable
    }
}
