
package bps.android.reader.activity;

import java.util.ArrayList;

import jp.bpsinc.android.viewer.epub.activity.EpubViewerActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import bps.android.reader.book.BookInfo;
import bps.android.reader.book.BookManager;
import bps.android.reader.fragment.BookDetailsFragment;

import com.example.bps_reader.R;

public class ShowBookDetailsActivity extends Activity implements OnClickListener {

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentBookId = getIntent().getIntExtra("bookId", 0);
        mBookManager = new BookManager();
        try {
            mBookList = mBookManager.getSDcardBookList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.bookdetails_vertical);
        BookDetailsFragment bdf = new BookDetailsFragment();
        bdf.setArguments(getIntent().getExtras());
        FragmentManager manager = getFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(R.id.detailsv, bdf);
        ft.commit();

        btn_read = (Button)findViewById(R.id.btn_read);
        btn_read.setOnClickListener(this);
        

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.btn_read:
                // intent.setClass(this, FxlEpubViewerActivity.class);
                intent.setClass(this, SampleDialogShelfActivity.class);
                intent.putExtra("bookId", mCurrentBookId);
                intent.putExtra(EpubViewerActivity.INTENT_KEY_EPUB_CONTENTS,
                        mBookList.get(mCurrentBookId));
                startActivity(intent);
                break;
        }

    }

    private Button btn_read;

    private BookManager mBookManager;

    private int mCurrentBookId;

    private ArrayList<BookInfo> mBookList;

}