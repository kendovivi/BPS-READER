package jp.bpsinc.android.viewer.epub.omf.listener;

import android.view.MotionEvent;
import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.activity.EpubViewerActivity;
import jp.bpsinc.android.viewer.epub.content.PageOperation;
import jp.bpsinc.android.viewer.epub.listener.EpubViewerOnGestureListener;
import jp.bpsinc.android.viewer.epub.view.EpubScrollView;
import jp.bpsinc.android.viewer.epub.view.util.FinishDetectableScroller;
import jp.bpsinc.android.viewer.epub.view.util.ViewMode.Mode;

public class OmfEpubViewerOnGestureListener extends EpubViewerOnGestureListener {
	public OmfEpubViewerOnGestureListener(EpubViewerActivity epubViewerActivity, EpubScrollView epubScrollView) {
		super(epubViewerActivity, epubScrollView);
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		LogUtil.v("MotionEvent");
		if (isTouchEventDisable()) {
			return true;
		}
		mEpubScrollView.spreadChange();
		return true;
	}

	/**
	 * 進行方向へページ内スクロールの余地があったら端までページ内スクロール、ページの端なら次のページへ移動
	 */
	@Override
	protected void pageScrollOrNextPage() {
		boolean isNextPage = false;
		PageOperation pageOperation = mEpubScrollView.getRetentionPageHelper().getCurrentPageOperation();
		int startX = (int) pageOperation.getPosition().x;
		int startY = (int) pageOperation.getPosition().y;
		int destX = 0; // X方向への移動量
		int destY = 0; // Y方向への移動量
		int canScaleMinX = (int) pageOperation.getCanScaleMinX();
		int canScaleMinY = (int) pageOperation.getCanScaleMinY();
		LogUtil.v("currentPage=%d, startX=%d, startY=%d, canScaleMinX=%d, canScaleMinY=%d",
				mEpubScrollView.getCurrentPageIndex(), startX, startY, canScaleMinX, canScaleMinY);
		if (mEpubScrollView.getViewMode().getMode() == Mode.LANDSCAPE_STANDARD) {
			if (startY <= canScaleMinY) {
				isNextPage = true;
			} else {
				destY = canScaleMinY - startY;
			}
		} else {
			if (mEpubScrollView.getEpubPageAccess().isRTL()) {
				if (startX >= 0) {
					isNextPage = true;
				} else {
					destX = -startX;
				}
			} else {
				if (startX <= canScaleMinX) {
					isNextPage = true;
				} else {
					destX = canScaleMinX - startX;
				}
			}
		}
		if (isNextPage) {
			mEpubScrollView.nextPageScroll();
		} else {
			mEpubScrollView.scrollTo(true, startX, startY, destX, destY, FinishDetectableScroller.DURATION_TAP);
		}
	}

	/**
	 * 進行方向とは逆へページ内スクロールの余地があったら端までページ内スクロール、ページの端なら前のページへ移動
	 */
	@Override
	protected void pageScrollOrPrevPage() {
		boolean isPrevPage = false;
		PageOperation pageOperation = mEpubScrollView.getRetentionPageHelper().getCurrentPageOperation();
		int startX = (int) pageOperation.getPosition().x;
		int startY = (int) pageOperation.getPosition().y;
		int destX = 0; // X方向への移動量
		int destY = 0; // Y方向への移動量
		int canScaleMinX = (int) pageOperation.getCanScaleMinX();
		int canScaleMinY = (int) pageOperation.getCanScaleMinY();
		LogUtil.v("currentPage=%d, startX=%d, startY=%d, canScaleMinX=%d, canScaleMinY=%d",
				mEpubScrollView.getCurrentPageIndex(), startX, startY, canScaleMinX, canScaleMinY);
		if (mEpubScrollView.getViewMode().getMode() == Mode.LANDSCAPE_STANDARD) {
			if (startY >= 0) {
				isPrevPage = true;
			} else {
				destY = -startY;
			}
		} else {
			if (mEpubScrollView.getEpubPageAccess().isRTL()) {
				if (startX <= canScaleMinX) {
					isPrevPage = true;
				} else {
					destX = canScaleMinX - startX;
				}
			} else {
				if (startX >= 0) {
					isPrevPage = true;
				} else {
					destX = -startX;
				}
			}
		}
		if (isPrevPage) {
			mEpubScrollView.prevPageScroll();
		} else {
			mEpubScrollView.scrollTo(true, startX, startY, destX, destY, FinishDetectableScroller.DURATION_TAP);
		}
	}
}
