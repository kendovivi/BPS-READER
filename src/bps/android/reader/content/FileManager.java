
package bps.android.reader.content;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.content.Context;

public class FileManager {

    private ArrayList<String> mPageList;

    private int mTotalPages;

    private int mByte;

    public String getTextContent(Context context, int bookId, int pageId, String bookName)
            throws UnsupportedEncodingException, IOException {
        mByte = 550;
        mPageList = new ArrayList<String>();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        InputStream input = context.getAssets().open("textSrc/" + bookName + ".txt");
        byte[] data = new byte[mByte];
        @SuppressWarnings("unused")
        int count = -1;
        while ((count = input.read(data, 0, mByte)) != -1) {
            outStream.reset();
            outStream.write(data, 0, mByte);
            String s = new String(outStream.toByteArray(), "UTF-8");
            mPageList.add(s);

        }
        mTotalPages = mPageList.size();
        return mPageList.get(pageId);
        // Log.d("bps-reader", pageList.size() + "");
    }

    public int getTotalPagesNum() {
        return this.mTotalPages;
    }

}
