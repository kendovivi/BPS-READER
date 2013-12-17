
package bps.android.reader.book;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipException;

import jp.bpsinc.android.viewer.epub.content.EpubZipFile;
import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import bps.android.reader.listadapter.BookshelfEpubFile;
import bps.android.reader.listadapter.BookshelfEpubPageAccess;
import bps.android.reader.xmlparser.MyParser;

public class BookManager {

    private static ArrayList<BookInfo> mBookList;

    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath();

    public static String[] mFiles;

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

    /**
     * get bookList from SDcard
     * 
     * @throws FileNotFoundException
     * @throws ZipException
     * @throws EpubOtherException
     * @throws EpubParseException
     */
    public static void getSDcardBookList() throws FileNotFoundException, ZipException,
            EpubOtherException, EpubParseException {

        mFiles = new File(SDCARD_PATH).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return (filename != null && filename.endsWith(".epub"));
            }
        });
        BookInfo bookInfo;
        mBookList = new ArrayList<BookInfo>();
        for (int i = 0; i < mFiles.length; i++) {

            bookInfo = new BookInfo();
            EpubZipFile epubZipFile = new EpubZipFile(new File(SDCARD_PATH, mFiles[i]).getPath());
            BookshelfEpubFile epubFile = new BookshelfEpubFile(epubZipFile, null);
            bookInfo.setmName(epubFile.getTitle());
            bookInfo.setmAuthor(epubFile.getAuthor());
            bookInfo.setmPublisher(epubFile.getOpfMeta().getPublisher());
            bookInfo.setmEpubPath(new File(SDCARD_PATH, mFiles[i]).getPath());
            mBookList.add(bookInfo);
            // Log.d("booklist size = ", epubFile.get );
        }
    }

    public static Bitmap getBookBmp(int position) throws EpubOtherException, EpubParseException, IOException {
        Bitmap bmp;
        EpubZipFile epubZipFile = new EpubZipFile(new File(SDCARD_PATH, mFiles[position]).getPath());
        BookshelfEpubFile epubFile = new BookshelfEpubFile(epubZipFile, null);
        BookshelfEpubPageAccess pageaccess = new BookshelfEpubPageAccess(epubZipFile, epubFile);
        InputStream is =  pageaccess.getInputStream(epubFile.getCoverItem());
        bmp = BitmapFactory.decodeStream(is);
        return bmp;
    }
}
