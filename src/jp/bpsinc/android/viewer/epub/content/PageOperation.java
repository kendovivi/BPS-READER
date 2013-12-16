package jp.bpsinc.android.viewer.epub.content;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.view.util.ViewMode;
import jp.bpsinc.android.viewer.epub.view.util.ViewMode.FitMode;
import jp.bpsinc.android.viewer.exception.LoadImageException;
import jp.bpsinc.android.viewer.util.Shadow;
import jp.bpsinc.android.viewer.util.Size;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.BitmapFactory.Options;

public class PageOperation {
	/**
	 * 新しい画像のデコードが完了し、適用する直前に呼ばれるリスナ。falseを返すと、適用を中止することができる。
	 */
	public interface OnBeforeReplaceHandler {
		public boolean onBeforeReplace();
	}

	/**
	 * Bitmap読み込み時の
	 */
	public enum LoadQuality {
		HIGH,
		NEUTRAL,
	}

	/**
	 * 初期ポジションの横位置設定
	 */
	public enum HorizontalAlign {
		CENTER,
		LEFT,
		RIGHT,
	}

	/**
	 * 初期ポジションの縦位置設定
	 */
	public enum VerticalAlign {
		TOP,
		MIDDLE,
		BOTTOM,
	}

	/** ページ情報 */
	private Page mPage;
	/** センター位置のBitmap情報 */
	private BitmapHolder mCenterBitmap;
	/** レフト位置のBitmap情報 */
	private BitmapHolder mLeftBitmap;
	/** ライト位置のBitmap情報 */
	private BitmapHolder mRightBitmap;
	/** Bitmap取得時のオプション指定 */
	private Options mOptions;
	/** 画像の描画設定 */
	private Paint mPaint;
	/** 画像の拡大率や描画位置の指定に使用 */
	private Matrix mMatrix;
	/** ページ内のポジション */
	private PointF mPosition;
	/** ユーザ操作で変更された拡大縮小率 */
	private float mScale;
	/** 初期ポジションの水平揃え位置 */
	private HorizontalAlign mHorizontalAlign;
	/** 初期ポジションの垂直揃え位置 */
	private VerticalAlign mVerticalAlign;
	/** ディスプレイフィットと原寸に差があるため、拡大時に差し替える必要があるときはtrueにするフラグ */
	private boolean mNeedReplaceOriginal;
	/** 初期化、または、reset直後はfalse、Bitmapを読み込んだあとtrueになる、画像差し替え時などはtrueの状態 */
	private boolean mIsFirstLoadFinished;
	/** 縮小画像を一時的に保存しておくのに使う。描画直後にrecycleすると、ICSにて謎の画像が混ざる現象が発生するため */
	private List<Bitmap> mTemporaryBitmaps;

	/** 表示領域のサイズ */
	private final Size mDisplaySize;
	/** Viewのモード設定 */
	private final ViewMode mViewMode;
	/** 画像の周りに影描画するための機能を持つ */
	private final Shadow mShadow;

	/** 初期ポジションの水平揃え位置のデフォルト値 */
	private static final HorizontalAlign DEFAULT_HORIZONTAL_ARIGN = HorizontalAlign.CENTER;
	/** 初期ポジションの垂直揃え位置のデフォルト値 */
	private static final VerticalAlign DEFAULT_VERTICAL_ALIGN = VerticalAlign.MIDDLE;
	/** ページ拡大率の初期値 */
	public static final float PAGE_DEFAULT_SCALE = 1.0f;
	/** ditherするとグラデーションの粗が目立たなくなるが、代わりにギザギザ・モアレが目立つ */
	private static final boolean OPTIONS_DITHER = false;
	/** 画像拡大縮小時のフィルター設定ON */
	private static final boolean BITMAP_FILTER_SCALE = true;
	/** このサイズを超えたら強制的に縮小する */
	public static final int MAX_TEXTURE_SIZE = 2048;
	/** このサイズを超えたら強制的に縮小するサイズ */
	private static final Size mMaxSize = new Size(MAX_TEXTURE_SIZE, MAX_TEXTURE_SIZE);
	/** ディスプレイフィットデコードする場合などに使用、これより大きなサイズにはデコードしない */
	private final Size mMaxDisplaySize;

	public PageOperation(Size displaySize, ViewMode viewMode) {
		LogUtil.v();
		int displayWidth = displaySize.width;
		int displayHeight = displaySize.height;
		if (displayWidth < 1 || displayHeight < 1) {
			throw new IllegalArgumentException("display size must be positive, displayWidth=" + displayWidth + " displayHeight=" + displayHeight);
		}
		if (displayWidth > MAX_TEXTURE_SIZE || displayHeight > MAX_TEXTURE_SIZE) {
			float scale = Math.max(displayWidth / MAX_TEXTURE_SIZE, displayHeight / MAX_TEXTURE_SIZE);
			displayWidth /= scale;
			displayHeight /= scale;
		}
		mDisplaySize = displaySize;
		mMaxDisplaySize = new Size(displayWidth, displayHeight);
		mCenterBitmap = new BitmapHolder();
		mLeftBitmap = new BitmapHolder();
		mRightBitmap = new BitmapHolder();
		mOptions = new Options();
		mOptions.inDither = OPTIONS_DITHER;
		mOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		mPaint = new Paint();
		mMatrix = new Matrix();
		mPosition = new PointF(0, 0);
		mViewMode = viewMode;
		mScale = PAGE_DEFAULT_SCALE;
		mHorizontalAlign = DEFAULT_HORIZONTAL_ARIGN;
		mVerticalAlign = DEFAULT_VERTICAL_ALIGN;
		mIsFirstLoadFinished = false;
		mTemporaryBitmaps = new ArrayList<Bitmap>();

		switch (mViewMode.getContentMode()) {
		case OMF:
			mShadow = new Shadow();
			break;
		default:
			mShadow = null;
			break;
		}
	}

	public void reset() {
		mCenterBitmap.cleanupBitmap();
		mLeftBitmap.cleanupBitmap();
		mRightBitmap.cleanupBitmap();
		setScale(PAGE_DEFAULT_SCALE);
		mIsFirstLoadFinished = false;
	}

	/**
	 * このページが見開きページか判定する。この値はページ単位に異なり、ビューアの見開き設定とは異なる。
	 * 
	 * @return 見開きページならtrue、単ページならfalse(Pageオブジェクトがセットされていない場合もfalse)
	 */
	public boolean isPageSpread() {
		return mPage != null && mPage.getCenterOpfItem() == null;
	}
	/**
	 * 画像を読み込み済みか判定する
	 * 
	 * @return 画像読み込み済みならtrue、それ以外ならfalseを返す
	 */
	public synchronized boolean hasBitmap() {
		// ここのifはisSpreadじゃダメ
		if (isPageSpread()) {
			return mLeftBitmap.hasBitmap() || mRightBitmap.hasBitmap();
		} else {
			return mCenterBitmap.hasBitmap();
		}
	}

	public void onChangeBitmap() {
		LogUtil.d("%s, %s, %s", getCenterBitmap(), getLeftBitmap(), getRightBitmap());
		float minimumScale = getMinimumScale();
		if (getCenterBitmap() != null) {
			mCenterBitmap.setFitScale(minimumScale);
		}
		if (getLeftBitmap() != null) {
			mLeftBitmap.setFitScale(minimumScale);
		}
		if (getRightBitmap() != null) {
			mRightBitmap.setFitScale(minimumScale);
		}

		if (mIsFirstLoadFinished == false) {
			// 初回読み込み時のみポジション設定、画像拡大などで差し替えが発生した場合はポジション変えたらまずい
			setDefaultPosition();
			mIsFirstLoadFinished = true;
		}
	}

	public void setPage(Page page) {
		mPage = page;
	}

	public Page getPage() {
		return mPage;
	}

	public ViewMode getViewMode() {
		return mViewMode;
	}

	public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
		mHorizontalAlign = horizontalAlign;
	}

	public void setVerticalAlign(VerticalAlign verticalAlign) {
		mVerticalAlign = verticalAlign;
	}

	private Bitmap getCenterBitmap() {
		if (mCenterBitmap.hasBitmap()) {
			return mCenterBitmap.getBitmap();
		}
		return null;
	}

	public Bitmap getLeftBitmap() {
		if (mLeftBitmap.hasBitmap()) {
			return mLeftBitmap.getBitmap();
		}
		return null;
	}

	public Bitmap getRightBitmap() {
		if (mRightBitmap.hasBitmap()) {
			return mRightBitmap.getBitmap();
		}
		return null;
	}

	public float getLeftBitmapFitScaleWidth() {
		return mLeftBitmap.getFitScaleWidth();
	}

	public float getRightBitmapFitScaleWidth() {
		return mLeftBitmap.getFitScaleWidth();
	}

	public void setScale(float scale) {
		mScale = scale;
	}

	public float getScale() {
		return mScale;
	}

	public void setPosition(float x, float y) {
		mPosition.x = x;
		mPosition.y = y;
	}

	public PointF getPosition() {
		return mPosition;
	}

	public void drawPage(Canvas canvas, float drawX, float drawY, boolean isClearBitmap, boolean isAnimating) {
		if (hasBitmap() == false) {
			return;
		}

		mPaint.setAntiAlias(isClearBitmap);
		mPaint.setFilterBitmap(isClearBitmap);

		if (mCenterBitmap.hasBitmap()) {
			LogUtil.d("CenterMatrix x=%f, y=%f, mPosition.x=%f,  mPosition.y=%f",
					(drawX + mPosition.x), (drawY + mPosition.y), mPosition.x, mPosition.y);
			postScaleTranslate(mMatrix, mScale * mCenterBitmap.getFitScale(), drawX + mPosition.x, drawY + mPosition.y);
			drawUsingSampledBitmapIfNeeded(canvas, getCenterBitmap(), mMatrix, isAnimating);

			// 設定されてれば影を描画
			drawSinglePageShadow(canvas, drawX, drawY);
		} else {
			float leftMargin = 0;
			int topMargin = 0;
			if (mLeftBitmap.hasBitmap()) {
				// アスペクトフィット時に左画像の方が縦幅や横幅が小さい場合、描画開始位置を調節する
				if (mViewMode.getFitMode() == FitMode.ASPECT_FIT) {
					if (mLeftBitmap.getFitScaleHeight() < getBitmapHeight()) {
						topMargin = (int) (((getBitmapHeight() - mLeftBitmap.getFitScaleHeight()) / 2) * mScale);
					}
				}
				if (mViewMode.getFitMode() == FitMode.ASPECT_FIT) {
					if (mLeftBitmap.getFitScaleWidth() < getBitmapWidth() / 2) {
						leftMargin = (getBitmapWidth() / 2 - mLeftBitmap.getFitScaleWidth()) * mScale;
					}
				}
				LogUtil.d("LeftMatrix drawX=%f, pos.x=%f, leftMargin=%f, drawY=%f, pos.y=%f, topMargin=%d",
						drawX, mPosition.x, leftMargin, drawY, mPosition.y, topMargin);
				postScaleTranslate(mMatrix, mScale * mLeftBitmap.getFitScale(), drawX + mPosition.x + leftMargin, drawY + mPosition.y + topMargin);
				drawUsingSampledBitmapIfNeeded(canvas, getLeftBitmap(), mMatrix, isAnimating);
				leftMargin += mLeftBitmap.getFitScaleWidth() * mScale;
			} else if (mRightBitmap.hasBitmap() && mViewMode.getFitMode() == FitMode.ASPECT_FIT) {
				// 見開きアスペクトフィット時に右ページしかない場合、描画開始位置を調節する
				leftMargin = mRightBitmap.getFitScaleWidth() * mScale;
			}
			topMargin = 0;
			if (mRightBitmap.hasBitmap()) {
				// アスペクトフィット時に右画像の方が縦幅が小さい場合、描画開始位置を調節する
				if (mViewMode.getFitMode() == FitMode.ASPECT_FIT) {
					if (mRightBitmap.getFitScaleHeight() < getBitmapHeight()) {
						topMargin = (int) (((getBitmapHeight() - mRightBitmap.getFitScaleHeight()) / 2) * mScale);
					}
				}
				LogUtil.d("RightMatrix drawX=%f, pos.x=%f, leftMargin=%f, drawY=%f, pos.y=%f, topMargin=%d",
						drawX, mPosition.x, leftMargin, drawY, mPosition.y, topMargin);
				// 横位置は誤差で空白ができないように切り捨て
				postScaleTranslate(mMatrix, mScale * mRightBitmap.getFitScale(), (int) (drawX + mPosition.x + leftMargin), drawY + mPosition.y + topMargin);
				drawUsingSampledBitmapIfNeeded(canvas, getRightBitmap(), mMatrix, isAnimating);
			}
			// 設定されてれば影を描画
			drawSpreadPageShadow(canvas, drawX, drawY);
		}
		synchronized (this) {
			for (Bitmap bmp : mTemporaryBitmaps) {
				bmp.recycle();
			}
			mTemporaryBitmaps.clear();
		}
	}

	/**
	 * Canvasに描画するが、画像サイズが描画サイズの2倍より大きい場合、適宜間引いて表示する。モアレやジャギを低減できる。 
	 * メモリに余裕がない機種ではやらない方が良いかも
	 */
	private void drawUsingSampledBitmapIfNeeded(Canvas canvas, Bitmap bitmap, Matrix matrix, boolean animating) {
		if (animating) {
			// アニメーション中は重いので縮小処理などしない
			draw(canvas, bitmap, matrix);
			return;
		}

		final float SAMPLING = 0.5f; // 2倍より大きい場合、1/2に縮小する
		// 既存のmatrixから設定取得
		float[] values = new float[9];
		matrix.getValues(values);
		final float x = values[Matrix.MTRANS_X];
		final float y = values[Matrix.MTRANS_Y];
		final float scaleW = values[Matrix.MSCALE_X];
		final float scaleH = values[Matrix.MSCALE_Y];

		if (scaleW > SAMPLING || scaleH > SAMPLING) {
			// 2倍以下の場合はそのまま描画
			draw(canvas, bitmap, matrix);
			return;
		}

		LogUtil.d("1/2 scaled image. scale=%f", scaleW);
		Bitmap scaledBmp = Bitmap.createScaledBitmap(bitmap,
				(int) (bitmap.getWidth() * SAMPLING), (int) (bitmap.getHeight() * SAMPLING), true);
		postScaleTranslate(matrix, scaleW / SAMPLING, scaleH / SAMPLING, x, y);
		draw(canvas, scaledBmp, matrix);
		mTemporaryBitmaps.add(scaledBmp);
	}

	private void draw(Canvas canvas, Bitmap bitmap, Matrix matrix) {
		canvas.drawBitmap(bitmap, matrix, mPaint);
	}

	private void drawSinglePageShadow(Canvas canvas, float drawX, float drawY) {
		if (mShadow != null) {
			float x0 = drawX + mPosition.x; // 画像の左端座標
			float y0 = drawY + mPosition.y; // 画像の上端座標
			int x1 = (int) ((mCenterBitmap.getFitScaleWidth() * mScale) + x0); // 画像の右端座標
			int y1 = (int) ((mCenterBitmap.getFitScaleHeight() * mScale) + y0); // 画像の下端座標
			// 左と上は誤差で空白が出来ないように値を切り上げ
			mShadow.shadowDraw(canvas, (int) Math.ceil(x0), (int) Math.ceil(y0), x1, y1);
		}
	}

	private void drawSpreadPageShadow(Canvas canvas, float drawX, float drawY) {
		if (mShadow != null) {
			float leftMargin = 0;
			if (mLeftBitmap.hasBitmap() == false && mRightBitmap.hasBitmap()
					&& mViewMode.getFitMode() == FitMode.ASPECT_FIT) {
				// 見開きアスペクトフィット時に右ページしかない場合、描画開始位置を調節する
				leftMargin = mRightBitmap.getFitScaleWidth() * mScale;
			}
			float x0 = drawX + mPosition.x + leftMargin;	// 画像の左端座標
			float y0 = drawY + mPosition.y;					// 画像の上端座標
			float x1 = 0;									// 画像の右端座標
			float y1 = (getBitmapHeight() * mScale) + y0;	// 画像の下端座標

			// ページの持ち方によって横幅の取得方法変更
			if (mLeftBitmap.hasBitmap() && mRightBitmap.hasBitmap()) {
				x1 = getBitmapWidth();
			} else if (mLeftBitmap.hasBitmap()) {
				x1 = mLeftBitmap.getFitScaleWidth();
			} else if (mRightBitmap.hasBitmap()) {
				x1 = mRightBitmap.getFitScaleWidth();
			}
			x1 = (x1 * mScale) + x0;
			LogUtil.v("scale=%f, LeftFitScale=%f, RightFitScale=%f, ws=%f, hs=%f, we=%f, he=%f",
					mScale, mLeftBitmap.getFitScale(), mRightBitmap.getFitScale(), x0, y0, x1, y1);
			// 左と上は誤差で空白が出来ないように値を切り上げ
			mShadow.shadowDraw(canvas, (int) Math.ceil(x0), (int) Math.ceil(y0), (int) x1, (int) y1);
		}
	}

	private void postScaleTranslate(Matrix matrix, float scale, float posX, float posY) {
		postScaleTranslate(matrix, scale, scale, posX, posY);
	}

	private void postScaleTranslate(Matrix matrix, float scaleW, float scaleH, float posX, float posY) {
		matrix.reset();
		matrix.postScale(scaleW, scaleH);
		matrix.postTranslate(posX, posY);
	}

	/**
	 * 水平・垂直の設定を元に、このページのデフォルトポジションを設定する
	 */
	public void setDefaultPosition() {
		setPosition(0, 0);
		if (hasBitmap() == false) {
			return;
		}
		float bitmapWidth = getBitmapWidth();
		float bitmapHeight = getBitmapHeight();
		if (mHorizontalAlign != HorizontalAlign.LEFT || bitmapWidth < mDisplaySize.width) {
			mPosition.x = mDisplaySize.width - bitmapWidth;
			LogUtil.d("DisplayWidth=%d, bitmapWidth=%f", mDisplaySize.width, bitmapWidth);

			if (mHorizontalAlign == HorizontalAlign.CENTER || bitmapWidth < mDisplaySize.width) {
				mPosition.x /= 2;
			}
		}
		if (mVerticalAlign != VerticalAlign.TOP || bitmapHeight < mDisplaySize.height) {
			mPosition.y = mDisplaySize.height - bitmapHeight;

			if (mVerticalAlign == VerticalAlign.MIDDLE || bitmapHeight < mDisplaySize.height) {
				mPosition.y /= 2;
			}
			LogUtil.d("ret.y=%f, DisplayHeight=%d, bitmapHeight=%f", mPosition.y, mDisplaySize.height, bitmapHeight);
		}
	}

	/**
	 * 現在保持しているBitmapの、フィットモードに対応するデフォルト拡大率を取得する
	 * 
	 * @return
	 */
	public float getMinimumScale() {
		if (hasBitmap() == false) {
			return BitmapHolder.DISPLAY_FIT_DEFAULT_SCALE;
		}

		float ret = 0.0f;
		switch (mViewMode.getFitMode()) {
		case ASPECT_FIT:
			ret = Math.min(getBitmapWidthFitScale(), getBitmapHeightFitScale());
			break;
		case WIDTH_FIT:
			ret = getBitmapWidthFitScale();
			break;
		case HEIGHT_FIT:
			ret = getBitmapHeightFitScale();
			break;
		}

		ret *= BitmapHolder.DISPLAY_FIT_DEFAULT_SCALE;
		LogUtil.d("ret=%f", ret);
		return ret;
	}

	/**
	 * スクリーンサイズと画像原寸サイズを元に、幅フィットさせた際の縮小率を計算
	 */
	private float getBitmapWidthFitScale() {
		float width;
		if (getCenterBitmap() != null) {
			width = getCenterBitmap().getWidth();
		} else if (getLeftBitmap() != null && getRightBitmap() != null) {
			width = Math.max(getLeftBitmap().getWidth(), getRightBitmap().getWidth()) * 2;
		} else if (getLeftBitmap() != null) {
			width = getLeftBitmap().getWidth() * 2;
		} else if (getRightBitmap() != null) {
			width = getRightBitmap().getWidth() * 2;
		} else {
			throw new IllegalStateException();
		}
		return mDisplaySize.width / width;
	}

	/**
	 * スクリーンサイズと画像原寸サイズを元に、縦フィットさせた際の縮小率を計算
	 */
	private float getBitmapHeightFitScale() {
		float height;
		if (getCenterBitmap() != null) {
			height = getCenterBitmap().getHeight();
		} else if (getLeftBitmap() != null && getRightBitmap() != null) {
			height = Math.max(getLeftBitmap().getHeight(), getRightBitmap().getHeight());
		} else if (getLeftBitmap() != null) {
			height = getLeftBitmap().getHeight();
		} else if (getRightBitmap() != null) {
			height = getRightBitmap().getHeight();
		} else {
			throw new IllegalStateException();
		}
		return mDisplaySize.height / height;
	}

	/**
	 * 設定に合った画像の横幅を返す
	 * 
	 * @return ・単ページの場合：その画像のフィットスケール横幅<br>
	 *         ・見開きの場合：左右のページを合わせたフィットスケール横幅<br>
	 *         ・見開きかつアスペクトフィットモードの場合：左右のページで大きい方のフィットスケール横幅×2<br>
	 *         ・見開きかつ片方のページがnullの場合：存在するページのフィットスケール横幅×2
	 */
	public float getBitmapWidth() {
		float bitmapWidth = 0;

		if (mCenterBitmap.hasBitmap()) {
			bitmapWidth = mCenterBitmap.getFitScaleWidth();
		}
		if (mLeftBitmap.hasBitmap()) {
			bitmapWidth = mLeftBitmap.getFitScaleWidth();

			// 見開きアスペクトフィット時に左右どちらかしかページが無い場合、サイズを2倍して返す
			if (mRightBitmap.hasBitmap() == false && mViewMode.getFitMode() == FitMode.ASPECT_FIT) {
				bitmapWidth *= 2;
			}
		}
		if (mRightBitmap.hasBitmap()) {
			bitmapWidth += mRightBitmap.getFitScaleWidth();

			// 見開きアスペクトフィット時に左右どちらかしかページが無い場合、サイズを2倍して返す
			if (mLeftBitmap.hasBitmap() == false && mViewMode.getFitMode() == FitMode.ASPECT_FIT) {
				bitmapWidth *= 2;
			}
		}
		if (mLeftBitmap.hasBitmap() && mRightBitmap.hasBitmap()
				&& mViewMode.getFitMode() == FitMode.ASPECT_FIT) {
			// 2枚で中央寄せではなく、中央点で閉じ部分を実現するため、擬似的に調整している
			bitmapWidth = Math.max(mLeftBitmap.getFitScaleWidth(), mRightBitmap.getFitScaleWidth()) * 2;
		}

		return bitmapWidth;
	}

	/**
	 * 設定に合った画像の縦幅を返す
	 * 
	 * @return ・単ページの場合：センター画像のフィットスケール縦幅<br>
	 *         ・見開きの場合：左右のページで大きい方のフィットスケール縦幅
	 */
	public float getBitmapHeight() {
		float bitmapHeight = 0;
		if (mCenterBitmap.hasBitmap()) {
			bitmapHeight = mCenterBitmap.getFitScaleHeight();
		}
		if (mLeftBitmap.hasBitmap()) {
			if (mRightBitmap.hasBitmap()) {
				bitmapHeight = Math.max(mLeftBitmap.getFitScaleHeight(), mRightBitmap.getFitScaleHeight());
			} else {
				bitmapHeight = mLeftBitmap.getFitScaleHeight();
			}
		}
		if (mRightBitmap.hasBitmap()) {
			if (bitmapHeight == 0)
				bitmapHeight = mRightBitmap.getFitScaleHeight();
		}
		return bitmapHeight;
	}

	/**
	 * ビットマップ原寸画像のサイズ取得して保存する。必ず1回呼ぶ必要がある
	 * 
	 * @param center センター位置画像用インプットストリーム
	 * @throws LoadImageException 画像読み込みに失敗
	 */
	public synchronized void detectBitmapSize(InputStream center) throws LoadImageException {
		requireSingleMode();
		if (center == null) {
			throw new IllegalArgumentException("bitmap stream is null");
		}
		if (mCenterBitmap.getOriginalSize() != null) {
			return;
		}
		mCenterBitmap.setOriginalSize(center);

		// ディスプレイフィットと原寸に差があるかどうかチェック
		Size fitsize = getFitSize(mCenterBitmap.getOriginalSize(), mMaxDisplaySize);

		mNeedReplaceOriginal = mCenterBitmap.getOriginalSize().isGreaterThanOrEqualTo(fitsize);
		LogUtil.v("needReplaceOriginal=%b", mNeedReplaceOriginal);
	}

	/**
	 * ビットマップ原寸画像のサイズ取得して保存する。必ず1回呼ぶ必要がある
	 * 
	 * @param left レフト位置画像用インプットストリーム
	 * @param right ライト位置画像用インプットストリーム
	 * @throws LoadImageException 画像読み込みに失敗
	 */
	public synchronized void detectBitmapSize(InputStream left, InputStream right) throws LoadImageException {
		requireSpreadMode();
		if (left == null && right == null) {
			throw new IllegalArgumentException("bitmap stream is null");
		}
		if (mLeftBitmap.getOriginalSize() != null || mRightBitmap.getOriginalSize() != null) {
			return;
		}
		if (left != null) {
			mLeftBitmap.setOriginalSize(left);
		}
		if (right != null) {
			mRightBitmap.setOriginalSize(right);
		}

		// ディスプレイフィットと原寸に差があるかどうかチェック
		// 左右どちらかでも原寸とフィットに差があれば、差し替え必要にする
		mNeedReplaceOriginal = false;
		detectBitmapSizeCheck(mLeftBitmap.getOriginalSize());
		detectBitmapSizeCheck(mRightBitmap.getOriginalSize());
		LogUtil.v("needReplaceOriginal=%b", mNeedReplaceOriginal);
	}

	/**
	 * ディスプレイフィットと原寸に差があるか判定し、拡大時に画像を差し替える必要があればmNeedReplaceOriginalをtrueにする
	 * 
	 * @param targetSize ディスプレイサイズと比較する画像サイズ
	 */
	private void detectBitmapSizeCheck(Size targetSize) {
		if (targetSize != null) {
			Size fitsize = getFitSize(targetSize, mMaxDisplaySize);
			mNeedReplaceOriginal |= targetSize.isGreaterThanOrEqualTo(fitsize);
		}
	}

	/**
	 * 指定画質の画像に差し替えることができるか判定する
	 * 
	 * @return 差し替え可能ならtrue、すでに同一解像度の場合などはfalseを返す
	 */
	public boolean canReplaceToQuality(LoadQuality quality) {
		if (hasBitmap() == false) {
			return true;
		}

		switch (quality) {
		case HIGH:
			// ズーム時に高解像度画像に差し替える必要があるかどうか
			// 差し替え必要でも、すでに高解像度ならfalseを返す
			return mNeedReplaceOriginal && isOriginalSize() == false;
		case NEUTRAL:
			// 高解像度になっていて、不要になったので標準解像度に戻すべきかどうか
			// 差し替え必要でも、すでに標準解像度ならfalseを返す
			return mNeedReplaceOriginal && isOriginalSize();
		default:
			// 現状LOWは未対応
			throw new IllegalArgumentException("unsupported quality " + quality);
		}
	}

	private synchronized boolean isOriginalSize() {
		if (isPageSpread()) {
			if (mLeftBitmap.hasBitmap() == false && mRightBitmap.hasBitmap()) {
				return false;
			}

			// 左右両方が原寸なら原寸とする
			boolean ret = true;
			if (mLeftBitmap.hasBitmap()) {
				ret &= mLeftBitmap.isOriginalSize();
			}
			if (mRightBitmap.hasBitmap()) {
				ret &= mRightBitmap.isOriginalSize();
			}
			return ret;
		} else {
			if (mCenterBitmap.hasBitmap() == false) {
				return false;
			}
			return mCenterBitmap.isOriginalSize();
		}
	}

	/**
	 * 原寸デコードし、mCenterBitmapを差し替える
	 * 
	 * @throws LoadImageException 画像読み込みに失敗
	 */
	public void replaceToOriginalSize(InputStream center, OnBeforeReplaceHandler beforeReplace) throws LoadImageException {
		requireSingleMode();
		Bitmap centerBitmap = decodeBitmap(center, mCenterBitmap.getOriginalSize(), mMaxSize);
		synchronized (this) {
			if (beforeReplace != null) {
				if (beforeReplace.onBeforeReplace() == false) {
					// キャンセル
					centerBitmap.recycle();
					return;
				}
			}
			mCenterBitmap.cleanupBitmap();
			mCenterBitmap.setBitmap(centerBitmap);
		}
	}

	/**
	 * 原寸デコードし、mLeftBitmap, mRightBitmapを差し替える
	 * 
	 * @throws LoadImageException 画像読み込みに失敗
	 */
	public void replaceToOriginalSize(InputStream left, InputStream right, OnBeforeReplaceHandler beforeReplace) throws LoadImageException {
		requireSpreadMode();
		Bitmap leftBitmap = left == null ? null : decodeBitmap(left, mLeftBitmap.getOriginalSize(), mMaxSize);
		Bitmap rightBitmap = right == null ? null : decodeBitmap(right, mRightBitmap.getOriginalSize(), mMaxSize);
		synchronized (this) {
			if (beforeReplace != null) {
				if (beforeReplace.onBeforeReplace() == false) {
					// キャンセル
					if (leftBitmap != null) {
						leftBitmap.recycle();
					}
					if (rightBitmap != null) {
						rightBitmap.recycle();
					}
					return;
				}
			}
			mLeftBitmap.cleanupBitmap();
			mRightBitmap.cleanupBitmap();
			mLeftBitmap.setBitmap(leftBitmap);
			mRightBitmap.setBitmap(rightBitmap);
		}
	}

	/**
	 * 画面フィットデコードし、mCenterBitmapを差し替える
	 * 
	 * @throws LoadImageException 画像読み込みに失敗
	 */
	public void replaceToDisplayFitSize(InputStream center, OnBeforeReplaceHandler beforeReplace) throws LoadImageException {
		requireSingleMode();
		Bitmap centerBitmap = decodeBitmap(center, mCenterBitmap.getOriginalSize(), mMaxDisplaySize);
		synchronized (this) {
			if (beforeReplace != null) {
				if (beforeReplace.onBeforeReplace() == false) {
					// キャンセル
					centerBitmap.recycle();
					return;
				}
			}
			mCenterBitmap.cleanupBitmap();
			mCenterBitmap.setBitmap(centerBitmap);
		}
	}

	/**
	 * 画面フィットデコードし、mLeftBitmap, mRightBitmapを差し替える
	 * 
	 * @throws LoadImageException 画像読み込みに失敗
	 */
	public void replaceToDisplayFitSize(InputStream left, InputStream right, OnBeforeReplaceHandler beforeReplace) throws LoadImageException {
		requireSpreadMode();
		Bitmap leftBitmap = null;
		Bitmap rightBitmap = null;
		leftBitmap = displayFitDecodeBitmap(left, mLeftBitmap.getOriginalSize());
		rightBitmap = displayFitDecodeBitmap(right, mRightBitmap.getOriginalSize());

		synchronized (this) {
			if (beforeReplace != null) {
				if (beforeReplace.onBeforeReplace() == false) {
					// キャンセル
					if (leftBitmap != null) {
						leftBitmap.recycle();
					}
					if (rightBitmap != null) {
						rightBitmap.recycle();
					}
					return;
				}
			}
			mLeftBitmap.cleanupBitmap();
			mRightBitmap.cleanupBitmap();
			mLeftBitmap.setBitmap(leftBitmap);
			mRightBitmap.setBitmap(rightBitmap);
		}
	}

	/**
	 * 見開き用、ディスプレイサイズに収まるようにフィットデコードする、見開き用なので画面の横幅は半分を指定
	 * 
	 * @param bmpIs デコード対象の画像のインプットストリーム
	 * @param bmpSize デコード対象の画像のサイズ
	 * @return デコードしたBitmapオブジェクトを返す、bmpIsまたはbmpSizeがnullの場合とデコードに失敗した場合はnullを返す
	 * @throws LoadImageException 画像読み込みに失敗
	 */
	private Bitmap displayFitDecodeBitmap(InputStream bmpIs, Size bmpSize) throws LoadImageException {
		Bitmap retBmp = null;
		if (bmpIs != null && bmpSize != null) {
			// 縦フィットの場合は横幅使わないので常にhalfWidth
			Size half = mMaxDisplaySize.halfWidth();
			retBmp = decodeBitmap(bmpIs, bmpSize, half);
		}
		return retBmp;
	}

	/**
	 * 指定サイズに収まるようにフィットデコードする。 targetSizeがnullの場合は原寸デコードされる。<br>
	 * ContentModeがWIDTH_FITの場合はtargetSize.width、HEIGHT_FIT・HEIGHT_SIDE_FITの場合はtargetSize.heightのみ参照される
	 */
	private Bitmap decodeBitmap(InputStream stream, Size bmpSize, Size targetSize) throws LoadImageException {
		if (targetSize == null) {
			LogUtil.v("Original decode bitmap. bmpSize=%s", bmpSize);
			return decodeStream(stream, 1);
		}
		Size outsize = getFitSize(bmpSize, targetSize);

		Bitmap bmp = decodeRoughlyScaledBitmap(stream, bmpSize, targetSize);
		if (bmp.getWidth() <= outsize.width && bmp.getHeight() <= outsize.height) {
			return bmp;
		} else {
			Bitmap res = Bitmap.createScaledBitmap(bmp, outsize.width, outsize.height, BITMAP_FILTER_SCALE);
			bmp.recycle();
			return res;
		}
	}

	private Size getFitSize(Size srcSize, Size targetSize) {
		Size retSize;
		switch (mViewMode.getFitMode()) {
		case ASPECT_FIT:
			// アスペクトフィット
			retSize = calculateFitSize(srcSize.width, srcSize.height, targetSize.width, targetSize.height);
			LogUtil.v("Aspect fit decode bitmap. bmpSize=%s, outSize=%s", srcSize, retSize);
			break;
		case WIDTH_FIT:
			// 横フィット時はターゲットの縦幅にソースの縦幅を渡して計算
			retSize = calculateFitSize(srcSize.width, srcSize.height, targetSize.width, srcSize.height);
			LogUtil.v("Width fit decode bitmap. bmpSize=%s, outSize=%s", srcSize, retSize);
			break;
		case HEIGHT_FIT:
			// 縦フィット時はターゲットの横幅にソースの横幅を渡して計算
			retSize = calculateFitSize(srcSize.width, srcSize.height, srcSize.width, targetSize.height);
			LogUtil.v("Height fit decode bitmap. bmpSize=%s, outSize=%s", srcSize, retSize);
			break;
		default:
			throw new IllegalArgumentException("ContentMode \"" + mViewMode.getFitMode().toString() + "\" is an incorrect value");
		}
		return retSize;
	}

	/**
	 * 縦横比を維持したまま、target領域にぴったり収まるサイズを求める。targetよりもsrcの方が小さい場合、そのままsrcを返す
	 */
	private Size calculateFitSize(int srcWidth, int srcHeight, int targetWidth, int targetHeight) {
		if (srcWidth <= targetWidth && srcHeight <= targetHeight) {
			return new Size(srcWidth, srcHeight);
		}

		double scaleX = (double) srcWidth / targetWidth;
		double scaleY = (double) srcHeight / targetHeight;

		int scaleWidth;
		int scaleHeight;
		if (scaleX > scaleY) {
			scaleWidth = targetWidth;
			scaleHeight = (int) (srcHeight / scaleX);
		} else {
			scaleWidth = (int) (srcWidth / scaleY);
			scaleHeight = targetHeight;
		}
		return new Size(scaleWidth, scaleHeight);
	}

	/**
	 * ビットマップをデコードする。ビットマップが指定サイズの2倍以上大きいときは、適宜間引いてデコードする。
	 */
	private Bitmap decodeRoughlyScaledBitmap(InputStream stream, Size bmpSize, Size targetSize) throws LoadImageException {
		int inSampleSize = 1;
		switch (mViewMode.getFitMode()) {
		case ASPECT_FIT:
			inSampleSize = Math.max(bmpSize.width / targetSize.width, bmpSize.height / targetSize.height);
			break;
		case WIDTH_FIT:
			inSampleSize = bmpSize.width / targetSize.width;
			break;
		case HEIGHT_FIT:
			inSampleSize = bmpSize.height / targetSize.height;
			break;
		default:
			throw new IllegalArgumentException("ContentMode \"" + mViewMode.getFitMode().toString() + "\" is an incorrect value");
		}

		if (inSampleSize < 1) {
			inSampleSize = 1;
		}
		LogUtil.v("bmpSize=%s, targetSize=%s, inSampleSize=%d", bmpSize, targetSize, inSampleSize);

		return decodeStream(stream, inSampleSize);
	}

	private Bitmap decodeStream(InputStream stream, int inSampleSize) throws LoadImageException {
		mOptions.inSampleSize = inSampleSize;
		Bitmap bmp = null;
		try {
			bmp = BitmapFactory.decodeStream(stream, null, mOptions);
			if (bmp == null) {
				throw new LoadImageException("failed to decode bitmap.");
			}
		} catch (OutOfMemoryError e) {
			throw new LoadImageException("failed to decode bitmap in out of memory", e);
		}
		return bmp;
	}

	public void pageScrollTo(float toDistanceX, float toDistanceY) {
		if (hasBitmap() == false) {
			return;
		}
		setPosition(getScaleXposition(mPosition.x - toDistanceX), getScaleYposition(mPosition.y - toDistanceY));
	}

	/**
	 * 座標Xがページの移動可能な座標の範囲外だった場合、範囲内の値になるように補正する
	 * 
	 * @param toX 座標X
	 * @return
	 */
	public float getScaleXposition(float toX) {
		toX = Math.max(toX, getCanScaleMinX());
		toX = Math.min(toX, getCanScaleMaxX());
		return toX;
	}

	/**
	 * 座標Yがページの移動可能な座標の範囲外だった場合、範囲内の値になるように補正する
	 * 
	 * @param toY 座標Y
	 * @return
	 */
	public float getScaleYposition(float toY) {
		toY = Math.max(toY, getCanScaleMinY());
		toY = Math.min(toY, getCanScaleMaxY());
		return toY;
	}

	public boolean canScrollToX(float toX, float distance) {
		if (distance < 0) {
			// 左から右にスクロール
			if (toX >= getCanScaleMaxX()) {
				return false;
			}
		} else {
			if (toX <= getCanScaleMinX()) {
				return false;
			}
		}
		return true;
	}

	public boolean canScrollToY(float toY, float distance) {
		if (distance < 0) {
			// 上から下にスクロール
			if (toY >= getCanScaleMaxY()) {
				return false;
			}
		} else {
			if (toY <= getCanScaleMinY()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * ページを表示可能な座標Xの最低値を返す<br>
	 * 
	 * @return ・画面横幅より画像横幅の方が大きい場合：画面横幅－画像横幅(マイナス値)<br>
	 *         ・画面横幅より画像横幅の方が小さい場合：(画面横幅－画像横幅)÷2(プラス値)
	 */
	public float getCanScaleMinX() {
		if (isPageWidthLargerBitmapWidth()) {
			return mDisplaySize.width - getBitmapWidth() * mScale;
		} else {
			return (mDisplaySize.width - getBitmapWidth() * mScale) / 2;
		}
	}

	/**
	 * ページを表示可能な座標Yの最低値を返す<br>
	 * 
	 * @return ・画面縦幅より画像縦幅の方が大きい場合：画面縦幅－画像縦幅(マイナス値)<br>
	 *         ・画面縦幅より画像縦幅の方が小さい場合：(画面縦幅－画像縦幅)÷2(プラス値)
	 */
	public float getCanScaleMinY() {
		if (isPageHeightLargerBitmapHeight()) {
			return mDisplaySize.height - getBitmapHeight() * mScale;
		} else {// 画面サイズより小さい場合は半分
			return (mDisplaySize.height - getBitmapHeight() * mScale) / 2;
		}
	}

	/**
	 * ページを表示可能な座標Xの最大値を返す<br>
	 * 
	 * @return ・画面横幅より画像横幅の方が大きい場合：0<br>
	 *         ・画面横幅より画像横幅の方が小さい場合：(画面横幅－画像横幅)÷2(プラス値)
	 */
	public float getCanScaleMaxX() {
		if (isPageWidthLargerBitmapWidth()) {
			return 0;
		} else {
			return (mDisplaySize.width - getBitmapWidth() * mScale) / 2;
		}
	}

	/**
	 * ページを表示可能な座標Yの最大値を返す<br>
	 * 
	 * @return ・画面縦幅より画像縦幅の方が大きい場合：0<br>
	 *         ・画面縦幅より画像縦幅の方が小さい場合：(画面縦幅－画像縦幅)÷2(プラス値)
	 */
	public float getCanScaleMaxY() {
		if (isPageHeightLargerBitmapHeight()) {
			return 0;
		} else {
			return (mDisplaySize.height - getBitmapHeight() * mScale) / 2;
		}
	}

	/**
	 * ページ横幅より画像横幅の方が大きいか判定
	 * 
	 * @return ページ横幅より画像横幅の方が大きい場合true、それ以外の場合falseを返す
	 */
	private boolean isPageWidthLargerBitmapWidth() {
		if (getBitmapWidth() * mScale > mDisplaySize.width) {
			return true;
		}
		return false;
	}

	/**
	 * ページ縦幅より画像縦幅の方が大きいか判定
	 * 
	 * @return ページ縦幅より画像縦幅の方が大きい場合true、それ以外の場合falseを返す
	 */
	private boolean isPageHeightLargerBitmapHeight() {
		if (getBitmapHeight() * mScale > mDisplaySize.height) {
			return true;
		}
		return false;
	}

	/**
	 * ページの縦幅・横幅の方が画像の縦幅・横幅より大きい場合、ページのalign指定をCENTERまたはMIDDLEに設定する
	 */
	public void setDefaultAlign() {
		if (isPageWidthLargerBitmapWidth() == false) {
			mHorizontalAlign = DEFAULT_HORIZONTAL_ARIGN;
		}
		if (isPageHeightLargerBitmapHeight() == false) {
			mVerticalAlign = DEFAULT_VERTICAL_ALIGN;
		}
	}

	public boolean isZoomed() {
		return mScale > PageOperation.PAGE_DEFAULT_SCALE;
	}

	/**
	 * 見開き設定が単ページか判定、見開きに設定されている場合はIllegalArgumentExceptionを投げる
	 */
	private void requireSingleMode() {
		if (isPageSpread()) {
			throw new IllegalArgumentException("PageBitmap is spread mode, but single method called");
		}
	}

	/**
	 * 見開き設定が見開きページか判定、単ページに設定されている場合はIllegalArgumentExceptionを投げる
	 */
	private void requireSpreadMode() {
		if (isPageSpread() == false) {
			throw new IllegalArgumentException("PageBitmap is single mode, but spread method called");
		}
	}
}
