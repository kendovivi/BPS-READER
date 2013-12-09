
package bps.android.reader;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import bps.android.reader.book.BookInfo;
import bps.android.reader.book.BookManager;

import com.example.bps_reader.R;

public class ShowArticle extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article);
        mBookList = BookManager.getbookList();
        mArticleView = (TextView)findViewById(R.id.article);
        int bookId = getIntent().getIntExtra("bookId", 0);
        mArticleView.setText(mBookList.get(bookId).getmArticle());

    }

    private TextView mArticleView;

    private ArrayList<BookInfo> mBookList;
}
