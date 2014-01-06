
package bps.android.reader.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;

public class BookSort {
    
    public ArrayList<BookInfo> getSortedList(Activity activity, ArrayList<BookInfo> list){
        
        
         Collections.sort(list, new BookComparator());
         return list;
    }
    
    class BookComparator implements Comparator<BookInfo>{

        
        @Override
        public int compare(BookInfo book1, BookInfo book2) {
            return book1.getmName().length() > book2.getmName().length()?  1  : -1;
        }
        
    }
}
