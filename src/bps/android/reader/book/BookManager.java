
package bps.android.reader.book;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.zip.ZipException;

import jp.bpsinc.android.viewer.epub.content.EpubZipFile;
import jp.bpsinc.android.viewer.epub.exception.EpubOtherException;
import jp.bpsinc.android.viewer.epub.exception.EpubParseException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;
import bps.android.reader.application.MyApplication;
import bps.android.reader.listadapter.BookshelfEpubFile;
import bps.android.reader.listadapter.BookshelfEpubPageAccess;
import bps.android.reader.xmlparser.MyParser;

import com.example.bps_reader.R;

public class BookManager {

    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath();

    private ArrayList<BookInfo> mBookList;

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
     * save the bookList load from SDcard into application context
     * 
     * @param activity
     * @param booklist
     */
    public void setAppBookList(Activity activity, ArrayList<BookInfo> booklist) {
        MyApplication application = (MyApplication)activity.getApplicationContext();
        application.setBookList(booklist);
    }

    /**
     * get the bookList from application context
     * 
     * @param activity
     * @return
     */
    public ArrayList<BookInfo> getAppBookList(Activity activity) {
        MyApplication application = (MyApplication)activity.getApplicationContext();
        return application.getBookList();
    }

    /**
     * set imageView of book cover
     * 
     * @param position
     * @param imageView
     * @param activity
     * @throws FileNotFoundException
     * @throws ZipException
     * @throws EpubOtherException
     * @throws EpubParseException
     */
    public void getBookBmp(int position, ImageView imageView, Activity activity)
            throws FileNotFoundException, ZipException, EpubOtherException, EpubParseException {
        loadBitmap(new File(SDCARD_PATH, mFiles[position]).getPath(), imageView, activity);
    }

    /**
     * define load book cover bitmap thread task
     * 
     * @author kendovivi
     */
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> mImageViewReference;

        private Activity mActivity;

        public BitmapWorkerTask(ImageView imageView, Activity activity) {
            mImageViewReference = new WeakReference<ImageView>(imageView);
            this.mActivity = activity;
        }

        // load bitmap in background
        @Override
        protected Bitmap doInBackground(String... params) {
            EpubZipFile epubZipFile;
            BookshelfEpubFile epubFile = null;
            InputStream is = null;

            BookshelfEpubPageAccess pageaccess;
            try {
                epubZipFile = new EpubZipFile(params[0]);
                epubFile = new BookshelfEpubFile(epubZipFile, null);
                pageaccess = new BookshelfEpubPageAccess(epubZipFile, epubFile);
                is = pageaccess.getInputStream(epubFile.getCoverItem());

            } catch (EpubOtherException e) {
                e.printStackTrace();
            } catch (ZipException e) {
                e.printStackTrace();
            } catch (EpubParseException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return epubFile.getCoverItem().isCoverImage() ? BitmapFactory.decodeStream(is) : null;

        }

        // set imageView when loading is complete
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (mImageViewReference != null) {
                final ImageView imageView = mImageViewReference.get();
                // use default cover bitmap
                if (bitmap == null) {
                    int defaultCoverViewId = R.drawable.default_book_cover;
                    bitmap = BitmapFactory.decodeResource(mActivity.getResources(),
                            defaultCoverViewId);
                }

                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    private void loadBitmap(String path, ImageView imageView, Activity activity) {
        BitmapWorkerTask task = new BitmapWorkerTask(imageView, activity);
        task.execute(path);
    }

}
