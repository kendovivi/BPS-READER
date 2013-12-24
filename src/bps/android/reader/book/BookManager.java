
package bps.android.reader.book;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipException;

import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.content.EpubZipFile;
import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import bps.android.reader.application.MyApplication;
import bps.android.reader.listadapter.BookshelfEpubFile;
import bps.android.reader.listadapter.BookshelfEpubPageAccess;
import bps.android.reader.xmlparser.MyParser;

public class BookManager {

    private ArrayList<BookInfo> mBookList;

    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath();

    public static String[] mFiles;

    /**
     * get bookList from bookres.xml
     * 
     * @param
     * @return
     */
    public ArrayList<BookInfo> getBookList() {
        mBookList = new ArrayList<BookInfo>();
        try {
            XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            InputStream in_s = this.getClass().getResourceAsStream("/assets/bookres.xml");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);
            mBookList = MyParser.parseXMLtoBookList(parser);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mBookList;
    }

    /**
     * get bookList from SDcard
     * 
     * @throws FileNotFoundException
     * @throws ZipException
     * @throws EpubOtherException
     * @throws EpubParseException
     */
    public ArrayList<BookInfo> getSDcardBookList() {

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
            EpubZipFile epubZipFile;
            try {
                epubZipFile = new EpubZipFile(new File(SDCARD_PATH, mFiles[i]).getPath());
                BookshelfEpubFile epubFile;
                epubFile = new BookshelfEpubFile(epubZipFile, null);
                bookInfo.setmName(epubFile.getTitle());
                bookInfo.setmAuthor(epubFile.getAuthor());
                bookInfo.setmPublisher(epubFile.getOpfMeta().getPublisher());
                bookInfo.setmEpubPath(new File(SDCARD_PATH, mFiles[i]).getPath());
                mBookList.add(bookInfo);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ZipException e) {
                e.printStackTrace();
            } catch (EpubOtherException e) {
                e.printStackTrace();
            } catch (EpubParseException e) {
                e.printStackTrace();
            }
        }
        return mBookList;
    }

    /**
     * get bitmap from epub opf file
     * 
     * @param position
     * @return
     * @throws FileNotFoundException
     * @throws ZipException
     * @throws EpubOtherException
     * @throws EpubParseException
     */
    public Bitmap getBookBmp(int position) throws FileNotFoundException, ZipException,
            EpubOtherException, EpubParseException {
        Bitmap bmp;
        EpubZipFile epubZipFile;

        epubZipFile = new EpubZipFile(new File(SDCARD_PATH, mFiles[position]).getPath());
        BookshelfEpubFile epubFile = new BookshelfEpubFile(epubZipFile, null);
        BookshelfEpubPageAccess pageaccess = new BookshelfEpubPageAccess(epubZipFile, epubFile);
        InputStream is = pageaccess.getInputStream(epubFile.getCoverItem());
        //LogUtil.e(epubFile.getCoverItem().getMediaType() + "  book title: " + epubFile.getTitle());

        bmp = BitmapFactory.decodeStream(is);
        return epubFile.getCoverItem().isCoverImage() ? bmp : null;

    }
    
    /**
     * save the bookList load from SDcard into application context
     * 
     * @param activity
     * @param booklist
     */
    public void setAppBookList(Activity activity, ArrayList<BookInfo> booklist){
        MyApplication application = (MyApplication) activity.getApplicationContext();
        application.setBookList(booklist);
    }
    
    /**
     * get the bookList from application context
     * 
     * @param activity
     * @return
     */
    public ArrayList<BookInfo> getAppBookList(Activity activity){
        MyApplication application = (MyApplication) activity.getApplicationContext();
        return application.getBookList();
    }
}
