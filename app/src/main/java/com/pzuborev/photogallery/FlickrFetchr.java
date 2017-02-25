package com.pzuborev.photogallery;


import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FlickrFetchr {

    private static final String TAG = "FlickrFetchr";
    private static final String ENDPOINT = "https://api.flickr.com/services/rest/";
    private static final String API_KEY = "a54c275c7dadd36d43ca6807e34932b1";
    private static final String SECRET = "9f59a28d428747ea";
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String PARAM_EXTRAS = "extras";
    private static final String EXTRA_SMALL_URL = "url_s";
    private static final String XML_PHOTO = "photo";

    byte[] getURLBytes(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("GET");
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];
            int readBytes;
            while ( (readBytes = in.read(bytes)) > 0) {
                outputStream.write(bytes, 0, readBytes);
            }
            outputStream.close();

            return outputStream.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    String getURLString(String urlString) throws IOException {
        return new String(getURLBytes(urlString));
    }

    public ArrayList<GalleryItem> fetchItems(int page){
        ArrayList<GalleryItem> items = new ArrayList<>();
        String uri = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_GET_RECENT)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("page", String.valueOf(page <= 0 ? 1 : page))
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .build().toString();
        try {
            String xmlString = getURLString(uri);
            Log.d(TAG, xmlString);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));
            parseItems(items, parser);
        } catch (Exception e) {
            Log.e(TAG, String.valueOf(e.getStackTrace()));
            e.printStackTrace();
        }
        return items;
    }

    private void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser) throws IOException, XmlPullParserException {
        int eventType = parser.next();
        while (eventType != XmlPullParser.END_DOCUMENT){
            if (eventType == XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName())){

                String id = parser.getAttributeValue(null, "id");
                String caption = parser.getAttributeValue(null, "title");
                String smallUrl = parser.getAttributeValue(null, EXTRA_SMALL_URL);
                GalleryItem item = new GalleryItem();
                item.setCaption(caption);
                item.setId(id);
                item.setUrl(smallUrl);
                items.add(item);

            }
            eventType = parser.next();
        }

    }



}
