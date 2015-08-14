package com.bignerdranch.android.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Created by Dino10 on 2015. 8. 11..
 */
public class CrimeCameraActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new CrimeCameraFragment();
    }
}
