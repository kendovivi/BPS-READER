
package bps.android.reader.application;

import java.util.ArrayList;

import android.app.Application;
import bps.android.reader.book.BookInfo;

public class MyApplication extends Application {

    private ArrayList<BookInfo> mBookList;
    
    private ArrayList<String> mPathIgnoreList;

    private boolean mIsFirstTime = true;
    
    private int mCount;
    
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
    
    public void setCount(int count){
        this.mCount = count;
    }
    
    public int getCount(){
        return mCount;
    }
}
