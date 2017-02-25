package com.pzuborev.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;


public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";
    private static final String CURRENT_POSITION = "CURRENT_POSITION";
    private GridView mGridView;
    private ArrayList<GalleryItem> mGalleryItems;
    private int mPage;
    private int mPosition;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        setRetainInstance(true);

        mPage = 0;
        mGalleryItems = new ArrayList<GalleryItem>();
        new FetchItemsTask().execute(mPage + 1);
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
        Log.d(TAG, "!!!!!!!!!!!setupAdapter: ");
        if (mGalleryItems == null) {
            Log.d(TAG, "set adapter = null");
            mGridView.setAdapter(null);
        } else {
            PhotoGalleryAdapter adapter = new PhotoGalleryAdapter();
            mGridView.setAdapter(adapter);
            Log.d(TAG, "set adapter not empty");
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
        public PhotoGalleryAdapter() {
            super(getActivity(), R.layout.fragment_photo_gallery_item, mGalleryItems);
        }

        @Nullable
        @Override
        public GalleryItem getItem(int position) {
            Log.d(TAG, "getItem: position = " + position);
            if (position >= getCount() - 1) {
                Log.d(TAG, "need refresh");
                new FetchItemsTask().execute(mPage + 1);
                notifyDataSetChanged();
            }
            GalleryItem item = super.getItem(position);
            //item.setId(String.valueOf(position));
            return item;
        }
    }
}


