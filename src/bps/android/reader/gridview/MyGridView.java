
package bps.android.reader.gridview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.GridView;

import com.example.bps_reader.R;

public class MyGridView extends GridView {

    private Bitmap mBackgroundBitmap;

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bookshelf_wood);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        int count = getChildCount();
        int top = count > 0 ? getChildAt(0).getTop() : 0;
        int backgroundWidth = mBackgroundBitmap.getWidth();
        int backgroundHeight = mBackgroundBitmap.getHeight();
        int width = getWidth();
        int height = getHeight();

        for (int y = top; y < height; y += backgroundHeight) {
            for (int x = 0; x < width; x += backgroundWidth) {
                canvas.drawBitmap(mBackgroundBitmap, x, y, null);
            }
        }

        super.dispatchDraw(canvas);
    }

}
