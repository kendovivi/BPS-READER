
package bps.android.reader.fragment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.zip.ZipException;

import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.activity.EpubViewerActivity;
import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import bps.android.reader.activity.SampleDialogShelfActivity;
import bps.android.reader.book.BookInfo;
import bps.android.reader.book.BookManager;

import com.example.bps_reader.R;

public class BookDetailsFragment extends Fragment implements OnClickListener {

    private ArrayList<BookInfo> mBookList;

    private BookManager mBookManager;

    private Bitmap mCoverBitmap;

    private Button btn_read;

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
        mBookList = mBookManager.getAppBookList(this.getActivity());
        if (container == null) {
            return null;
        }
        getActivity().getLayoutInflater().inflate(R.layout.details, container);
        TextView v = new TextView(getActivity());
        TextView bookName = (TextView)getActivity()
                .findViewById(R.id.book_fragment_horizontal_name);
        TextView bookAuthor = (TextView)getActivity().findViewById(
                R.id.book_fragment_horizontal_author);
        TextView bookPublisher = (TextView)getActivity().findViewById(
                R.id.book_fragment_horizontal_publisher);
        TextView bookPTime = (TextView)getActivity().findViewById(
                R.id.book_fragment_horizontal_ptime);
        ImageView bookCover = (ImageView)getActivity().findViewById(
                R.id.book_fragment_horizontal_image);
        btn_read = (Button)getActivity().findViewById(R.id.book_fragment_horizontal_btn_read);

        // set details bookCover bitmap

        try {
            mCoverBitmap = mBookManager.getBookBmp(getBookId());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (EpubOtherException e) {
            e.printStackTrace();
        } catch (EpubParseException e) {
            e.printStackTrace();
        }
        if (mCoverBitmap == null) {
            int coverViewId = getResources().getIdentifier(
                    "drawable/" + mBookList.get(getBookId()).getmImgURLH(), "drawable",
                    getActivity().getPackageName());
            coverViewId = coverViewId == 0 ? R.drawable.default_book_cover : coverViewId;
            // set to default book cover
            mCoverBitmap = BitmapFactory.decodeResource(getActivity().getResources(), coverViewId);
        }

        bookCover.setImageBitmap(mCoverBitmap);

        v.setPadding(5, 5, 5, 5);
        bookName.setText("本名　: " + mBookList.get(getBookId()).getmName());
        bookAuthor.setText("作者　:　" + mBookList.get(getBookId()).getmAuthor());
        bookPublisher.setText("出版社　: " + mBookList.get(getBookId()).getmPublisher());
        bookPTime.setText("出版時間　: " + mBookList.get(getBookId()).getmPtime());
        btn_read.setOnClickListener(this);
        btn_read.setEnabled(true);
        return v;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.book_fragment_horizontal_btn_read:
                intent.setClass(getActivity(), SampleDialogShelfActivity.class);
                intent.putExtra("bookId", getBookId());
                intent.putExtra(EpubViewerActivity.INTENT_KEY_EPUB_CONTENTS,
                        mBookList.get(getBookId()));
                startActivity(intent);
                break;
        }
    }

}
