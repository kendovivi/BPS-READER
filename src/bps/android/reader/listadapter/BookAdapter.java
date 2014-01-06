
package bps.android.reader.listadapter;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.zip.ZipException;

import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import bps.android.reader.application.MyApplication;
import bps.android.reader.book.BookInfo;
import bps.android.reader.book.BookManager;

import com.example.bps_reader.R;

public class BookAdapter extends ArrayAdapter<BookInfo> {

    public static final int LIST = 1;

    public static final int GRID = 2;

    private Activity mActivity;

    private int mListViewType;

    private LruCache<String, Bitmap> mMemoryCache;

    public BookAdapter(Activity a, int textViewResourceId, ArrayList<BookInfo> entries, int type) {
        super(a, textViewResourceId, entries);
        this.mActivity = a;
        this.mListViewType = type;

        RetainFragment retainFragment = RetainFragment.findOrCreateRetainFragment(a
                .getFragmentManager());
        mMemoryCache = retainFragment.mRetainedCache;

        if (mMemoryCache == null) {
            // get max available memory size (bytes)
            int maxMemory = (int)Runtime.getRuntime().maxMemory(); // ## another way to get available memory (Mbytes)
                                                                   // Context context = a.getApplicationContext();
                                                                   // int maxMemory = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

            // use 1/8 of the available memory for memory cache
            int cacheSize = maxMemory / 8;
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // return bitmap.getByteCount(); //requires API 12 or higher
                    return bitmap.getRowBytes() * bitmap.getHeight();
                }
            };
            retainFragment.mRetainedCache = mMemoryCache;
        }

        MyApplication application = (MyApplication)a.getApplicationContext();
        application.setMemoryCache(mMemoryCache);
    }

    public static class RetainFragment extends Fragment {
        private static final String TAG = "RetainFragment";

        private LruCache<String, Bitmap> mRetainedCache;

        public RetainFragment() {
        }

        public static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
            RetainFragment fragment = (RetainFragment)fm.findFragmentByTag(TAG);
            if (fragment == null) {
                fragment = new RetainFragment();
                fm.beginTransaction().add(fragment, TAG).commit();
            }
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
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
        BookManager bookManager = new BookManager(mActivity);
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
                bookManager.setBookBmp(position, holder.bookCover, mActivity);
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
