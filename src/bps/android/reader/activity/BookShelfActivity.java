
package bps.android.reader.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;
import bps.android.reader.application.MyApplication;
import bps.android.reader.book.BookInfo;
import bps.android.reader.book.BookManager;
import bps.android.reader.cache.CacheManager;
import bps.android.reader.fragment.BookDetailsFragment;
import bps.android.reader.listadapter.BookAdapter;
import com.example.bps_reader.R;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.zip.ZipException;
import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;

public class BookShelfActivity extends FragmentActivity{

    private static final int DEFAULT_POSITION = 0;

    private BookManager mBookManager;

    private int mPosition;
    
    private boolean mIsVertical;

    private boolean mIsDual;

    private int mGridViewId;

    private ArrayList<BookInfo> mBookList;

    private GridView mGrid;

    private BookAdapter mAdapter;

    private MyApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            savedInstanceState.getInt("curPosition", 0);
        }
        initVar();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.menu_add:
                intent.setClass(this, ChangeListGridActivity.class);
                this.startActivity(intent);
                break;
            case R.id.clear_cache:
                CacheManager.clear();
                Toast.makeText(this, "cache cleared!", Toast.LENGTH_SHORT).show();
            default:
                break;
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curPosition", mPosition);
    }

    /**
     * add booklist into specified GridVeiw, then set click event for each book
     * 
     * @param gridViewId
     */
    void setGridView(int gridViewId) {
        mGrid = (GridView)this.findViewById(gridViewId);
        mAdapter = new BookAdapter(this, gridViewId, mBookList, BookAdapter.GRID);
        mGrid.setAdapter(mAdapter);
        mGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                showFragment(position);
            }
        });
    }

    /**
     * show bookDetail
     * 
     * @param position
     */
    void showFragment(int position) {
        mPosition = position;
        
        // if horizontal, show details in the right frame
        if (mIsDual) {
            FragmentManager manager = getSupportFragmentManager();
            BookDetailsFragment bdf = (BookDetailsFragment)manager
                    .findFragmentById(R.id.detailsxxx);
            if (bdf == null || bdf.getBookId() != position) {
                bdf = BookDetailsFragment.newInstance(position);
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.detailsxxx, bdf).commit();
            }
            // if vertical, use showBookDetailsActivity to show book details
        } else {
            Intent intent = new Intent();
            intent.setClass(this, bps.android.reader.activity.ShowBookDetailsActivity.class);
            intent.putExtra("bookId", position);
            startActivity(intent);
        }
    }

    /**
     * initialize variables
     * 
     * @throws EpubParseException
     * @throws EpubOtherException
     * @throws ZipException
     * @throws FileNotFoundException
     */
    private void initVar() {

        mApplication = (MyApplication)this.getApplicationContext();
        mBookManager = new BookManager(this);
        // portrait -> booklist_vertical, horizontal -> booklist_horizantal
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mIsVertical = true;
            setContentView(R.layout.booklist_vertical);
        } else {
            mIsVertical = false;
            setContentView(R.layout.booklist_horizantal);
        }

        if (mApplication.getIsFirstTime()) {
            mBookList = mBookManager.getSDcardBookList();
            // sort by name length test
            // BookSort bs = new BookSort();
            // bs.getSortedList(this, mBookList);
            mApplication.setIsFirstTime(false);
            mBookManager.setAppBookList(mBookList);
        } else {
            mBookList = mBookManager.getAppBookList();
        }
        mPosition = DEFAULT_POSITION;
        mGridViewId = mIsVertical ? R.id.booklistgridv : R.id.booklistgridh;
        setGridView(mGridViewId);
        View detailshFrame = this.findViewById(R.id.detailsxxx);
        // mIsDual when horizontal, and it has the right frame
        mIsDual = detailshFrame != null && detailshFrame.getVisibility() == View.VISIBLE;
        if (mIsDual) {
            showFragment(mPosition);
        }
    }
    
   
    
}
