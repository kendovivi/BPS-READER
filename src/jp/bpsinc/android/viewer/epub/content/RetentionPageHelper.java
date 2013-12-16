package jp.bpsinc.android.viewer.epub.content;

import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.content.PageOperation.HorizontalAlign;
import jp.bpsinc.android.viewer.epub.content.PageOperation.LoadQuality;
import jp.bpsinc.android.viewer.epub.content.PageOperation.VerticalAlign;
import jp.bpsinc.android.viewer.epub.util.PageLoadManager;
import jp.bpsinc.android.viewer.epub.view.EpubScrollView;
import jp.bpsinc.android.viewer.epub.view.util.ViewMode;
import jp.bpsinc.android.viewer.epub.view.util.ViewMode.Mode;
import jp.bpsinc.android.viewer.util.Size;
import android.graphics.Canvas;

public class RetentionPageHelper {
	private final EpubScrollView mEpubScrollView;
	/** EPUBエントリにアクセスするためのインスタンス */
	private final EpubPageAccess mEpubPageAccess;
	/** ページごとに画像読み込みスレッドを作成、制御 */
	private final PageLoadManager mPageLoadManager;
	/** EPUBビューアの各種設定の取得などに使用 */
	private final ViewMode mViewMode;
	/** 端末の画面サイズ */
	private final Size mDisplaySize;
	/** カレントページに表示するページ */
	private PageOperation mCurrentPageOperation;
	/** カレントページの右側に表示するページ(縦スクロールの場合は上側に表示するページ) */
	private PageOperation mRightPageOperation;
	/** カレントページの左側に表示するページ(縦スクロールの場合は下側に表示するページ) */
	private PageOperation mLeftPageOperation;

	public RetentionPageHelper(EpubScrollView epubScrollView, EpubPageAccess epubPageAccess, PageLoadManager pageLoadManager,
			int displayWidth, int displayHeight) {
		LogUtil.v();
		mEpubScrollView = epubScrollView;
		mEpubPageAccess = epubPageAccess;
		mPageLoadManager = pageLoadManager;
		mViewMode = mEpubScrollView.getViewMode();
		mDisplaySize = new Size(displayWidth, displayHeight);
		mCurrentPageOperation = new PageOperation(mDisplaySize, mViewMode);
		mRightPageOperation = new PageOperation(mDisplaySize, mViewMode);
		mLeftPageOperation = new PageOperation(mDisplaySize, mViewMode);
	}

	public PageOperation getCurrentPageOperation() {
		return mCurrentPageOperation;
	}

	public void stopAllPageLoadAndReset() {
		mPageLoadManager.stopAllTasks();
		mCurrentPageOperation.reset();
		mRightPageOperation.reset();
		mLeftPageOperation.reset();
	}

	private void setLoadDefaultVerticalAlign(PageOperation pageOperation) {
		switch (mViewMode.getMode()) {
		case PORTRAIT_STANDARD:
		case PORTRAIT_SPREAD:
		case LANDSCAPE_SPREAD:
			pageOperation.setVerticalAlign(VerticalAlign.MIDDLE);
			break;
		case LANDSCAPE_STANDARD:
			pageOperation.setVerticalAlign(VerticalAlign.TOP);
			break;
		}
	}

	private void loadCurrentPage() {
		setLoadDefaultVerticalAlign(mCurrentPageOperation);
		if (mViewMode.isVerticalScroll()) {
			mCurrentPageOperation.setVerticalAlign(VerticalAlign.TOP);
		} else if (mViewMode.getMode() == Mode.PORTRAIT_SPREAD) {
			// 右開き、左開きでデフォルトポジションを変更する＋単ページから見開きに切り替わった場合にデフォルトポジションを単ページ時の方に寄せる
			if ((mEpubPageAccess.isRTL() && mViewMode.isSubsequentPagePositionFlag() == false)
					|| (mEpubPageAccess.isRTL() == false && mViewMode.isSubsequentPagePositionFlag())) {
				mCurrentPageOperation.setHorizontalAlign(HorizontalAlign.RIGHT);
			} else {
				mCurrentPageOperation.setHorizontalAlign(HorizontalAlign.LEFT);
			}
		}
		mViewMode.setIsSubsequentPagePositionFlag(false);
		if (mCurrentPageOperation.hasBitmap() == false) {
			mCurrentPageOperation.setPage(mEpubScrollView.getCurrentPage());
			mPageLoadManager.addPageLoadThread(mCurrentPageOperation, LoadQuality.NEUTRAL);
		}
	}

	private void loadRightPage() {
		if (mEpubScrollView.convertedPage(mEpubScrollView.getCurrentPageIndex()) > 0) {
			setLoadDefaultVerticalAlign(mRightPageOperation);
			if (mViewMode.isVerticalScroll()) {
				mRightPageOperation.setVerticalAlign(VerticalAlign.BOTTOM);
			} else if (mViewMode.getMode() == Mode.PORTRAIT_SPREAD) {
				mRightPageOperation.setHorizontalAlign(HorizontalAlign.LEFT);
			}
			if (mRightPageOperation.hasBitmap()) {
				setDefaultPageValue(mRightPageOperation);
				replaceToNormalQuality(mRightPageOperation);
			} else {
				mRightPageOperation.setPage(mEpubScrollView.getRightPage());
				mPageLoadManager.addPageLoadThread(mRightPageOperation, LoadQuality.NEUTRAL);
			}
		}
	}

	private void loadLeftPage() {
		int convertedAfterPageIndex = mEpubScrollView.isConvertedPage()
				? mEpubScrollView.convertedPage(mEpubScrollView.getCurrentPageIndex() - 1)
				: mEpubScrollView.getCurrentPageIndex() + 1;
		if (convertedAfterPageIndex < mEpubScrollView.getPageCount()) {
			setLoadDefaultVerticalAlign(mLeftPageOperation);
			if (mViewMode.isVerticalScroll()) {
				mLeftPageOperation.setVerticalAlign(VerticalAlign.TOP);
			} else if (mViewMode.getMode() == Mode.PORTRAIT_SPREAD) {
				mLeftPageOperation.setHorizontalAlign(HorizontalAlign.RIGHT);
			}
			if (mLeftPageOperation.hasBitmap()) {
				setDefaultPageValue(mLeftPageOperation);
				replaceToNormalQuality(mLeftPageOperation);
			} else {
				mLeftPageOperation.setPage(mEpubScrollView.getLeftPage());
				mPageLoadManager.addPageLoadThread(mLeftPageOperation, LoadQuality.NEUTRAL);
			}
		}
	}

	private void setDefaultPageValue(PageOperation pageOperation) {
		pageOperation.setScale(PageOperation.PAGE_DEFAULT_SCALE);
		pageOperation.setDefaultAlign();
		pageOperation.setDefaultPosition();
	}

	public void replaceToHighQuality() {
		if (mCurrentPageOperation.canReplaceToQuality(LoadQuality.HIGH)) {
			mPageLoadManager.addPageLoadThread(mCurrentPageOperation, LoadQuality.HIGH);
		}
	}

	private void replaceToNormalQuality(PageOperation pageOperation) {
		if (pageOperation.canReplaceToQuality(LoadQuality.NEUTRAL)) {
			mPageLoadManager.addPageLoadThread(pageOperation, LoadQuality.NEUTRAL);
		}
	}

	public void updatePages() {
		int convertedCurrentPageIndex = mEpubScrollView.convertedPage(mEpubScrollView.getCurrentPageIndex());
		int drawCurrentPageIndex = mEpubScrollView.getPageIndex(mCurrentPageOperation.getPage());

		if (drawCurrentPageIndex == -1 || convertedCurrentPageIndex >= drawCurrentPageIndex + 3 || convertedCurrentPageIndex <= drawCurrentPageIndex - 3) {
			// getPageがnullだったり、見開き切り替え時などでgetPageしたPageがページリスト内に無かったり、3ページ以上ずれてたら全とっかえ
			// stopAllPageLoadAndResetだとwaitが発生するのでひとまず3回シフトしとく、スレッド終了を待たない分メモリ食うかも？
			shiftPageLeft();
			shiftPageLeft();
			shiftPageLeft();
		} else if (convertedCurrentPageIndex > drawCurrentPageIndex) {
			// 左(下)のページ方向へ遷移
			for (int i = drawCurrentPageIndex; i < convertedCurrentPageIndex; i++) {
				shiftPageRight();
			}
		} else if (convertedCurrentPageIndex < drawCurrentPageIndex) {
			// 右(上)のページ方向へ遷移
			for (int i = drawCurrentPageIndex; i > convertedCurrentPageIndex; i--) {
				shiftPageLeft();
			}
		}
		loadCurrentPage();
		loadRightPage();
		loadLeftPage();
		mEpubScrollView.drawInvalidate();
	}

	private void shiftPageLeft() {
		mPageLoadManager.stopPageLoadThread(mLeftPageOperation);
		mLeftPageOperation.reset();

		PageOperation pageWork = mLeftPageOperation;
		mLeftPageOperation = mCurrentPageOperation;
		mCurrentPageOperation = mRightPageOperation;
		mRightPageOperation = pageWork;
	}

	private void shiftPageRight() {
		mPageLoadManager.stopPageLoadThread(mRightPageOperation);
		mRightPageOperation.reset();

		PageOperation pageWork = mRightPageOperation;
		mRightPageOperation = mCurrentPageOperation;
		mCurrentPageOperation = mLeftPageOperation;
		mLeftPageOperation = pageWork;
	}

	public void drawAllPage(Canvas canvas, float translateX, float translateY, boolean isClearBitmap, boolean isAnimating) {
		float drawX = 0;
		float drawY = 0;
		float pageDiffX = 0;
		float pageDiffY = 0;

		// 縦・横スクロールを判定し、Pageオブジェクトを元にしてビュー上の描画ポジション取得
		if (mViewMode.isVerticalScroll()) {
			drawY = translateY + mEpubScrollView.getPagePositionY(mEpubScrollView.getPageIndex(mCurrentPageOperation.getPage()));
			pageDiffY = mDisplaySize.height + EpubScrollView.PAGE_PADDING;
		} else {
			drawX = translateX + mEpubScrollView.getPagePositionX(mEpubScrollView.getPageIndex(mCurrentPageOperation.getPage()));
			pageDiffX = mDisplaySize.width + EpubScrollView.PAGE_PADDING;
		}
		LogUtil.v("drawX=%f, drawY=%f, pageDiffX=%f, pageDiffY=%f", drawX, drawY, pageDiffX, pageDiffY);
		mCurrentPageOperation.drawPage(canvas, drawX, drawY, isClearBitmap, isAnimating);
		mRightPageOperation.drawPage(canvas, drawX + pageDiffX, drawY - pageDiffY, isClearBitmap, isAnimating);
		mLeftPageOperation.drawPage(canvas, drawX - pageDiffX, drawY + pageDiffY, isClearBitmap, isAnimating);
	}
}
