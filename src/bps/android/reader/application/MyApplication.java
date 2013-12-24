package bps.android.reader.application;

import java.util.ArrayList;

import bps.android.reader.book.BookInfo;

import android.app.Application;

public class MyApplication extends Application {
    
    private ArrayList<BookInfo> mBookList;
    private boolean mIsFirstTime = true;
    
    @Override
    public void onCreate(){
        super.onCreate();
    }
    
    public ArrayList<BookInfo> getBookList(){
        return mBookList;
    }
    
    public void setBookList(ArrayList<BookInfo> list){
        this.mBookList = list;
    }
    
    public boolean getIsFirstTime(){
        return mIsFirstTime;
    }
    
    public void setIsFirstTime(boolean isFirstTime){
        this.mIsFirstTime = isFirstTime;
    }
}
