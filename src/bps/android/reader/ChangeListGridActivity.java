
package bps.android.reader;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import bps.android.reader.book.BookInfo;
import bps.android.reader.book.BookManager;
import bps.android.reader.listadapter.BookAdapter;

import com.example.bps_reader.R;

public class ChangeListGridActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booklist_listview);     
        mBookList = BookManager.getbookList();
        setListView(R.id.book_listview);

    }

    void setListView(int listViewId) {
        mListView = (ListView)findViewById(listViewId);
        mListView.setAdapter(new BookAdapter(this, listViewId, mBookList, BookAdapter.LIST));
        mListView.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), bps.android.reader.ShowBookDetailsActivity.class);
                intent.putExtra("bookId", position);
                startActivity(intent);
            }
            
        });
    }

    private ListView mListView;

    private ArrayList<BookInfo> mBookList;
}
