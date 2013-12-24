
package bps.android.reader.listadapter;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.zip.ZipException;

import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import bps.android.reader.book.BookInfo;
import bps.android.reader.book.BookManager;

import com.example.bps_reader.R;

public class BookAdapter extends ArrayAdapter<BookInfo> {

    public static final int LIST = 1;

    public static final int GRID = 2;

    private Activity mActivity;

    private Bitmap mBookCover;
    
    private int mListViewType;

    // private int defaultCoverViewId = R.drawable.default_book_cover;

    public BookAdapter(Activity a, int textViewResourceId, ArrayList<BookInfo> entries, int type) {
        super(a, textViewResourceId, entries);
        this.mActivity = a;
        this.mListViewType = type;
    }

    public static class ViewHolder {
        private TextView bookName;

        private TextView bookAuthor;

        private TextView bookPublisher;

        private TextView bookIssueDate;

        private ImageView bookCover;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BookManager bookManager = new BookManager();
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)mActivity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.book_list_item, null);
            holder = new ViewHolder();
            holder.bookName = (TextView)v.findViewById(R.id.bookName);
            holder.bookAuthor = (TextView)v.findViewById(R.id.bookAuthor);
            holder.bookPublisher = (TextView)v.findViewById(R.id.bookPublisher);
            holder.bookIssueDate = (TextView)v.findViewById(R.id.bookPTime);
            holder.bookCover = (ImageView)v.findViewById(R.id.bookCover);
            v.setTag(holder);
        } else {
            holder = (ViewHolder)v.getTag();
        }

        final BookInfo book = getItem(position);
        if (book != null) {
            try {
                bookManager.getBookBmp(position, holder.bookCover, mActivity);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ZipException e) {
                e.printStackTrace();
            } catch (EpubOtherException e) {
                e.printStackTrace();
            } catch (EpubParseException e) {
                e.printStackTrace();
            }
            if (mListViewType == LIST) {
                holder.bookName.setText("【" + book.getmName() + "】");
                holder.bookAuthor.setText("　本名:　" + book.getmAuthor());
                holder.bookPublisher.setText("　出版社: " + book.getmPublisher());
                holder.bookIssueDate.setText("　出版時間: " + book.getmPtime());
            }
        }
        return v;
    }

}
