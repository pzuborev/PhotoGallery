package com.pzuborev.photogallery;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;


public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";
    private static final String CURRENT_POSITION = "CURRENT_POSITION";
    private GridView mGridView;
    private ArrayList<GalleryItem> mGalleryItems;
    private int mPage;
    private int mPosition;
    private ThumbnailDownloader<ImageView> mThumbnailDownloader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        setRetainInstance(true);

        mPage = 0;
        mGalleryItems = new ArrayList<GalleryItem>();
        new FetchItemsTask().execute(mPage + 1);

        mThumbnailDownloader = new ThumbnailDownloader<>(new Handler());
        mThumbnailDownloader.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap bitmap) {
                if (isVisible()) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "onCreate: background thread started");
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

    private class FetchItemsTask extends AsyncTask<Integer, Void, ArrayList<GalleryItem>> {

        @Override
        protected ArrayList<GalleryItem> doInBackground(Integer... params) {
            FlickrFetchr fetchr = new FlickrFetchr();

            for (Integer p : params)
                return fetchr.fetchItems(p);
            return new ArrayList<GalleryItem>();


        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            super.onPostExecute(items);
            mGalleryItems.addAll(items);
            mPage = mPage + 1;


            setupAdapter();
            Log.d(TAG, "onPostExecute: position = " + mPosition);
            mGridView.setSelection(mPosition);
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
            mImageView.setImageResource(R.drawable.brian_up_close);

            GalleryItem item = getItem(position);
            mThumbnailDownloader.queueThumbnail(mImageView, item.getUrl());

            return convertView;
        }
    }
}


