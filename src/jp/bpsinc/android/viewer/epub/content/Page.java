package jp.bpsinc.android.viewer.epub.content;

import java.io.Serializable;

import jp.bpsinc.android.epub.info.OpfItem;

@SuppressWarnings("serial")
public class Page implements Serializable {
	/** 単一ページで使うOPF情報 */
	private OpfItem mCenterOpfItem;

	/** 見開き左ページで使うOPF情報 */
	private OpfItem mLeftOpfItem;

	/** 見開き右ページで使うOPF情報 */
	private OpfItem mRightOpfItem;

	/** ページ数(本全体の何ページ目か) */
	private int mThisPage;

	public Page(OpfItem centerOpfItem, int currentPage) {
		if (centerOpfItem == null) {
			throw new IllegalArgumentException("Single page item must not be null");
		}
		mCenterOpfItem = centerOpfItem;
		mThisPage = currentPage;
	}

	public Page(OpfItem leftOpfItem, OpfItem rightOpfItem, int currentPage) {
		if (leftOpfItem == null && rightOpfItem == null) {
			throw new IllegalArgumentException("Both left and right is null");
		}
		mLeftOpfItem = leftOpfItem;
		mRightOpfItem = rightOpfItem;
		mThisPage = currentPage;
	}

	public OpfItem getCenterOpfItem() {
		return mCenterOpfItem;
	}

	public OpfItem getLeftOpfItem() {
		return mLeftOpfItem;
	}

	public OpfItem getRightOpfItem() {
		return mRightOpfItem;
	}

	/**
	 * 本全体に対するこのページのページ数を取得する<br>
	 * 全100ページの場合、オブジェクトごとに0～99までの値を保持する<br>
	 * 見開きページの場合は手前のページ数を取得する(3～4ページを保持している場合は2(3ページ目)を取得する)
	 * 
	 * @return ページ数(本全体の何ページ目か)
	 */
	public int getThisPage() {
		return mThisPage;
	}
}
