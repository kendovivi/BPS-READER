
package bps.android.reader.book;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipException;

import jp.bpsinc.android.viewer.epub.content.EpubFile;
import jp.bpsinc.android.viewer.epub.content.EpubZipFile;
import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.Environment;
import android.util.Log;
import bps.android.reader.xmlparser.MyParser;

public class BookManager {

    private static ArrayList<BookInfo> mBookList;

    public static ArrayList<BookInfo> getbookList() {
        return BookManager.mBookList;
    }

    /**
     * get bookList from bookres.xml
     * 
     * @param
     * @return
     */
    public void initBookList() {
        ArrayList<BookInfo> bookList = new ArrayList<BookInfo>();
        try {
            XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            InputStream in_s = this.getClass().getResourceAsStream("/assets/bookres.xml");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);
            mBookList = MyParser.parseXMLtoBookList(parser);
            System.out.println(bookList);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getSDcardBookList() throws FileNotFoundException, ZipException,
            EpubOtherException, EpubParseException {
        String SDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String[] files = new File(SDCardPath).list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return (filename != null && filename.endsWith(".epub"));
                    }
                });
        BookInfo bookInfo;
        mBookList = new ArrayList<BookInfo>();
        for (int i = 0; i < files.length; i++) {
            bookInfo = new BookInfo();
            EpubZipFile epubZipFile = new EpubZipFile(new File(SDCardPath, files[i]).getPath());
            EpubFile epubFile = new EpubFile(epubZipFile, null);
            bookInfo.setmName(epubFile.getTitle());
            bookInfo.setmAuthor(epubFile.getAuthor());
            bookInfo.setmPublisher(epubFile.getOpfMeta().getPublisher());
            bookInfo.setmEpubPath(new File(SDCardPath, files[i]).getPath());
            mBookList.add(bookInfo);
        }

        // Log.d("booklist size = ", mBookList.size()+ "");

    }
}
