
package bps.android.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import bps.android.reader.book.BookInfo;
import bps.android.reader.fragment.BookDetailsFragment;
import bps.android.reader.listadapter.BookAdapter;
import bps.android.reader.xmlparser.MyParser;

import com.example.bps_reader.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class MainActivity extends Activity {

    private int mPosition = 0;

    private boolean mIsHorizantal;

    private boolean mIsVertical;

    private boolean mIsDual;

    private int mGridViewId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            initBookres();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mIsHorizantal = false;
            mIsVertical = true;
            setContentView(R.layout.booklist_vertical);
        } else {
            mIsHorizantal = true;
            mIsVertical = false;
            setContentView(R.layout.booklist_horizantal);
        }

        if (savedInstanceState != null) {
            savedInstanceState.getInt("curPosition", 0);
        }

        mGridViewId = mIsVertical ? R.id.booklistgridv : R.id.booklistgridh;
        setGridView(mGridViewId);
        View detailshFrame = this.findViewById(R.id.detailsxxx);
        mIsDual = detailshFrame != null && detailshFrame.getVisibility() == View.VISIBLE;
        if (mIsDual) {
            showFragment(mPosition);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curPosition", mPosition);
    }

    void setGridView(int gridViewId) {
        mGrid = (GridView)this.findViewById(gridViewId);
        mAdapter = new BookAdapter(this, gridViewId, mBookList);
        mGrid.setAdapter(mAdapter);
        mGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Toast.makeText(getApplicationContext(), "test1" + position, Toast.LENGTH_SHORT)
                        .show();
                showFragment(position);
            }
        });
    }

    void showFragment(int position) {
        mPosition = position;
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

        } else {
            Intent intent = new Intent();
            intent.setClass(this, bps.android.reader.ShowBookDetailsActivity.class);
            intent.putExtra("bookId", position);
            startActivity(intent);
        }
    }

    

    
    
    private ArrayList<BookInfo> mBookList;

    private GridView mGrid;

    private BookAdapter mAdapter;
    
    private void initBookres() throws XmlPullParserException, IOException {
        try {
            XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();

            InputStream in_s = this.getAssets().open("bookres.xml");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);
            mBookList = MyParser.parseXMLtoBookList(parser);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
