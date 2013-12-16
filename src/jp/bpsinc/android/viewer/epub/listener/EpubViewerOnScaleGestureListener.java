package jp.bpsinc.android.viewer.epub.listener;

import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.content.PageOperation;
import jp.bpsinc.android.viewer.epub.view.EpubScrollView;
import android.graphics.PointF;
import android.view.ScaleGestureDetector;

public class EpubViewerOnScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	protected EpubScrollView mEpubScrollView;

	private float mBeginScale;
	private float mBeginFocusX;
	private float mBeginFocusY;
	private PointF mBeginPoint;

	/** 画像の原寸サイズに対する最大拡大率(拡大処理でこのサイズを超えることはないが、フィットデコード時に超える可能性はある) */
	public static final float MAXIMUM_SCALE = 5.0f;

	public EpubViewerOnScaleGestureListener(EpubScrollView epubScrollView) {
		mEpubScrollView = epubScrollView;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		PageOperation currentPageOperation = mEpubScrollView.getRetentionPageHelper().getCurrentPageOperation();
		if (currentPageOperation.hasBitmap() == false) {
			return false;
		}
		mEpubScrollView.setZoomAnimating(true);
		mBeginScale = currentPageOperation.getScale();
		mBeginFocusX = detector.getFocusX();
		mBeginFocusY = detector.getFocusY();
		mBeginPoint = new PointF(currentPageOperation.getPosition().x, currentPageOperation.getPosition().y);
		LogUtil.v("MotionEvent beginScale=%f, beginFocusX=%f, beginFocusY=%f, beginPoint.x=%f, beginPoint.y=%f",
				mBeginScale, mBeginFocusX, mBeginFocusY, mBeginPoint.x, mBeginPoint.y);
		return true;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		LogUtil.v("MotionEvent");
		PageOperation currentPageOperation = mEpubScrollView.getRetentionPageHelper().getCurrentPageOperation();
		float minimumScale = currentPageOperation.getMinimumScale();

		// ディスプレイフィット時の拡大率が最大拡大率より大きい場合は拡大処理を出来なくする
		if (minimumScale >= MAXIMUM_SCALE) {
			return true;
		}
		float scale = currentPageOperation.getScale() * detector.getScaleFactor();

		// 変更後のスケールが範囲外の場合は境界値とする、既に境界値の場合は何もせず処理を終了する
		if (scale < PageOperation.PAGE_DEFAULT_SCALE) {
			if (currentPageOperation.getScale() == PageOperation.PAGE_DEFAULT_SCALE) {
				return true;
			}
			scale = PageOperation.PAGE_DEFAULT_SCALE;
		} else if (minimumScale * scale > MAXIMUM_SCALE) {
			if (minimumScale * currentPageOperation.getScale() == MAXIMUM_SCALE) {
				return true;
			}
			scale = MAXIMUM_SCALE / minimumScale;
		}
		currentPageOperation.setScale(scale);
		float scaleDif = scale / mBeginScale;

		float x = (mBeginPoint.x - mBeginFocusX) * scaleDif + detector.getFocusX();
		float y = (mBeginPoint.y - mBeginFocusY) * scaleDif + detector.getFocusY();
		currentPageOperation.setPosition(currentPageOperation.getScaleXposition(x),
				currentPageOperation.getScaleYposition(y));
		mEpubScrollView.drawInvalidate();
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		LogUtil.v("MotionEvent");
		mEpubScrollView.getRetentionPageHelper().replaceToHighQuality();
		mEpubScrollView.setZoomAnimating(false);
		mEpubScrollView.drawInvalidate();
	}
}
