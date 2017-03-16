package com.pzuborev.photogallery;

import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    private static final String TAG = "PhotoGalleryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
    }

    @Override
    protected PhotoGalleryFragment CreateFragment() {
        Log.d(TAG, "CreateFragment: ");
        return new PhotoGalleryFragment();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent: !!!!!!!!!!!!");
        PhotoGalleryFragment fragment = (PhotoGalleryFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (Intent.ACTION_SEARCH.contains(intent.getAction())){
            String query = (String) intent.getSerializableExtra(SearchManager.QUERY);
            Log.d(TAG, "onNewIntent: query = "+ query);

            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString(FlickrFetchr.SEARCH_QUERY, query)
                    .commit();
        }

        fragment.updateItems();


    }

    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData, boolean globalSearch) {
//        initialQuery = PreferenceManager.getDefaultSharedPreferences(this).getString(FlickrFetchr.SEARCH_QUERY, null);
//        selectInitialQuery = true;
        Log.d(TAG, "startSearch: ");

        super.startSearch(initialQuery, selectInitialQuery, appSearchData, globalSearch);
    }
}
