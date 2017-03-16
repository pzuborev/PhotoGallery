package com.pzuborev.photogallery;


import java.util.ArrayList;

public class GalleryItemList {
    private int mTotal;
    private int mPages;
    private int mPage;
    private ArrayList<GalleryItem> mItems;

    public static GalleryItemList emptyList (){
        return new GalleryItemList();
    }

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public GalleryItemList() {
        mItems = new ArrayList<>();
        setPages(0);
        setTotal(0);
    }

    public ArrayList<GalleryItem> getItems() {
        return mItems;
    }

    public void setItems(ArrayList<GalleryItem> items) {
        this.mItems = items;
    }

    public int getPages() {
        return mPages;
    }

    public void setPages(int pages) {
        this.mPages = pages;
    }

    public int getTotal() {
        return mTotal;
    }

    public void setTotal(int total) {
        this.mTotal = total;
    }
}
