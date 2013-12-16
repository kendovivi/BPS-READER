package jp.bpsinc.android.viewer.epub.view;

import java.util.List;

import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.epub.activity.EpubViewerActivity;
import jp.bpsinc.android.viewer.epub.content.EpubPageAccess;
import jp.bpsinc.android.viewer.epub.content.Page;
import jp.bpsinc.android.viewer.epub.content.PageOperation;
import jp.bpsinc.android.viewer.epub.content.RetentionPageHelper;
import jp.bpsinc.android.viewer.epub.fxl.listener.FxlEpubViewerOnGestureListener;
import jp.bpsinc.android.viewer.epub.listener.EpubViewerOnGestureListener;
import jp.bpsinc.android.viewer.epub.listener.EpubViewerOnScaleGestureListener;
import jp.bpsinc.android.viewer.epub.omf.listener.OmfEpubViewerOnGestureListener;
import jp.bpsinc.android.viewer.epub.util.PageLoadManager;
import jp.bpsinc.android.viewer.epub.util.PageLoadThread.PageLoadCompleteListener;
import jp.bpsinc.android.viewer.epub.view.util.FinishDetectableScroller;
import jp.bpsinc.android.viewer.epub.view.util.ViewMode;
import jp.bpsinc.android.viewer.epub.view.util.ViewMode.FitMode;
import jp.bpsinc.android.viewer.epub.view.util.ViewMode.PageAnimation;
import jp.bpsinc.android.viewer.util.MyViewCompat;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Build;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

@SuppressLint("ViewConstructor")
public class EpubScrollView extends View {
	private final EpubViewerActivity mEpubViewerActivity;
	/** EPUBエントリにアクセスするためのインスタンス */
	private final EpubPageAccess mEpubPageAccess;
	/** 画像を読み込み、保持、描画などを行う */
	private RetentionPageHelper mRetentionPageHelper;
	/** スクロール状態を制御する */
	private final FinishDetectableScroller mScroller;
	/** タップジェスチャー用のリスナー */
	private final EpubViewerOnGestureListener mGestureListener;
	/** タップジェスチャー用のデテクター */
	private final GestureDetector mGestureDetector;
	/** スケールジェスチャー用のリスナー */
	private final EpubViewerOnScaleGestureListener mScaleGestureListener;
	/** スケールジェスチャー用のデテクター */
	private final ScaleGestureDetector mScaleGestureDetector;

	/** EPUBビューアの各種設定の取得などに使用 */
	private final ViewMode mViewMode;
	/** ビューの描画開始位置 */
	private final PointF mPosition;
	/** 現在ページ */
	private int mCurrentPageIndex;
	/** 単ページ、または、見開きのページリスト */
	private List<Page> mPageList;

	/** ズーム処理中を表すフラグ */
	private boolean mZoomAnimating;
	/** ズーム処理中に描画を続けるための切り替えフラグ */
	private boolean mPendingZoomAnimating;

	/** 拡大時や横スクロール＋横フィット時にページ遷移するために必要な最低スクロール量 */
	private static float VIEW_JUMP_MIN_SCROLL_AMOUNT = 60;
	/** ページとページの隙間サイズ */
	public static final int PAGE_PADDING = 30;
	/** ハニカムのバージョン */
	private static final int VERSION_HONEYCOMB = 11;

	/**
	 * 
	 * @param epubViewerActivity 設定済みのEpubViewerActivityインスタンス
	 * @param epubPageAccess 設定済みのEpubPageAccessインスタンス
	 * @param viewMode 設定済みのViewModeインスタンス
	 * @param currentPageIndex 初期表示するページ数
	 */
	public EpubScrollView(EpubViewerActivity epubViewerActivity, EpubPageAccess epubPageAccess, ViewMode viewMode, int currentPageIndex) {
		super(epubViewerActivity);
		mEpubViewerActivity = epubViewerActivity;
		mEpubPageAccess = epubPageAccess;
		mViewMode = viewMode;
		mCurrentPageIndex = currentPageIndex;
		mPosition = new PointF();
		mRetentionPageHelper = null;
		mScroller = new FinishDetectableScroller(epubViewerActivity, new DecelerateInterpolator(2f));
		switch (mViewMode.getContentMode()) {
		case FXL:
			mGestureListener = new FxlEpubViewerOnGestureListener(mEpubViewerActivity, this);
			break;
		case OMF:
			mGestureListener = new OmfEpubViewerOnGestureListener(mEpubViewerActivity, this);
			break;
		default:
			mGestureListener = new FxlEpubViewerOnGestureListener(mEpubViewerActivity, this);
			break;
		}
		mGestureDetector = new GestureDetector(mEpubViewerActivity, mGestureListener);
		mGestureDetector.setIsLongpressEnabled(false);
		mScaleGestureListener = new EpubViewerOnScaleGestureListener(this);
		mScaleGestureDetector = new ScaleGestureDetector(epubViewerActivity, mScaleGestureListener);
		mZoomAnimating = false;
		mPendingZoomAnimating = false;
		setPageList();
	}

	public void destroy() {
		if (mRetentionPageHelper != null) {
			mRetentionPageHelper.stopAllPageLoadAndReset();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		LogUtil.v("action=%d", action);

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			LogUtil.v("MotionEvent DOWN");

			// ページ内のスクロール中にタップしたらスクロール停止
			if (mScroller.scrollingTargetIsPage() && mScroller.isFinished() == false) {
				mScroller.abortAnimation();
				mScroller.setScrollingTargetIsPage(false);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			LogUtil.v("MotionEvent MOVE");
			break;
		case MotionEvent.ACTION_UP:
			LogUtil.v("MotionEvent UP");
		case MotionEvent.ACTION_CANCEL:
			mGestureListener.setIsPageMoving(false);

			if (mScroller.isFinishedAndFinishEventProcessed()) {
				int startX = 0;
				int startY = 0;
				int destX = 0;
				int destY = 0;
				float diffX = 0;
				float diffY = 0;
				int currentPageIndex = convertedPage(mCurrentPageIndex);
				if (mViewMode.isVerticalScroll()) {
					diffY = mPosition.y + getPagePositionY(currentPageIndex);
					// 次・前ページへ遷移可能か判定
					if (isViewJumpMinScrollAmount(diffX, diffY)) {
						if (diffY > 0) {
							// 上から下にスクロール
							currentPageIndex--;
						} else if (diffY < 0) {
							currentPageIndex++;
						}
					}
					startY = (int) mPosition.y;
					destY = (int) -getPagePositionY(currentPageIndex);
				} else {
					diffX = mPosition.x + getPagePositionX(currentPageIndex);
					// 次・前ページへ遷移可能か判定
					if (isViewJumpMinScrollAmount(diffX, diffY)) {
						if (diffX > 0) {
							// 左から右にスクロール
							currentPageIndex++;
						} else if (diffX < 0) {
							// 右から左にスクロール
							currentPageIndex--;
						}
					}
					startX = (int) mPosition.x;
					destX = (int) -getPagePositionX(currentPageIndex);
				}
				// ページ遷移やスクロール量が少なくて元のページに戻る場合などにスクロール処理を実行
				if ((startX != destX) || (startY != destY)) {
					LogUtil.v("ACTION_UP startX=%d, startY=%d, destX=%d, destY=%d", startX, startY, destX, destY);
					mCurrentPageIndex = convertedPage(currentPageIndex);
					scrollTo(false, startX, startY, destX - startX, destY - startY, FinishDetectableScroller.DURATION_DRAG);
				}
			}
			break;
		default:
			break;
		}
		if (mScroller.isFinishedAndFinishEventProcessed() && mGestureListener.isPageMoving() == false) {
			// スクロール中でなければスケールジェスチャー処理を実行
			mScaleGestureDetector.onTouchEvent(event);
		}
		return mGestureDetector.onTouchEvent(event);
	}

	@Override
	public void computeScroll() {
		LogUtil.v();
		if (mScroller.scrollingTargetIsPage()) {
			// ページ内スクロール(ページのポジション変更)
			if (mScroller.computeScrollOffset()) {
				mRetentionPageHelper.getCurrentPageOperation().setPosition(mScroller.getCurrX(), mScroller.getCurrY());
				MyViewCompat.postInvalidateOnAnimation(this);
			}
		} else {
			// ページ移動(ビューのポジション変更)
			if (mScroller.computeScrollOffset()) {
				viewMoveTo(mScroller.getCurrX(), mScroller.getCurrY());

				// アニメーション終了時に再描画を行ってしまうと、ダブルタップによる見開き切り替えのタイミングによっては
				// 画像読み込み中にonDrawが走り一瞬空白が表示されてしまうため最後は再描画を行わない
				if (mScroller.isFinished() == false) {
					drawInvalidate();
				}
			}
		}
		if (mScroller.isFinishedNow()) {
			LogUtil.v("isFinished");
			if (mScroller.scrollingTargetIsPage() == false) {
				mRetentionPageHelper.updatePages();
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		LogUtil.v();
		super.onDraw(canvas);

		if (mRetentionPageHelper == null) {
			return;
		}

		boolean isClearBitmap = true;
		// TODO 自動スクロール系もOFFにした方が良い？2系の端末だと多少速度差があるけどスクロール停止タイミングでアンチエイリアスがかかる動きも微妙に気になる
		if ((mScroller.isFinishedAndFinishEventProcessed() == false || mZoomAnimating) && isViewHardwareAccelerated() == false) {
//		if (mZoomAnimating && isViewHardwareAccelerated() == false) {
			// GPUレンダリング無効環境では、高速化のためにアニメーション中アンチエイリアスをOFFにする
			isClearBitmap = false;
		}

		switch (mViewMode.getContentMode()) {
		case FXL:
			// 背景色 #000000
			canvas.drawColor(Color.rgb(0, 0, 0));
			break;
		case OMF:
			// 背景色 #FFFFFF
			canvas.drawColor(Color.rgb(255, 255, 255));
			break;
		}
		float tx = mPosition.x;
		float ty = mPosition.y;
		switch (mViewMode.getPageAnimation()) {
		case NONE:
			// ページアニメーションなしに設定されている場合、現在のビューポジション無視して常にページごとの初期位置を固定値として使う
			if (mViewMode.isVerticalScroll()) {
				ty = getPagePositionY(convertedPage(mCurrentPageIndex)) * -1;
			} else {
				tx = getPagePositionX(convertedPage(mCurrentPageIndex)) * -1;
			}
			break;
		default:
			break;
		}
		mRetentionPageHelper.drawAllPage(canvas, tx, ty, isClearBitmap, mZoomAnimating || mScroller.isFinished() == false);

		// ズーム処理時にフラグを切り替えながら描画を続ける
		if (mPendingZoomAnimating != mZoomAnimating) {
			mZoomAnimating = mPendingZoomAnimating;
			drawInvalidate();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		LogUtil.v("changed=%b", changed);
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			if (mRetentionPageHelper == null) {
				PageLoadManager pageLoadManager = new PageLoadManager(mEpubViewerActivity, mEpubPageAccess,
						new PageLoadCompleteListener() {
							@Override
							public void onLoadComplete(PageOperation pageOperation) {
								PageOperation currentPage = mRetentionPageHelper.getCurrentPageOperation();

								// 横フィットや縦フィット時に画面より画像のサイズの方が小さい場合、デフォルト表示位置が画面の端によってしまうのでポジションをセットし直す
								// この処理は画像のサイズを取得できる状況(画像読み込み後)でないと無理なため、ここで行っている
								// 拡大処理で再読み込みが発生した場合はポジション変更したらまずい
								if (currentPage != pageOperation || pageOperation.isZoomed() == false) {
									pageOperation.setDefaultAlign();
									pageOperation.setDefaultPosition();
								}
								if (currentPage == pageOperation) {
									drawPostInvalidate();
								}
							}
						}
				);
				// 最初の1回のみ生成、ビューの縦幅・横幅とRTL指定はずっと変わらない、mViewModeの中身は見開き切替時などに変更される
				mRetentionPageHelper = new RetentionPageHelper(this, mEpubPageAccess, pageLoadManager, getWidth(), getHeight());
				viewDefaultUpdatePage();
			}
		}
	}

	/**
	 * UIスレッド用:Bitmapを1ページ以上読み込み済みなら再描画<br>
	 * ※画像読み込み前に再描画を行ってしまうと読み込み終了まで白画面が表示されてしまう(ここで再描画されなくても読み込み終了時に再描画をする)
	 */
	public void drawInvalidate() {
		boolean hasBitmap = mRetentionPageHelper.getCurrentPageOperation().hasBitmap();
		LogUtil.v("hasBitmap=%b", hasBitmap);
		if (hasBitmap) {
			invalidate();
		}
	}

	/**
	 * 非UIスレッド用：Bitmapを1ページ以上読み込み済みなら再描画<br>
	 * ※画像読み込み前に再描画を行ってしまうと読み込み終了まで白画面が表示されてしまう(ここで再描画されなくても読み込み終了時に再描画をする)
	 */
	public void drawPostInvalidate() {
		boolean hasBitmap = mRetentionPageHelper.getCurrentPageOperation().hasBitmap();
		LogUtil.v("hasBitmap=%b", hasBitmap);
		if (hasBitmap) {
			postInvalidate();
		}
	}

	/**
	 * 見開き設定を切り替えて再読み込み
	 */
	public void spreadChange() {
		if (mEpubViewerActivity.isSpreadPageList()) {
			mCurrentPageIndex = mEpubPageAccess.getSingleIndexFromSpreadIndex(mCurrentPageIndex);
			if (isSubsequentPagePosition()) {
				// 見開き表示状態で後続ページ側にポジションが寄っている場合は単ページインデックスを+1する
				mCurrentPageIndex++;
			}
		} else {
			int singlePageIndex = mCurrentPageIndex;
			mCurrentPageIndex = mEpubPageAccess.getSpreadIndexFromSingleIndex(mCurrentPageIndex);
			mEpubViewerActivity.spreadSubsequentPageJudgment(singlePageIndex, mCurrentPageIndex);
		}
		mViewMode.setIsSpread(mViewMode.isSpread() == false);
		pageListInit();
	}

	/**
	 * 現在の設定に基づいてページリストの更新、ビューのポジションをカレントページに対するデフォルト値に更新、ページのアップデート
	 */
	public void pageListInit() {
		setPageList();
		viewDefaultUpdatePage();
	}

	/**
	 * ビューのポジションをカレントページに対するデフォルト値に更新してページのアップデート
	 */
	private void viewDefaultUpdatePage() {
		if (mRetentionPageHelper != null) {
			setViewDefaultPosition();
			mRetentionPageHelper.updatePages();
		}
	}

	/**
	 * 次のページヘビューをスクロールする
	 */
	public void nextPageScroll() {
		if (mCurrentPageIndex + 1 < getPageCount()) {
			mCurrentPageIndex++;
			int viewStartX = 0;
			int viewStartY = 0;
			int viewDestX = 0;
			int viewDestY = 0;
			if (mViewMode.isVerticalScroll()) {
				viewStartY = (int) mPosition.y;
				viewDestY = (int) -getPagePositionY(mCurrentPageIndex);
			} else {
				viewStartX = (int) mPosition.x;
				viewDestX = (int) -getPagePositionX(convertedPage(mCurrentPageIndex));
			}
			scrollTo(false, viewStartX, viewStartY, viewDestX - viewStartX, viewDestY - viewStartY, FinishDetectableScroller.DURATION_TAP);
		}
	}

	/**
	 * 前のページヘビューをスクロールする
	 */
	public void prevPageScroll() {
		if (mCurrentPageIndex > 0) {
			mCurrentPageIndex--;
			int viewStartX = 0;
			int viewStartY = 0;
			int viewDestX = 0;
			int viewDestY = 0;
			if (mViewMode.isVerticalScroll()) {
				viewStartY = (int) mPosition.y;
				viewDestY = (int) -getPagePositionY(mCurrentPageIndex);
			} else {
				viewStartX = (int) mPosition.x;
				viewDestX = (int) -getPagePositionX(convertedPage(mCurrentPageIndex));
			}
			scrollTo(false, viewStartX, viewStartY, viewDestX - viewStartX, viewDestY - viewStartY, FinishDetectableScroller.DURATION_TAP);
		}
	}

	/**
	 * ビュー(ページ遷移)、または、ページ内スクロールをする。
	 * 
	 * @param isPage trueの場合ページ内スクロール、falseの場合ビューのスクロール(ページ遷移)
	 * @param startX スクロール開始座標X
	 * @param startY スクロール開始座標Y
	 * @param destX X方向へのスクロール量
	 * @param destY Y方向へのスクロール量
	 * @param duration スクロールにかかる時間
	 */
	public void scrollTo(boolean isPage, int startX, int startY, int destX, int destY, int duration) {
		// ビュー(ページ遷移)スクロール時にページめくりアニメーションがNONEの場合、ページ遷移速度とアンチエイリアスの無効解除(Android2系の場合)のためにdurationを0にする
		if (isPage == false && mViewMode.getPageAnimation() == PageAnimation.NONE) {
			duration = 0;
		}
		// スクロールアニメーションありでビューのスクロール開始(ここでいうビューのスクロールはページ内の移動)
		mScroller.setScrollingTargetIsPage(isPage);
		mScroller.startScroll(startX, startY, destX, destY, duration);
		drawInvalidate();
	}

	public void jumpToPage(int pageIndex) {
		mCurrentPageIndex = pageIndex;
		// onLayout前の場合は以下のメソッドでは何もしない(あとでonLayout動いた時に動くのでカレントページだけ更新しとけば良い)
		viewDefaultUpdatePage();
	}

	/**
	 * ページ移動が横スクロール時はビューのX座標、縦スクロール時はビューのY座標を更新<br>
	 * (横スクロール時のY座標と縦スクロール時のX座標は使用されない)<br>
	 * 指定した座標が範囲外だった場合、範囲内に収まるように補正する
	 * 
	 * @param posX スクロール先のX座標
	 * @param posY スクロール先のY座標
	 */
	public void viewMoveTo(float posX, float posY) {
		if (mViewMode.isVerticalScroll()) {
			mPosition.y = getScrollTo(posY);
		} else {
			mPosition.x = getScrollTo(posX);
		}
		// TODO スクロール操作で最初・最終ページより先に進もうとした場合に何か表示させたい場合、ここで判定？
		LogUtil.v("mPosition.x=%f, mPosition.y=%f", mPosition.x, mPosition.y);
	}

	/**
	 * 指定した座標が範囲外だった場合、範囲内に収まるように補正した値を返す<br>
	 * 範囲内の場合は値は変化しない
	 * 
	 * @param base ページ移動が横スクロール時は座標X、縦スクロール時は座標Y
	 * @return
	 */
	private float getScrollTo(float base) {
		float move = base;
		float diff = 0;
		if (mViewMode.isVerticalScroll()) {
			diff = (getViewHeight() - getHeight()) * -1;
		} else {
			diff = (getViewWidth() - getWidth()) * -1;
		}
		if (base > 0) {
			move = 0;
		}
		if (base < diff) {
			move = diff;
		}
		LogUtil.v("base=%f, diff=%f", base, diff);
		return move;
	}

	/**
	 * 設定に対応したページの表示X座標を返す、返される表示座標はキャンバスに対する位置
	 * 
	 * @param pageIndex 表示座標を求めるページ数
	 * @return ページ数に対応したX座標
	 */
	public float getPagePositionX(int pageIndex) {
		// ページの表示X座標を返す 返される表示座標はキャンバスに対する位置
		float x;
		x = getViewWidth();
		x -= (getWidth() + PAGE_PADDING) * pageIndex;
		x -= getWidth();
		return x;
	}

	/**
	 * 設定に対応したページの表示Y座標を返す、返される表示座標はキャンバスに対する位置
	 * 
	 * @param pageIndex 表示座標を求めるページ数
	 * @return ページ数に対応したY座標
	 */
	public float getPagePositionY(int pageIndex) {
		return (getHeight() + PAGE_PADDING) * pageIndex;
	}

	/**
	 * 設定に対応したビューの横幅を返す
	 * 
	 * @return ・ページ移動が縦スクロール：ビューの横幅<br>
	 *         ・ページ移動が横スクロール：(ビューの横幅*ページ数)＋(パディング*(ページ数-1))
	 */
	private float getViewWidth() {
		if (mViewMode.isVerticalScroll()) {
			return getWidth();
		} else {
			return getWidth() * getPageCount() + PAGE_PADDING * (getPageCount() - 1);
		}
	}

	/**
	 * 設定に対応したビューの縦幅を返す>
	 * 
	 * @return ・ページ移動が縦スクロール：(ビューの縦幅*ページ数)＋(パディング*(ページ数-1))<br>
	 *         ・ページ移動が横スクロール：ビューの縦幅
	 */
	private float getViewHeight() {
		if (mViewMode.isVerticalScroll()) {
			return getHeight() * getPageCount() + PAGE_PADDING * (getPageCount() - 1);
		} else {
			return getHeight();
		}
	}

	/**
	 * 使用するページリストを現在の設定にあったものに変更する
	 */
	private void setPageList() {
		if (mEpubViewerActivity.isSpreadPageList()) {
			mPageList = mEpubPageAccess.getSpreadPageList();
		} else {
			mPageList = mEpubPageAccess.getSinglePageList();
		}
	}

	/**
	 * カレントページインデックスを使用してビューのポジションを初期化する
	 */
	private void setViewDefaultPosition() {
		if (mViewMode.isVerticalScroll()) {
			mPosition.set(0, -getPagePositionY(mCurrentPageIndex));
		} else {
			mPosition.set(-getPagePositionX(convertedPage(mCurrentPageIndex)), 0);
		}
	}

	public PointF getPosition() {
		return mPosition;
	}

	public ViewMode getViewMode() {
		return mViewMode;
	}

	public int getPageCount() {
		return mPageList.size();
	}

	public Page getCurrentPage() {
		return mPageList.get(convertedPage(mCurrentPageIndex));
	}

	public Page getRightPage() {
		return mPageList.get(convertedPage(mCurrentPageIndex) - 1);
	}

	public Page getLeftPage() {
		return mPageList.get(convertedPage(mCurrentPageIndex) + 1);
	}

	public int getCurrentPageIndex() {
		return mCurrentPageIndex;
	}

	/**
	 * 渡したページインデックスを、横スクロール＋LTRの時に逆順にする
	 * 
	 * @param pageIndex ページインデックス
	 * @return 横スクロール＋LTRの場合：総ページ数 - pageIndex - 1、それ以外の場合：pageIndex
	 */
	public int convertedPage(int pageIndex) {
		if (mEpubViewerActivity.isConvertedPage()) {
			return mPageList.size() - pageIndex - 1;
		} else {
			return pageIndex;
		}
	}

	public boolean isConvertedPage() {
		return mEpubViewerActivity.isConvertedPage();
	}

	public RetentionPageHelper getRetentionPageHelper() {
		return mRetentionPageHelper;
	}

	public EpubPageAccess getEpubPageAccess() {
		return mEpubPageAccess;
	}

	public FinishDetectableScroller getScroller() {
		return mScroller;
	}

	/**
	 * Pageオブジェクトを元に、ページ数を取得する
	 * 
	 * @param page ページ数を確かめたいPageオブジェクト
	 * @return ページ数
	 */
	public int getPageIndex(Page page) {
		return mPageList.indexOf(page);
	}

	/**
	 * 次のページへ遷移するのに必要なスクロール量を満たしているか判定
	 * 
	 * @param diffX X方向へのスクロール量
	 * @param diffY Y方向へのスクロール量
	 * @return 横スクロール＋横フィット、または、ズーム中にスクロール量が一定以下の場合false、それ以外ならtrue
	 */
	private boolean isViewJumpMinScrollAmount(float diffX, float diffY) {
		return (((mViewMode.isVerticalScroll() == false && mViewMode.getFitMode() == FitMode.WIDTH_FIT) || mRetentionPageHelper.getCurrentPageOperation().isZoomed())
				&& diffX < VIEW_JUMP_MIN_SCROLL_AMOUNT && diffX > -VIEW_JUMP_MIN_SCROLL_AMOUNT
				&& diffY < VIEW_JUMP_MIN_SCROLL_AMOUNT && diffY > -VIEW_JUMP_MIN_SCROLL_AMOUNT) == false;
	}

	/**
	 * 見開きページの表示位置がスクロールなどにより後続ページよりになっているか判定
	 * 
	 * @return 画面の中央が後続ページ側に寄っている場合はtrue、それ以外ならfalse
	 */
	public boolean isSubsequentPagePosition() {
		if (mRetentionPageHelper == null) {
			return false;
		}
		boolean ret = false;
		PageOperation currentPageOperation = mRetentionPageHelper.getCurrentPageOperation();

		// 左右ページ読み込み済みか判定、読み込み前ならポジション変わってるはずないので常にfalse
		if (currentPageOperation.getLeftBitmap() != null && currentPageOperation.getRightBitmap() != null) {
			float spreadBitmapCenterPosition;	// 見開きページの中央位置

			switch (mViewMode.getMode()) {
			case PORTRAIT_SPREAD:
				// 縦見開きの場合は全体ページサイズの中央が見開きの中央とは限らないので、左画像の横幅を取得することにより見開きの中央位置を取得
				spreadBitmapCenterPosition = currentPageOperation.getLeftBitmapFitScaleWidth() * currentPageOperation.getScale();
				break;
			case LANDSCAPE_SPREAD:
				// 横見開きの場合は全体ページサイズを2で割って中央位置を取得
				spreadBitmapCenterPosition = (currentPageOperation.getBitmapWidth() * currentPageOperation.getScale()) / 2;
				break;
			default:
				return false;
			}
			if (mEpubPageAccess.isRTL()) {
				if (currentPageOperation.getPosition().x + spreadBitmapCenterPosition > getWidth() / 2) {
					ret = true;
				} else {
					ret = false;
				}
			} else {
				if (currentPageOperation.getPosition().x + spreadBitmapCenterPosition < getWidth() / 2) {
					ret = true;
				} else {
					ret = false;
				}
			}
		}
		return ret;
	}

	public void setZoomAnimating(boolean anim) {
		mPendingZoomAnimating = anim;
	}

	/**
	 * GPUレンダリングの有無を調査、本来はisHardwareAccelerated()を実行すべきだが、高速化のために簡易実装
	 * 
	 * @return ハニカム以降ならtrue、それ以前ならfalse
	 */
	private boolean isViewHardwareAccelerated() {
		return Build.VERSION.SDK_INT >= VERSION_HONEYCOMB;
	}
}
