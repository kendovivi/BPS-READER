package jp.bpsinc.android.viewer.epub.view.util;

import java.io.Serializable;

/**
 * ビューアの各種設定を保持する<br>
 * このクラスのインスタンスをビューアに1つ作成し、全ての箇所で使用できるようにクラス間で引き回す<br>
 * 複数のインスタンスを作成したり、このクラス内に保持する情報(端末の向きや見開き情報など)を別のクラス上に分散して保持してはいけない<br>
 * ビューアに1つだけ存在するこのクラスのインスタンスの設定値を1回変更すれば、それを使用している全ての箇所に反映されるように使用すること
 */
@SuppressWarnings("serial")
public class ViewMode implements Serializable {
	/** 画面の表示モード */
	public enum Mode {
		/** 画面の向き：縦、ページ：単一 */
		PORTRAIT_STANDARD,
		/** 画面の向き：縦、ページ：見開き */
		PORTRAIT_SPREAD,
		/** 画面の向き：横、ページ：単一 */
		LANDSCAPE_STANDARD,
		/** 画面の向き：横、ページ：見開き */
		LANDSCAPE_SPREAD,
	}
	/** コンテンツごとの動作モード */
	public enum ContentMode {
		/** Fixed-Layout */
		FXL,
		/** OpenMangaFormat */
		OMF,
	}
	/** 画像表示直後の表示方法(ここで指定したフィットモードに対応する初期サイズが最低縮小率になる) */
	public enum FitMode {
		/** 画像を画面にアスペクトフィットさせる */
		ASPECT_FIT,
		/** 画像の横幅を画面の横幅にフィットさせる */
		WIDTH_FIT,
		/** 画像の縦幅を画面の縦幅にフィットさせる */
		HEIGHT_FIT,
	}
	/** ページめくり時のアニメーション効果設定 */
	public enum PageAnimation {
		/** ページアニメーション：なし */
		NONE,
		/** ページアニメーション：スライド */
		SLIDE,
	}

	private final ContentMode mContentMode;
	private final boolean mIsLandscape;
	private Mode mMode;
	private FitMode mFitMode;
	/** 現在の設定が見開き家 */
	private boolean mIsSpread;
	/** 現在のモードが縦スクロールか */
	private boolean mIsVerticalScroll;

	/** 見開き時のカレントページのデフォルトポジションを後続ページにする場合true */
	private boolean mIsSubsequentPagePositionFlag;
	/** 現在のページアニメーション設定 */
	private PageAnimation mPageAnimation;

	public ViewMode(ContentMode contentMode, boolean isLandscape, boolean isSpread) {
		mContentMode = contentMode;
		mIsLandscape = isLandscape;
		mIsSpread = isSpread;
		setMode();
		setFitMode();
		setIsVertical();

		mIsSubsequentPagePositionFlag = false;
		mPageAnimation = PageAnimation.SLIDE;
	}

	private void setMode() {
		if (mIsLandscape) {
			if (mIsSpread) {
				mMode = Mode.LANDSCAPE_SPREAD;
			} else {
				mMode = Mode.LANDSCAPE_STANDARD;
			}
		} else {
			switch (mContentMode) {
			case FXL:
				mMode = Mode.PORTRAIT_STANDARD;
				break;
			default:
				if (mIsSpread) {
					mMode = Mode.PORTRAIT_SPREAD;
				} else {
					mMode = Mode.PORTRAIT_STANDARD;
				}
				break;
			}
		}
	}

	/**
	 * セットした端末の向きと見開き設定を元に、Viewの表示モードを取得する
	 * 
	 * @return Viewの表示モード
	 */
	public Mode getMode() {
		return mMode;
	}

	public ContentMode getContentMode() {
		return mContentMode;
	}

	private void setFitMode() {
		switch (mMode) {
		case PORTRAIT_STANDARD:
			mFitMode = FitMode.ASPECT_FIT;
			break;
		case PORTRAIT_SPREAD:
			mFitMode = FitMode.HEIGHT_FIT;
			break;
		case LANDSCAPE_STANDARD:
			mFitMode = FitMode.WIDTH_FIT;
			break;
		case LANDSCAPE_SPREAD:
			mFitMode = FitMode.ASPECT_FIT;
			break;
		}
	}

	/**
	 * セットした端末の向きと見開き設定を元に、画像の表示モードを取得する
	 * 
	 * @return 画像の表示モード
	 */
	public FitMode getFitMode() {
		return mFitMode;
	}

	/**
	 * 設定した端末の向きを取得
	 * 
	 * @return 横向きに設定されている場合true、縦向きに設定されている場合false
	 */
	public boolean isLandscape() {
		return mIsLandscape;
	}

	public void setIsSpread(boolean isSpread) {
		mIsSpread = isSpread;
		setMode();
		setFitMode();
		setIsVertical();
	}

	/**
	 * 設定した見開き状態を取得
	 * 
	 * @return 見開きならtrue、単ページならfalse
	 */
	public boolean isSpread() {
		return mIsSpread;
	}

	private void setIsVertical() {
		switch (mContentMode) {
		case OMF:
			if (mMode == Mode.LANDSCAPE_STANDARD) {
				mIsVerticalScroll = true;
			} else {
				mIsVerticalScroll = false;
			}
			break;
		default:
			mIsVerticalScroll = false;
			break;
		}
	}

	/**
	 * セットしたコンテンツの種類、端末の向きと見開き設定を元に、ビューのスクロール方向を取得する
	 * 
	 * @return 縦スクロールの場合true、横スクロールの場合false
	 */
	public boolean isVerticalScroll() {
		return mIsVerticalScroll;
	}

	/**
	 * 見開き時のカレントページのデフォルトポジションを後続ページにするかどうか
	 * 
	 * @return デフォルトポジションを後続ページにする場合はtrue、それ以外ならfalse
	 */
	public boolean isSubsequentPagePositionFlag() {
		return mIsSubsequentPagePositionFlag;
	}

	public void setIsSubsequentPagePositionFlag(boolean isSubsequentPagePositionFlag) {
		mIsSubsequentPagePositionFlag = isSubsequentPagePositionFlag;
	}

	/**
	 * 現在のページアニメーション設定を取得
	 * 
	 * @return ページアニメーション設定
	 */
	public PageAnimation getPageAnimation() {
		return mPageAnimation;
	}

	public void setPageAnimation(PageAnimation pageAnimation) {
		this.mPageAnimation = pageAnimation;
	}
}
