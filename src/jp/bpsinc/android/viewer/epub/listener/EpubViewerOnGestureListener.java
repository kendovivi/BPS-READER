package jp.bpsinc.android.viewer.epub.listener;

import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.activity.EpubViewerActivity;
import jp.bpsinc.android.viewer.epub.content.PageOperation;
import jp.bpsinc.android.viewer.epub.view.EpubScrollView;
import android.view.GestureDetector;
import android.view.MotionEvent;

public abstract class EpubViewerOnGestureListener extends GestureDetector.SimpleOnGestureListener {
	protected EpubViewerActivity mEpubViewerActivity;
	protected EpubScrollView mEpubScrollView;
	protected boolean mIsPageMoving;
	protected boolean mPageScrollable;

	/** 横フィットでズームしていない時、ページ遷移するために必要な最低スクロール量(縦スクロールしたい時に誤ってページ遷移してしまわないようにする) */
	private static final int PAGE_JUMP_MIN_SCROLL_AMOUNT = 16;

	public EpubViewerOnGestureListener(EpubViewerActivity epubViewerActivity, EpubScrollView epubScrollView) {
		mEpubViewerActivity = epubViewerActivity;
		mEpubScrollView = epubScrollView;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		LogUtil.v("MotionEvent");
		mIsPageMoving = false;
		mPageScrollable = true;
		return true;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		float tapX = e.getX();
		float tapY = e.getY();
		LogUtil.v("MotionEvent tapX=%f, tapY=%f", tapX, tapY);

		if (isTouchEventDisable()) {
			return true;
		}

		int viewWidth = mEpubScrollView.getWidth();
		if (viewWidth / 3 > tapX) {
			// 画面左をタップ
			onLeftAreaTapped();
		} else if (viewWidth / 3 * 2 < tapX) {
			// 画面右をタップ
			onRightAreaTapped();
		} else {
			// 画面中央をタップ
			onCenterAreaTapped();
		}
		return super.onSingleTapConfirmed(e);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		LogUtil.v("MotionEvent distanceX=%f, distanceY=%f", distanceX, distanceY);

		if (isTouchEventDisable()) {
			return true;
		}

		mIsPageMoving = true;
		boolean isPageJump = false;

		PageOperation currentPage = mEpubScrollView.getRetentionPageHelper().getCurrentPageOperation();
		if (mPageScrollable == false) {
			float xDiff = Math.abs(e1.getX() - e2.getX());
			float yDiff = Math.abs(e1.getY() - e2.getY());

			if (xDiff > PAGE_JUMP_MIN_SCROLL_AMOUNT && xDiff > yDiff) {
				mPageScrollable = true;
			} else if (yDiff > PAGE_JUMP_MIN_SCROLL_AMOUNT && yDiff > xDiff) {
				// 縦スクロール量の方が多い場合はページ遷移しない
				mPageScrollable = false;
			}
		}
		if (mPageScrollable) {
			// ページ移動が縦スクロールか横スクロールか判定
			if (mEpubScrollView.getViewMode().isVerticalScroll()) {
				int viewHeight = mEpubScrollView.getHeight();
				float viewBottomPosition = mEpubScrollView.getPagePositionY(mEpubScrollView.getCurrentPageIndex())
						+ mEpubScrollView.getPosition().y;
				float viewTopPosition = viewBottomPosition + viewHeight;

				LogUtil.d("pageY=%f, distanceY=%f", currentPage.getPosition().y, distanceY);
				if (currentPage.canScrollToY(currentPage.getPosition().y, distanceY) == false) {
					// ページ内移動がY方向にできない
					isPageJump = true;
				} else if (distanceY > 0) {
					// ページ内移動が可能な状態で下から上にスクロールした時
					if (viewTopPosition > viewHeight) {
						isPageJump = true;
						if (viewTopPosition - viewHeight < distanceY) {
							distanceY = viewTopPosition - viewHeight;
						}
					}
				} else {
					// ページ内移動が可能な状態で上から下にスクロールした時
					if (viewBottomPosition < 0) {
						isPageJump = true;
						if (viewBottomPosition - distanceY > 0) {
							distanceY = viewBottomPosition;
						}
					}
				}
			} else {
				int viewWidth = mEpubScrollView.getWidth();
				float viewLeftPosition = mEpubScrollView.getPagePositionX(mEpubScrollView.convertedPage(mEpubScrollView.getCurrentPageIndex()))
						+ mEpubScrollView.getPosition().x;
				float viewRightPosition = viewLeftPosition + viewWidth;

				LogUtil.d("pageX=%f, distanceX=%f", currentPage.getPosition().x, distanceX);
				if (currentPage.canScrollToX(currentPage.getPosition().x, distanceX) == false) {
					// ページ内移動がX方向にできない
					isPageJump = true;
				} else if (distanceX > 0) {
					// ページ内移動が可能な状態で右から左にスクロールした時
					if (viewRightPosition > viewWidth) {// 画面右端
						isPageJump = true;
						if (viewRightPosition - viewWidth < distanceX) {
							distanceX = viewRightPosition - viewWidth;
						}
					}
				} else {
					// ページ内移動が可能な状態で左から右にスクロールした時
					if (viewLeftPosition < 0) {// 画面左端
						isPageJump = true;
						if (viewLeftPosition - distanceX > 0) {
							distanceX = viewLeftPosition;
						}
					}
				}
			}
		}
		// ページ内移動がXまたはY方向にできなければページを移動する
		if (isPageJump) {
			float toX = mEpubScrollView.getPosition().x;
			float toY = mEpubScrollView.getPosition().y;
			if (mEpubScrollView.getViewMode().isVerticalScroll()) {
				toY -= distanceY;
				distanceY = 0;
			} else {
				toX -= distanceX;
				distanceX = 0;
			}
			mEpubScrollView.viewMoveTo(toX, toY);
		}
		currentPage.pageScrollTo(distanceX, distanceY);
		mEpubScrollView.drawInvalidate();

		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		LogUtil.v("MotionEvent velocityX=%f, velocityY=%f", velocityX, velocityY);

		PageOperation currentPage = mEpubScrollView.getRetentionPageHelper().getCurrentPageOperation();

		// スクロール中(ページ移動アニメーションも含む)か判定、カレントページがビットマップ持ってない場合もminXやmaxXなどの値がおかしくなるので判定
		if (isTouchEventDisable() || currentPage.hasBitmap() == false) {
			return true;
		}
		mIsPageMoving = true;

		// ページフィットの箇所までFling
		int vx = (int) (velocityX);
		int vy = (int) (velocityY);

		int minX = (int) currentPage.getCanScaleMinX();
		int maxX = (int) currentPage.getCanScaleMaxX();

		int minY = (int) currentPage.getCanScaleMinY();
		int maxY = (int) currentPage.getCanScaleMaxY();

		int startX = (int) currentPage.getPosition().x;
		int startY = (int) currentPage.getPosition().y;

		LogUtil.v("posX=%d, posY=%d, minX=%d, minY=%d, maxX=%d, maxY=%d, vx=%d, vy=%d",
				startX, startY, minX, minY, maxX, maxY, vx, vy);
		mEpubScrollView.getScroller().setScrollingTargetIsPage(true);
		mEpubScrollView.getScroller().fling(startX, startY, vx, vy, minX, maxX, minY, maxY);
		mEpubScrollView.drawInvalidate();

		return true;
	}

	protected boolean isTouchEventDisable() {
		return mEpubScrollView.getScroller().isFinishedAndFinishEventProcessed() == false;
	}

	public boolean isPageMoving() {
		return mIsPageMoving;
	}

	public void setIsPageMoving(boolean isPageMoving) {
		mIsPageMoving = isPageMoving;
	}

	private void onCenterAreaTapped() {
		mEpubViewerActivity.openOptionsMenu();
	}

	private void onLeftAreaTapped() {
		if (mEpubScrollView.getRetentionPageHelper().getCurrentPageOperation().isZoomed()) {
			mEpubViewerActivity.openOptionsMenu();
		} else {
			if (mEpubScrollView.getEpubPageAccess().isRTL()) {
				pageScrollOrNextPage();
			} else {
				pageScrollOrPrevPage();
			}
		}
	}

	private void onRightAreaTapped() {
		if (mEpubScrollView.getRetentionPageHelper().getCurrentPageOperation().isZoomed()) {
			mEpubViewerActivity.openOptionsMenu();
		} else {
			if (mEpubScrollView.getEpubPageAccess().isRTL()) {
				pageScrollOrPrevPage();
			} else {
				pageScrollOrNextPage();
			}
		}
	}

	/**
	 * 進行方向へページ内スクロール、または、次のページへ移動、スクロールの仕方などは実装により変わる
	 */
	protected abstract void pageScrollOrNextPage();
	/**
	 * 進行方向とは逆へページ内スクロール、または、前のページへ移動、スクロールの仕方などは実装により変わる
	 */
	protected abstract void pageScrollOrPrevPage();
}
