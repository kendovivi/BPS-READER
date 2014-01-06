
package bps.android.reader.application;

import java.util.ArrayList;

import bps.android.reader.book.BookInfo;

import android.app.Application;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class MyApplication extends Application {

    private ArrayList<BookInfo> mBookList;
    
    private ArrayList<String> mPathIgnoreList;

    private boolean mIsFirstTime = true;
    
    private LruCache<String, Bitmap> mMemoryCache;
    
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public ArrayList<BookInfo> getBookList() {
        return mBookList;
    }

    public void setBookList(ArrayList<BookInfo> list) {
        this.mBookList = list;
    }
    
    public ArrayList<String> getPathIgnoreList(){
        return mPathIgnoreList;
    }
    
    public void setPathIgnoreList(ArrayList<String> pathIgnoreList){
        this.mPathIgnoreList = pathIgnoreList;
    }

    public boolean getIsFirstTime() {
        return mIsFirstTime;
    }

    public void setIsFirstTime(boolean isFirstTime) {
        this.mIsFirstTime = isFirstTime;
    }
    
    public LruCache<String, Bitmap> getMemoryCache(){
        return mMemoryCache;
    }
    
    public void setMemoryCache(LruCache<String, Bitmap> memoryCache){
        this.mMemoryCache = memoryCache;
    }
}
