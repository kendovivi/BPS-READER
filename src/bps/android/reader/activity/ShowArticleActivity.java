
package bps.android.reader.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import bps.android.reader.book.BookInfo;
import bps.android.reader.book.BookManager;
import bps.android.reader.content.FileManager;

import com.example.bps_reader.R;

public class ShowArticleActivity extends Activity implements OnClickListener {

    private static final int NEXT = 1;

    private static final int LAST = 2;

    private ArrayList<BookInfo> mBookList;

    private BookManager mBookManager;

    private FileManager mFileManager;

    private int mBookId;

    private String mBookName;

    private int mPageNum;

    private int mTotalPageNum;

    private TextView mArticleView;

    private TextView mTotalPageView;

    private String mPageToShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article);

        init();

        try {
            getArticle(mBookId, mPageNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTotalPageNum = mFileManager.getTotalPagesNum();
        mArticleView.setText(mPageToShow);
        setPageNumView();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next_page:
                checkMove(NEXT);
                break;

            case R.id.last_page:
                checkMove(LAST);
                break;
        }
        try {
            getArticle(mBookId, mPageNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mArticleView.setText(mPageToShow);
        setPageNumView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void getArticle(int bookId, int pageNum) throws UnsupportedEncodingException,
            IOException {
        mFileManager = new FileManager();
        mPageToShow = mFileManager.getTextContent(this, bookId, pageNum, mBookName);
    }

    private void checkMove(int moveDirection) {
        switch (moveDirection) {
            case NEXT:
                if (mPageNum >= mTotalPageNum - 1) {
                    Toast.makeText(this, "this is the last page", Toast.LENGTH_LONG).show();
                } else {
                    mPageNum += 1;
                }
                break;
            case LAST:
                if (mPageNum == 0) {
                    Toast.makeText(this, "this is the first page", Toast.LENGTH_LONG).show();
                } else {
                    mPageNum -= 1;
                }
        }
    }

    private void setPageNumView() {
        mTotalPageView.setText((mPageNum + 1) + " page in " + mTotalPageNum + " pages");
    }

    private void init() {
        mBookId = getIntent().getIntExtra("bookId", 0);
        mPageNum = getIntent().getIntExtra("pageNum", 0);
        mBookList = mBookManager.getBookList();
        mBookName = mBookList.get(mBookId).getmName();
        mArticleView = (TextView)findViewById(R.id.article);
        mTotalPageView = (TextView)findViewById(R.id.total_page);
        Button btn_next = (Button)findViewById(R.id.next_page);
        Button btn_last = (Button)findViewById(R.id.last_page);
        btn_next.setOnClickListener(this);
        btn_last.setOnClickListener(this);
    }

}
