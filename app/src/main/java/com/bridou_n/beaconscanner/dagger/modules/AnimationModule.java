package com.bridou_n.beaconscanner.dagger.modules;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.bridou_n.beaconscanner.R;
import com.bridou_n.beaconscanner.dagger.PerActivity;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by bridou_n on 05/10/2016.
 */

@Module
public class AnimationModule {

    @Provides @Named("fab_search") @PerActivity
    public Animation providesFabSearchAnimation(Context ctx) {
        return AnimationUtils.loadAnimation(ctx, R.anim.fab_search);
    }
}
