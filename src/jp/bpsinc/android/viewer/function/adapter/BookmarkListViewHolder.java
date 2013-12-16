package jp.bpsinc.android.viewer.function.adapter;

import android.widget.Button;
import android.widget.TextView;

public class BookmarkListViewHolder {
	private final TextView mBookmarkPage;
	private final TextView mLabel;
	private final Button mEditButton;

	public BookmarkListViewHolder(TextView bookmarkPage, TextView label, Button editButton) {
		mBookmarkPage = bookmarkPage;
		mLabel = label;
		mEditButton = editButton;
	}

	public TextView getBookmarkPage() {
		return mBookmarkPage;
	}

	public TextView getLabel() {
		return mLabel;
	}

	public Button getEditButton() {
		return mEditButton;
	}
}
