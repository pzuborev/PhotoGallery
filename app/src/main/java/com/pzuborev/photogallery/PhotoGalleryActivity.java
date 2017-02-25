package com.pzuborev.photogallery;

import android.os.Bundle;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected PhotoGalleryFragment CreateFragment() {
        return new PhotoGalleryFragment();
    }
}
