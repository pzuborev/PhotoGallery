package com.pzuborev.photogallery;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;


public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";
    private static final String CURRENT_POSITION = "CURRENT_POSITION";
    private GridView mGridView;
    private ArrayList<GalleryItem> mGalleryItems;
    private int mTotalPhotosCount;
    private int mPagesCount;
    private int mPage;
    private int mPosition;
    private ThumbnailDownloader<ImageView> mThumbnailDownloader;
    private LruCache<String, Bitmap> mLruCache;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        setRetainInstance(true);
        setHasOptionsMenu(true);

        updateItems();

        mThumbnailDownloader = new ThumbnailDownloader<>(new Handler());
        mThumbnailDownloader.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap bitmap, String url) {
                if (mLruCache.get(url) == null)
                    mLruCache.put(url, bitmap);
                if (isVisible()) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "onCreate: background thread started");

        Intent i = new Intent(getActivity(), PollService.class);
        getActivity().startService(i);
    }

    public void updateItems() {
        Log.d(TAG, "updateItems: ");
        mPage = 0;
        mTotalPhotosCount = 0;
        mPagesCount = 0;
        mGalleryItems = new ArrayList<GalleryItem>();
        new FetchItemsTask().execute(mPage + 1);
        mLruCache = new LruCache<>(100);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "onDestroy: Background thread destroyed");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mGridView = (GridView) v.findViewById(R.id.gridView);

        if (mGridView.getAdapter() == null) {
            Log.d(TAG, "onCreateView: adapter is null");

            mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    mPosition = firstVisibleItem;
                }
            });
        }

        setupAdapter();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_item_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.d(TAG, "onClose: searchView");
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(FlickrFetchr.SEARCH_QUERY, null)
                        .commit();
                updateItems();
                return false;
            }
        });
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getActivity().getComponentName());
        searchView.setSearchableInfo(searchableInfo);
//        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(true);
//        searchView.setQueryRefinementEnabled(true);
        searchView.setQuery(
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getString(FlickrFetchr.SEARCH_QUERY, null)
                , false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_search:
                getActivity().onSearchRequested();
                return true;
            case R.id.menu_item_clear_search:
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(FlickrFetchr.SEARCH_QUERY, null)
                        .commit();
                updateItems();
                Log.d(TAG, "onOptionsItemSelected: clearSearch!!!!!!!!!!!!!!");

                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setupAdapter() {
        if (getActivity() == null || mGridView == null)
            return;
        if (mGalleryItems == null) {
            mGridView.setAdapter(null);
        } else {
            PhotoGalleryAdapter adapter = new PhotoGalleryAdapter(mGalleryItems);
            mGridView.setAdapter(adapter);
        }

    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, GalleryItemList> {

        @Override
        protected GalleryItemList doInBackground(Integer... params) {
            Log.d(TAG, "doInBackground: ");
            FlickrFetchr fetchr = new FlickrFetchr();

            Activity activity = getActivity();

            if (activity == null)
                return GalleryItemList.emptyList();

            for (Integer p : params) {
                String query = PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getString(FlickrFetchr.SEARCH_QUERY, null);
                if (query != null) {
                    Log.d(TAG, "doInBackground: search query =" + query);
                    return fetchr.search(query);
                } else
                    return fetchr.fetchItems(p);
            }


            return GalleryItemList.emptyList();


        }

        @Override
        protected void onPostExecute(GalleryItemList galleryItemList) {
            super.onPostExecute(galleryItemList);
            mGalleryItems.addAll(galleryItemList.getItems());
            mPagesCount = galleryItemList.getPages();
            mTotalPhotosCount = galleryItemList.getTotal();
            mPage = galleryItemList.getPage();
            setupAdapter();
            Log.d(TAG, "onPostExecute: position = " + mPosition);
            mGridView.setSelection(mPosition);
            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                    String.format("Total %,d", mTotalPhotosCount), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }


    }

    private class PhotoGalleryAdapter extends ArrayAdapter<GalleryItem> {
        private ImageView mImageView;

        public PhotoGalleryAdapter(List<GalleryItem> items) {
            super(getActivity(), 0, items);
        }

//        @Nullable
//        @Override
//        public GalleryItem getItem(int position) {
//            Log.d(TAG, "getItem: position = " + position);
//            if (position >= getCount() - 1) {
//                Log.d(TAG, "need refresh");
//                new FetchItemsTask().execute(mPage + 1);
//                notifyDataSetChanged();
//            }
//            GalleryItem item = super.getItem(position);
//            return item;
//        }


        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.fragment_photo_gallery_item, parent, false);
            }

            mImageView = (ImageView) convertView.findViewById(R.id.gallery_item_imageView);
            GalleryItem item = getItem(position);
            Bitmap bitmap = mLruCache.get(item.getUrl());
            if (bitmap == null) {
                mImageView.setImageResource(R.drawable.brian_up_close);
                mThumbnailDownloader.queueThumbnail(mImageView, item.getUrl());
            } else
                mImageView.setImageBitmap(bitmap);

            return convertView;
        }
    }
}


