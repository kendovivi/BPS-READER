package bps.android.reader.fragment;

import java.util.ArrayList;

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

import com.example.bps_reader.R;

public class BookDetailsFragment extends Fragment {
    
    private ArrayList<BookInfo> mBookList = new ArrayList<BookInfo>() {
        {

            add(new BookInfo(1, "colorful", "matsu", "publisher", "20010608",
                    "colorful", "colorful", ""));
            add(new BookInfo(2, "illustrator_rules", "Michale", "publisher", "20010809",
                    "illustrator_rules", "illustrator_rules", ""));
            add(new BookInfo(3, "praysrelease", "Riri", "publisher", "20010809",
                    "praysrelease", "praysrelease", ""));
            add(new BookInfo(4, "servertech", "Square Enix", "publisher", "20010809",
                    "servertech", "servertech", ""));
            add(new BookInfo(5, "androidforbeginners", "nippo", "publisher", "20010809",
                    "androidforbeginners", "androidforbeginners", ""));
            add(new BookInfo(6, "jenkinsforbeginners", "nippo", "publisher", "20010809",
                    "jenkinsforbeginners", "jenkinsforbeginners", ""));
            add(new BookInfo(7, "fireworks_cs4", "nippo", "publisher", "20010809",
                    "fireworks_cs4", "fireworks_cs4", ""));
            add(new BookInfo(8, "book_rails", "nippo", "publisher", "20010809",
                    "book_rails", "book_rails", ""));
            add(new BookInfo(9, "no pic book", "nippo", "publisher", "20010809",
                    "no pic book", "no pic book", ""));
            add(new BookInfo(10, "javarestful", "nippo", "publisher", "20010809",
                    "javarestful", "javarestful", ""));
            add(new BookInfo(11, "javascript5th", "nippo", "publisher", "20010809",
                    "javascript5th", "javascript5th", ""));
        }
    };
    
    public static BookDetailsFragment newInstance(int bookId) {
        BookDetailsFragment bf = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putInt("bookId", bookId);
        bf.setArguments(args);
        return bf;
    }
    
    public int getBookId(){
        return getArguments().getInt("bookId");
    }
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        TextView v = new TextView(getActivity());
        TextView bookName = (TextView) getActivity().findViewById(R.id.bookName);
        TextView bookAuthor = (TextView)getActivity().findViewById(R.id.bookAuthor);
        TextView bookPublisher = (TextView)getActivity().findViewById(R.id.bookPublisher);
        TextView bookPTime = (TextView)getActivity().findViewById(R.id.bookPTime);
        TextView bookPrice = (TextView)getActivity().findViewById(R.id.bookPrice);
        //need fix
        ImageView bookCover = (ImageView)getActivity().findViewById(R.id.imageView1);
        int coverViewId = getResources().getIdentifier("drawable/" + mBookList.get(getBookId()).getmImgURLH(), "drawable", getActivity().getPackageName());
        coverViewId = coverViewId == 0? R.drawable.default_book_cover : coverViewId;
        Bitmap bookcover = BitmapFactory.decodeResource(getResources(), coverViewId);
        v.setPadding(5, 5, 5, 5);
        System.out.println(bookName);
        bookName.setText("本名　:　" + mBookList.get(getBookId()).getmName());
        bookAuthor.setText("作者　:　" + mBookList.get(getBookId()).getmAuthor());
        bookPublisher.setText("出版社　: " + mBookList.get(getBookId()).getmPublisher());
        bookPTime.setText("出版時間　: " + mBookList.get(getBookId()).getmPtime());
        bookCover.setImageBitmap(bookcover);
        return v;
    }
}
