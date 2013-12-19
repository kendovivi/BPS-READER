
package bps.android.reader.fragment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.zip.ZipException;

import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import bps.android.reader.book.BookInfo;
import bps.android.reader.book.BookManager;

import com.example.bps_reader.R;

public class BookDetailsFragment extends Fragment {

    private ArrayList<BookInfo> mBookList;

    private BookManager mBookManager;

    public static BookDetailsFragment newInstance(int bookId) {
        BookDetailsFragment bf = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putInt("bookId", bookId);
        bf.setArguments(args);
        return bf;
    }

    public int getBookId() {
        return getArguments().getInt("bookId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBookManager = new BookManager();
        mBookList = mBookManager.getSDcardBookList();
        if (container == null) {
            return null;
        }

        TextView v = new TextView(getActivity());
        TextView bookName = (TextView)getActivity().findViewById(R.id.bookName);
        TextView bookAuthor = (TextView)getActivity().findViewById(R.id.bookAuthor);
        TextView bookPublisher = (TextView)getActivity().findViewById(R.id.bookPublisher);
        TextView bookPTime = (TextView)getActivity().findViewById(R.id.bookPTime);
        ImageView bookCover = (ImageView)getActivity().findViewById(R.id.imageView1);

        int coverViewId = getResources().getIdentifier(
                "drawable/" + mBookList.get(getBookId()).getmImgURLH(), "drawable",
                getActivity().getPackageName());
        coverViewId = coverViewId == 0 ? R.drawable.default_book_cover : coverViewId;
        Bitmap bookcover = BitmapFactory.decodeResource(getResources(), coverViewId);
        v.setPadding(5, 5, 5, 5);
        bookName.setText("本名　:　" + mBookList.get(getBookId()).getmName());
        bookAuthor.setText("作者　:　" + mBookList.get(getBookId()).getmAuthor());
        bookPublisher.setText("出版社　: " + mBookList.get(getBookId()).getmPublisher());
        bookPTime.setText("出版時間　: " + mBookList.get(getBookId()).getmPtime());

        try {
            bookcover = mBookManager.getBookBmp(getBookId());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (EpubOtherException e) {
            e.printStackTrace();
        } catch (EpubParseException e) {
            e.printStackTrace();
        }
        bookCover.setImageBitmap(bookcover);
        return v;
    }
}
