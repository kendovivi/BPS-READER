
package bps.android.reader.listadapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipException;

import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
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

    

    public BookAdapter(Activity a, int textViewResourceId, ArrayList<BookInfo> entries, int type) {
        super(a, textViewResourceId, entries);
        this.activity = a;
        this.mEntries = entries;
        this.mType = type;
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
        mBookManager = new BookManager();
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)activity
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
                mBookCover = mBookManager.getBookBmp(position);
                
                if (mBookCover == null) {

                    coverViewId = activity.getResources().getIdentifier(
                            "drawable/" + book.getmImgURLH(), "drawable", activity.getPackageName());
                    // coverViewId = coverViewId == 0 ? defaultCoverViewId :
                    // coverViewId;
                    mBookCover = BitmapFactory.decodeResource(activity.getResources(), coverViewId);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ZipException e) {
                e.printStackTrace();
            } catch (EpubOtherException e) {
                e.printStackTrace();
            } catch (EpubParseException e) {
                e.printStackTrace();
            }
            holder.bookCover.setImageBitmap(mBookCover);
            if (mType == LIST) {
                holder.bookName.setText("【" + book.getmName() + "】");
                holder.bookAuthor.setText("　本名:　" + book.getmAuthor());
                holder.bookPublisher.setText("　出版社: " + book.getmPublisher());
                holder.bookIssueDate.setText("　出版時間: " + book.getmPtime());
            }
        }
        return v;
    }
    
    private Activity activity;

    private Bitmap mBookCover;

    ArrayList<BookInfo> mEntries;

    private int coverViewId;

    //private int defaultCoverViewId = R.drawable.default_book_cover;

    private int mType;
    
    private BookManager mBookManager;

    public static final int LIST = 1;

    public static final int GRID = 2;

}
