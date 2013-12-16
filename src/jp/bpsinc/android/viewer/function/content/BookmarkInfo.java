package jp.bpsinc.android.viewer.function.content;

import java.io.Serializable;

@SuppressWarnings("serial")
public class BookmarkInfo implements Serializable {
	private final String mUserId;
	private final String mBookId;
	private final int mBookmarkPage;
	private final int mBookPageCount;

	public BookmarkInfo(String userId, String bookId, int bookmarkPage, int bookPageCount) {
		mUserId = userId;
		mBookId = bookId;
		mBookmarkPage = bookmarkPage;
		mBookPageCount = bookPageCount;
	}

	public String getUserId() {
		return mUserId;
	}

	public String getBookId() {
		return mBookId;
	}

	public int getBookmarkPage() {
		return mBookmarkPage;
	}

	public int getBookPageCount() {
		return mBookPageCount;
	}
}
