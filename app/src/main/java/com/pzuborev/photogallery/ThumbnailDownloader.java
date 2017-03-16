package com.pzuborev.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class ThumbnailDownloader<Token> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler mHandler;
    private Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
    private Handler mResponseHandler;
    private Listener<Token> mListener;

    public interface Listener<Token> {
        void onThumbnailDownloaded(Token token, Bitmap bitmap, String url);
    }

    public void setListener(Listener<Token> listener) {
        mListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        Log.d(TAG, "ThumbnailDownloader create");
        mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        Log.d(TAG, "onLooperPrepared");
        super.onLooperPrepared();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD ){
                    Token token = (Token) msg.obj;
                    Log.i(TAG, "handleMessage: got url = " + requestMap.get(token));
                    handleRequest(token);
                }
            }
        };
    }

    private void handleRequest(final Token token) {
        Log.d(TAG, "handleRequest");
        final String url = requestMap.get(token);
        if (url == null)
            return;

        try {
            byte[] bytes = new FlickrFetchr().getURLBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Log.i(TAG, "handleRequest: bitmap created");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: request");
                    if (requestMap.get(token) != url) {
                        return;
                    }

                    requestMap.remove(token);
                    mListener.onThumbnailDownloaded(token, bitmap, url);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "handleRequest: " + e.getStackTrace(), e);
            e.printStackTrace();
        }
    }

    public void clearQueue(){
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }

    public void queueThumbnail(Token token, String url){
        Log.i(TAG, "queueThumbnail");
        requestMap.put(token, url);

        mHandler.obtainMessage(MESSAGE_DOWNLOAD, token)
                .sendToTarget();
    }


}
