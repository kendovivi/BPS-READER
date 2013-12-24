package jp.bpsinc.android.viewer.epub.fxl.listener;

import android.graphics.PointF;
import android.view.MotionEvent;
import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.activity.EpubViewerActivity;
import jp.bpsinc.android.viewer.epub.content.PageOperation;
import jp.bpsinc.android.viewer.epub.dialog.EpubViewerDialog;
import jp.bpsinc.android.viewer.epub.listener.EpubViewerOnGestureListener;
import jp.bpsinc.android.viewer.epub.listener.EpubViewerOnScaleGestureListener;
import jp.bpsinc.android.viewer.epub.view.EpubScrollView;
import jp.bpsinc.android.viewer.epub.view.util.FinishDetectableScroller;
import jp.bpsinc.android.viewer.epub.view.util.ViewMode.FitMode;
import jp.bpsinc.android.viewer.epub.view.util.ViewMode.Mode;
import jp.bpsinc.android.viewer.util.MyViewCompat;

public class FxlEpubViewerOnGestureListener extends EpubViewerOnGestureListener {
	/** ダブルタップによる拡大縮小処理スレッド */
	private MoveThread mMoveThread;

	/** ダブルタップによる拡大率 */
	private static final float ZOOM_SCALE_BY_DOUBLETAP = 2.5f;

	public FxlEpubViewerOnGestureListener(EpubViewerActivity epubViewerActivity, EpubScrollView epubScrollView) {
		super(epubViewerActivity, epubScrollView);
		mMoveThread = null;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		super.onDown(e);
		if (shouldTightenPaging()) {
			// 横フィット
			mPageScrollable = false;
		}
		return true;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		if (mMoveThread != null) {
			return true;
		}
		return super.onSingleTapConfirmed(e);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		if (mMoveThread != null) {
			return true;
		}
		return super.onScroll(e1, e2, distanceX, distanceY);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (mMoveThread != null) {
			return true;
		}
		return super.onFling(e1, e2, velocityX, velocityY);
	}

	@SuppressWarnings("deprecation")
    @Override
	public boolean onDoubleTap(MotionEvent e) {
		LogUtil.v("MotionEvent");
		if (isTouchEventDisable()) {
			return true;
		}
		try {
			if (mMoveThread == null) {
				PageOperation currentPageOperation = mEpubScrollView.getRetentionPageHelper().getCurrentPageOperation();
				if (currentPageOperation.hasBitmap() == false) {
					LogUtil.d("bitmap is null");
					return true;
				}

				if (currentPageOperation.isZoomed()) {
					LogUtil.v("zoom out");
					zoomWithPoint(currentPageOperation, e.getRawX(), e.getRawY(), PageOperation.PAGE_DEFAULT_SCALE);
				} else {
					LogUtil.v("zoom in");
					float scale = ZOOM_SCALE_BY_DOUBLETAP;
					float minimumScale = currentPageOperation.getMinimumScale();

					// ディスプレイフィット時の拡大率が最大拡大率より大きい場合は拡大処理を出来なくする
					if (minimumScale >= EpubViewerOnScaleGestureListener.MAXIMUM_SCALE) {
						return true;
					}
					// 最大拡大率を超えていたら値を調節
					if (minimumScale * scale > EpubViewerOnScaleGestureListener.MAXIMUM_SCALE) {
						scale = EpubViewerOnScaleGestureListener.MAXIMUM_SCALE / minimumScale;
					}
					zoomWithPoint(currentPageOperation, e.getRawX(), e.getRawY(), scale);
				}
			}
		} catch (RuntimeException ex) {
			LogUtil.e("unexpected error", ex);
			mEpubViewerActivity.showDialog(EpubViewerDialog.ID_UNEXPECTED_ERR);
		}
		return true;
	}

	private void zoomWithPoint(PageOperation pageOperation, float rawX, float rawY, float toScale) {
		float befScale = pageOperation.getScale();
		float toX = (pageOperation.getPosition().x - rawX) * (toScale / befScale) + rawX;
		float toY = (pageOperation.getPosition().y - rawY) * (toScale / befScale) + rawY;

		pageOperation.setScale(toScale);
		toX = pageOperation.getScaleXposition(toX);
		toY = pageOperation.getScaleYposition(toY);
		pageOperation.setScale(befScale);

		mMoveThread = new MoveThread(pageOperation, toX, toY, toScale);
		MyViewCompat.postOnAnimation(mEpubScrollView, mMoveThread);
	}

	/**
	 * ダブルタップによる拡大縮小アニメーション用スレッド
	 */
	private class MoveThread implements Runnable {
		PageOperation mPageOperation;
		PointF mToPoint;
		float mToScale;
		int mSpeed;
		float mRemain;
		float mProgressX;
		float mProgressY;
		float mProgressScale;
		boolean mIsHalt;

		private static final float ANIMATION_FPS = 10;
		private static final float ANIMATION_MILLIS = 300;

		public MoveThread(PageOperation pageOperation, float toX, float toY, float toScale) {
			LogUtil.d("start toX=%f, toY=%f, toScale:%f", toX, toY, toScale);
			mIsHalt = false;
			mPageOperation = pageOperation;
			mToPoint = new PointF(toX, toY);
			mToScale = toScale;
			mSpeed = (int) (ANIMATION_MILLIS / ANIMATION_FPS);
			mRemain = ANIMATION_MILLIS;
			mProgressX = (mToPoint.x - pageOperation.getPosition().x) / ANIMATION_FPS;
			mProgressY = (mToPoint.y - pageOperation.getPosition().y) / ANIMATION_FPS;
			mProgressScale = (toScale - pageOperation.getScale()) / ANIMATION_FPS;
		}

		public void halt() {
			mIsHalt = true;
			mIsPageMoving = false;
		}

		@Override
		public void run() {
			try {
				mEpubScrollView.setZoomAnimating(mIsHalt == false);
				if (mIsHalt) {
					return;
				}

				mIsPageMoving = true;
				mPageOperation.setPosition(mPageOperation.getPosition().x + mProgressX,
						mPageOperation.getPosition().y + mProgressY);

				mPageOperation.setScale(mPageOperation.getScale() + mProgressScale);
				mRemain -= mSpeed;
				// 最終位置を越えたら停止
				if (mRemain <= 0.0f) {
					LogUtil.d("finish toScale=%f", mToScale);
					mPageOperation.setPosition(mToPoint.x, mToPoint.y);
					mPageOperation.setScale(mToScale);
					mEpubScrollView.getRetentionPageHelper().replaceToHighQuality();
					halt();
					mMoveThread = null;
				}
				mEpubScrollView.setZoomAnimating(mIsHalt == false);
				mEpubScrollView.drawPostInvalidate();
				if (mIsHalt == false) {
					// 最終位置を越えるまでアニメーションを続ける
					MyViewCompat.postOnAnimation(mEpubScrollView, this);
				}
			} catch (RuntimeException e) {
				LogUtil.e("unexpected error", e);
				mEpubViewerActivity.postShowDialog(EpubViewerDialog.ID_UNEXPECTED_ERR);
			}
		}
	}

	/**
	 * 横拡大モード時、上下スクロールしやすいように少し判定に余裕を持たせる。この適用有無を判定
	 * 
	 * @return 横フィット、かつ、非ズームの場合はtrue、それ以外ならfalse
	 */
	private boolean shouldTightenPaging() {
		return mEpubScrollView.getViewMode().getFitMode() == FitMode.WIDTH_FIT
				&& mEpubScrollView.getRetentionPageHelper().getCurrentPageOperation().isZoomed() == false;
	}

	/**
	 * 進行方向へページ内スクロールの余地があったら画面サイズ分ページ内スクロール、ページの端なら次のページへ移動
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
				int viewHeight = mEpubScrollView.getHeight();
				destY = canScaleMinY - startY;
				if (destY < -viewHeight) {
					destY = -viewHeight;
				}
			}
		} else {
			isNextPage = true;
		}
		if (isNextPage) {
			mEpubScrollView.nextPageScroll();
		} else {
			// ページ内移動スクロール)
			mEpubScrollView.scrollTo(true, startX, startY, destX, destY, FinishDetectableScroller.DURATION_TAP);
		}
	}

	/**
	 * 進行方向とは逆へページ内スクロールの余地があったら画面サイズ分ページ内スクロール、ページの端なら前のページへ移動
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
				if (destY > mEpubScrollView.getHeight()) {
					destY = mEpubScrollView.getHeight();
				}
			}
		} else {
			isPrevPage = true;
		}
		if (isPrevPage) {
			mEpubScrollView.prevPageScroll();
		} else {
			// ページ内移動スクロール)
			mEpubScrollView.scrollTo(true, startX, startY, destX, destY, FinishDetectableScroller.DURATION_TAP);
		}
	}
}
