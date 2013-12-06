package bps.android.reader;

import java.util.ArrayList;

import com.example.bps_reader.R;

import bps.android.reader.book.BookInfo;
import bps.android.reader.fragment.BookDetailsFragment;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.FragmentManager;


public class ShowBookDetailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookdetails_vertical);
        BookDetailsFragment bdf = new BookDetailsFragment();
        bdf.setArguments(getIntent().getExtras());
        FragmentManager manager = getFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(android.R.id.content, bdf);
        ft.commit();
    }
    
    
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
    
}