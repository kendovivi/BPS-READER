
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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.widget.ImageView;
import bps.android.reader.application.MyApplication;
import bps.android.reader.cache.CacheManager;
import bps.android.reader.listadapter.BookshelfEpubFile;
import bps.android.reader.listadapter.BookshelfEpubPageAccess;
import bps.android.reader.xmlparser.MyParser;

public class BookManager {

    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath();

    private ArrayList<BookInfo> mBookList;

    /** paths of files that do NOT contain a customized cover image */
    private ArrayList<String> mPathIgnoreList;

    private String[] mFiles;

    private LruCache<String, Bitmap> mMemoryCache;

    private MyApplication mApplication;

    public BookManager(Activity activity) {
        this.mApplication = (MyApplication)activity.getApplicationContext();
    }

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
    public void setAppBookList(ArrayList<BookInfo> booklist) {
        mApplication.setBookList(booklist);
    }

    /**
     * get the bookList from application context
     * 
     * @param activity
     * @return
     */
    public ArrayList<BookInfo> getAppBookList() {
        return mApplication.getBookList();
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
    public void setBookBmp(int position, ImageView imageView, Bitmap defBookCover, Activity activity)
            throws FileNotFoundException, ZipException, EpubOtherException, EpubParseException {
        // loadBitmap(new File(SDCARD_PATH, mFiles[position]).getPath(),
        // imageView, activity);
        mBookList = mApplication.getBookList();
        loadBitmap(mBookList.get(position).getmEpubPath(), imageView, defBookCover, activity);
    }

    /**
     * check the imageView for whether there is a task running in background
     * 
     * @param newPath
     * @param imageView
     * @return
     */
    public boolean cancelCurrentTask(String newPath, ImageView imageView) {

        final BitmapWorkerTask task = getBitmapWorkerTask(imageView);
        if (task != null) {
            final String loadingImagePath = task.mPath;
            if (loadingImagePath != null) {
                // when different, cancel the old task and start the new one
                if (!loadingImagePath.equals(newPath)) {
                    task.cancel(true);
                } else {
                    // no need to run the same task again
                    return false;
                }
            }
        }
        // no task is running, start the task
        return true;
    }

    /**
     * define load book cover bitmap thread task
     * 
     * @author kendovivi
     */
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> mImageViewReference;

        private Bitmap mBitmap;
        
        private Bitmap mDefBookCover;

        String mPath;

        public BitmapWorkerTask(ImageView imageView, Bitmap defBookCover) {
            mImageViewReference = new WeakReference<ImageView>(imageView);
            this.mDefBookCover = defBookCover;
        }

        // start loading bitmap in background and return finished bitmap
        @Override
        protected Bitmap doInBackground(String... params) {
            mPathIgnoreList = mApplication.getPathIgnoreList();
            if (mPathIgnoreList == null) {
                mPathIgnoreList = new ArrayList<String>();
            }
            mPath = params[0];
            this.mBitmap = getEpubCoverBitmap(params[0]);
            if (mBitmap == null) {
                // add path to ignore list if the Epub file contains no cover image information
                mPathIgnoreList.add(mPath);
                mApplication.setPathIgnoreList(mPathIgnoreList);
            } else {
                addBitmapToMemoryCache(mPath, mBitmap);
            }
            return mBitmap;
        }

        // set imageView when bitmap loading is complete
        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (isCancelled()) {
                bitmap = null;
            } else if (mImageViewReference != null) {
                final ImageView imageView = mImageViewReference.get();
                // use default cover bitmap
                if (bitmap == null) {
                    bitmap = mDefBookCover;
                }

                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }

        // get EpubFile cover bitmap
        private Bitmap getEpubCoverBitmap(String path) {
            EpubZipFile epubZipFile;
            BookshelfEpubFile epubFile = null;
            InputStream is = null;
            BookshelfEpubPageAccess pageaccess;

            try {
                epubZipFile = new EpubZipFile(path);
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
    }

    /**
     * set a reference by bitmapDrawable for the task
     * 
     * @author kendovivi
     */
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask task) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(task);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }

    }

    /**
     * get task from imageView
     * 
     * @param imageView
     * @return
     */
    private BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                AsyncDrawable asyncDrawable = (AsyncDrawable)drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }

        return null;
    }

    /**
     * load bitmap using AsyncDrawable and set finished part to imageView
     * 
     * @param path
     * @param imageView
     * @param activity
     */
    private void loadBitmap(String path, ImageView imageView, Bitmap defBookCover, Activity activity) {
        //get image from cache
        Bitmap bitmapFromCache = getBitmapFromMemoryCache(path);
        mPathIgnoreList = mApplication.getPathIgnoreList();
        if (bitmapFromCache != null) {
            //set image to view if the image is loaded successfully from cache 
            imageView.setImageBitmap(bitmapFromCache);
        } else if (mPathIgnoreList != null && mPathIgnoreList.contains(path)) {
            //if the path is in the ingoreList, use default cover image instead
            imageView.setImageBitmap(defBookCover);
        } else {
            // check whether the task is same or not
            if (cancelCurrentTask(path, imageView)) {
                BitmapWorkerTask task = new BitmapWorkerTask(imageView, defBookCover);
                AsyncDrawable asyncDrawable = new AsyncDrawable(activity.getApplicationContext()
                        .getResources(), task.mBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(path);
            }
        }
    }

    
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            // put bitmap into memory cache
            mMemoryCache.put(key, bitmap);
            CacheManager.setMemoryCacheForImage(mMemoryCache);
        }
    }

    private Bitmap getBitmapFromMemoryCache(String key) {
        mMemoryCache = CacheManager.getMemoryCacheForImage();
        // read bitmap from cache
        return mMemoryCache.get(key);
    }

}
