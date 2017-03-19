package com.pzuborev.photogallery;


import android.support.v4.app.Fragment;

public class PhotoPageActivity extends SingleFragmentActivity {
    @Override
    protected Fragment CreateFragment() {
        return new PhotoPageFragment();
    }
}
