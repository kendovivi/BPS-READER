package bps.android.reader.listadapter;

import java.util.ArrayList;

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

import com.example.bps_reader.R;


public class BookAdapter extends ArrayAdapter<BookInfo>{
    
    private ArrayList<BookInfo> entries;
    private Activity activity;
    private Bitmap bookCover;
    private Context context;
    private String bookCoverPath;
    private int coverViewId;
    private int defaultCoverViewId = R.drawable.default_book_cover;
    private int count;
    public BookAdapter(Activity a, int textViewResourceId, ArrayList<BookInfo> entries){
        super(a, textViewResourceId,entries);
        this.activity = a;   
        this.entries = entries;
         
    }
    
    public static class ViewHolder {
        //private TextView item1;
        //private TextView item2;
        //private TextView item3;
        //private TextView item4;
        //private TextView item5;
        //private TextView item6;
        private TextView bookTitle;
        private ImageView bookCover;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
            //v = LayoutInflater.from(context).inflate(R.layout.book_list_item, null);
            LayoutInflater vi = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.book_list_item, null);
            holder = new ViewHolder();
            //holder.item1 = (TextView) v.findViewById(R.id.bookTitle);
            //holder.item2 = (TextView) v.findViewById(R.id.bookName);
            //holder.item3 = (TextView) v.findViewById(R.id.bookAuthor);
            //holder.item4 = (TextView) v.findViewById(R.id.bookPublisher);
            //holder.item5 = (TextView) v.findViewById(R.id.bookPTime);
            //holder.item6 = (TextView) v.findViewById(R.id.bookText);
            holder.bookTitle = (TextView) v.findViewById(R.id.bookTitle);
            holder.bookCover = (ImageView) v.findViewById(R.id.bookCover);
            v.setTag(holder);
        } else {
            holder = (ViewHolder)v.getTag();
        }
        
        final BookInfo book = getItem(position);
        if (book != null) {
            //holder.item1.setText(book.getmId() + "");
            //holder.item2.setText(book.getmName());
            //holder.item3.setText(book.getmAuthor());
            //holder.item4.setText(book.getmPublisher());
            //holder.item5.setText(book.getmPtime());
            //holder.item6.setText(book.getmText());
            
            coverViewId = activity.getResources().getIdentifier("drawable/" + book.getmImgURLH(),"drawable",activity.getPackageName());
            coverViewId = coverViewId == 0? defaultCoverViewId : coverViewId;
            bookCover = BitmapFactory.decodeResource(activity.getResources(), coverViewId);
            System.out.println(book.getmName() + " : " + book.getmImgURLH() );
            holder.bookTitle.setText(book.getmName());
            holder.bookCover.setImageBitmap(bookCover);
        }
        return v;
    }

}
