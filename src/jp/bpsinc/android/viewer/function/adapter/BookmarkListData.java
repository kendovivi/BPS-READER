package jp.bpsinc.android.viewer.function.adapter;

public class BookmarkListData {
	private int mBookmarkPage;
	private String mLabel;

	public BookmarkListData(int bookmarkPage, String label) {
		mBookmarkPage = bookmarkPage;
		mLabel = label;
	}

	public int getBookmarkPage() {
		return mBookmarkPage;
	}

	public String getLabel() {
		return mLabel;
	}

	public void setLabel(String label) {
		mLabel = label;
	}
}
