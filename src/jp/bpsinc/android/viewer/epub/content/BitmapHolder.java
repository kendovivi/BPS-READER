package jp.bpsinc.android.viewer.epub.content;

import java.io.InputStream;

import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.exception.LoadImageException;
import jp.bpsinc.android.viewer.util.Size;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

public class BitmapHolder {
	/** Bitmap */
	private Bitmap mBitmap;
	/**
	 * 読み込んだBitmapのディスプレイフィットスケール、縦フィット、横フィット、見開きの設定などに応じて値は変化、
	 * 見開きの場合、左右で画像サイズが違う場合は大きい方の画像のディスプレイフィットスケールを格納する、
	 * この場合小さい方の画像は縦横どちらもディスプレイにフィットしない
	 */
	private float mFitScale;
	/** ビットマップを原寸デコードした場合のサイズ */
	private Size mOriginalSize;
	/** Bitmapのステータス情報取得時のオプション指定 */
	private Options mOptions;

	/** BITMAP拡大率の初期値 */
	public static final float DISPLAY_FIT_DEFAULT_SCALE = 1.0f;

	public BitmapHolder() {
		LogUtil.v();
		mBitmap = null;
		mFitScale = DISPLAY_FIT_DEFAULT_SCALE;
		mOriginalSize = null;
		mOptions = new Options();
		// Bitmapのステータス情報のみ取得に設定
		mOptions.inJustDecodeBounds = true;
	}

	public synchronized void cleanupBitmap() {
		LogUtil.v();
		if (mBitmap != null && mBitmap.isRecycled() == false) {
			mBitmap.recycle();
		}
		mBitmap = null;
		mFitScale = DISPLAY_FIT_DEFAULT_SCALE;
	}

	public void setBitmap(Bitmap bitmap) {
		mBitmap = bitmap;
	}

	public Bitmap getBitmap() {
		return mBitmap;
	}

	public void setFitScale(float fitScale) {
		mFitScale = fitScale;
	}

	public float getFitScale() {
		return mFitScale;
	}

	public boolean hasBitmap() {
		return mBitmap != null && mBitmap.isRecycled() == false;
	}

	/**
	 * ストリームから画像をデコード(ステータス情報のみ取得設定)し、取得したサイズを設定する
	 * 
	 * @param bitmapStream 画像のストリーム
	 * @throws LoadImageException デコード失敗
	 */
	public void setOriginalSize(InputStream bitmapStream) throws LoadImageException {
		// デコード失敗時は前回のmimetypeなどが上書きされず前回の情報が残ってしまうため初期化
		mOptions.outMimeType = null;
		BitmapFactory.decodeStream(bitmapStream, null, mOptions);
		// ファイルのデコードに失敗した(対応しないMIMEタイプだった)ら例外をthrow
		if (mOptions.outMimeType == null) {
			throw new LoadImageException("failed to decode bitmap.");
		}
		mOriginalSize =  new Size(mOptions.outWidth, mOptions.outHeight);
	}

	public Size getOriginalSize() {
		return mOriginalSize;
	}

	public boolean isOriginalSize() {
		return mBitmap.getWidth() == mOriginalSize.width && mBitmap.getHeight() == mOriginalSize.height;
	}

	public float getFitScaleWidth() {
		return mBitmap.getWidth() * mFitScale;
	}

	public float getFitScaleHeight() {
		return mBitmap.getHeight() * mFitScale;
	}
}
