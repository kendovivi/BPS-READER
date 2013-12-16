
package jp.bpsinc.android.viewer.epub.view.util;

import jp.bpsinc.android.util.LogUtil;
import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class FinishDetectableScroller extends Scroller {
	/** スクロールが開始されたらtrueをセットしておく */
	private boolean mScrollStarted;
	/** ビューのスクロール(ページ遷移)ではなくページのスクロール(ページ内スクロール)をしているフラグ。 */
	private boolean mScrollingTargetIsPage;
	/** タップによるスクロールの速度 */
	public static final int DURATION_TAP = 250;
	/** ドラッグによるスクロールの速度 */
	public static final int DURATION_DRAG = 350;

	public FinishDetectableScroller(Context context) {
		super(context);
	}

	public FinishDetectableScroller(Context context, Interpolator interpolator) {
		super(context, interpolator);
	}

	/**
	 * スクロール終了直後かどうかの判定を行う。
	 * abortAnimation()などが呼ばれても、mScrollStartedはこのメソッドを呼ぶまで更新されない。
	 */
	public boolean isFinishedNow() {
		if (mScrollStarted && isFinished()) {
			mScrollStarted = false;
			return true;
		}
		return false;
	}

	/**
	 * スクロール終了直後のイベントがすでに検知された後かどうか
	 */
	public boolean isFinishEventProcessed() {
		return mScrollStarted == false;
	}

	public boolean isFinishedAndFinishEventProcessed() {
		return isFinished() && isFinishEventProcessed();
	}

	public void setScrollingTargetIsPage(boolean page) {
		mScrollingTargetIsPage = page;
		mScrollStarted = false;
	}

	public boolean scrollingTargetIsPage() {
		return mScrollingTargetIsPage;
	}

	@Override
	public void startScroll(int startX, int startY, int dx, int dy, int duration) {
		LogUtil.v("sx=%d, sy=%d, dx=%d, dy=%d, duration=%d", startX, startY, dx, dy, duration);
		mScrollStarted = true;
		super.startScroll(startX, startY, dx, dy, duration);
	}

	@Override
	public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
		LogUtil.v();
		mScrollStarted = true;
		super.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
	}

	@Override
	public void extendDuration(int extend) {
		mScrollStarted = true;
		super.extendDuration(extend);
	}

	@Override
	public void setFinalX(int newX) {
		mScrollStarted = true;
		super.setFinalX(newX);
	}

	@Override
	public void setFinalY(int newY) {
		mScrollStarted = true;
		super.setFinalY(newY);
	}
}
