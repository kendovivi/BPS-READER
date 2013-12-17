
package bps.android.reader;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.zip.ZipException;

import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import bps.android.reader.book.BookInfo;
import bps.android.reader.book.BookManager;
import bps.android.reader.fragment.BookDetailsFragment;
import bps.android.reader.listadapter.BookAdapter;

import com.example.bps_reader.R;

public class BookShelfActivity extends Activity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            savedInstanceState.getInt("curPosition", 0);
        }
        
        try {
            initVar();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ZipException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (EpubOtherException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (EpubParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
    @SuppressLint("NewApi")
    void showFragment(int position) {
        mPosition = position;
        // if horizontal, show details in the right frame
        if (mIsDual) {
            FragmentManager manager = getFragmentManager();
            BookDetailsFragment bdf = (BookDetailsFragment)manager
                    .findFragmentById(R.id.detailsxxx);
            if (bdf == null || bdf.getBookId() != position) {

                bdf = BookDetailsFragment.newInstance(position);
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.detailsxxx, bdf);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }

            // if vertical, use showBookDetailsActivity to show book details
        } else {
            Intent intent = new Intent();
            intent.setClass(this, bps.android.reader.ShowBookDetailsActivity.class);
            intent.putExtra("bookId", position);
            startActivity(intent);
        }
    }

    /**
     * initialize variables
     * @throws EpubParseException 
     * @throws EpubOtherException 
     * @throws ZipException 
     * @throws FileNotFoundException 
     */
    private void initVar() throws FileNotFoundException, ZipException, EpubOtherException, EpubParseException {

        // portrait -> booklist_vertical, horizontal -> booklist_horizantal
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mIsHorizantal = false;
            mIsVertical = true;
            setContentView(R.layout.booklist_vertical);
        } else {
            mIsHorizantal = true;
            mIsVertical = false;
            setContentView(R.layout.booklist_horizantal);
            mBtn_read = (Button)findViewById(R.id.btn_read);
            mBtn_read.setOnClickListener(this);
        }

        if (mBM == null) {
            mBM = new BookManager();
            //mBM.initBookList();
            BookManager.getSDcardBookList();
        }
        mBookList = BookManager.getbookList();
        System.out.println(mBookList);
        mGridViewId = mIsVertical ? R.id.booklistgridv : R.id.booklistgridh;
        setGridView(mGridViewId);
        View detailshFrame = this.findViewById(R.id.detailsxxx);
        // mIsDual when horizontal, and it has the right frame
        mIsDual = detailshFrame != null && detailshFrame.getVisibility() == View.VISIBLE;
        if (mIsDual) {
            showFragment(mPosition);
        }

    }

    /**
     * set onClick event
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_read:
                Intent intent = new Intent();
                intent.setClass(this, ShowArticle.class);
                intent.putExtra("bookId", mPosition);
                intent.putExtra("pageNum", 0);
                startActivity(intent);
                break;
        }

    }

    private BookManager mBM;

    private int mPosition = 0;

    private boolean mIsHorizantal;

    private boolean mIsVertical;

    private boolean mIsDual;

    private int mGridViewId;

    private ArrayList<BookInfo> mBookList;

    private GridView mGrid;

    private BookAdapter mAdapter;

    private Button mBtn_read;
}
