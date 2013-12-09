
package bps.android.reader.xmlparser;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import bps.android.reader.book.BookInfo;

public class MyParser {
    
    public static ArrayList<BookInfo> parseXMLtoBookList(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<BookInfo> bookList = null;
        int eventType = parser.getEventType();
        BookInfo currentBook = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    bookList = new ArrayList<BookInfo>();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("book")) {
                        currentBook = new BookInfo();
                    } else if (currentBook != null) {
                        if (name.equalsIgnoreCase("bookid")) {
                            currentBook.setmId(Integer.parseInt(parser.nextText()));
                        } else if (name.equalsIgnoreCase("bookname")) {
                            currentBook.setmName(parser.nextText());
                        } else if (name.equalsIgnoreCase("bookauthor")){
                            currentBook.setmAuthor(parser.nextText());
                        } else if (name.equalsIgnoreCase("bookpublisher")) {
                            currentBook.setmPublisher(parser.nextText());
                        } else if (name.equalsIgnoreCase("bookptime")) {
                            currentBook.setmPtime(parser.nextText());
                        } else if (name.equalsIgnoreCase("bookurlh")) {
                            currentBook.setmImgURLH(parser.nextText());
                        } else if (name.equalsIgnoreCase("bookarticle")) {
                            currentBook.setmArticle(parser.nextText());
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("book") && currentBook != null) {
                        bookList.add(currentBook);
                    }
                default:
                break;
            }
            eventType = parser.next();        
        }
        return bookList;
    }
    
    
}
