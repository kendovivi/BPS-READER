package jp.bpsinc.android.viewer.function.adapter;

import java.util.List;

import com.example.bps_reader.R;

import jp.bpsinc.android.viewer.function.activity.BookmarkActivity;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BookmarkListAdapter extends ArrayAdapter<BookmarkListData> {
	private final BookmarkActivity mBookmarkActivity;
	private final LayoutInflater mInflater;
	private int mBookmarkRowPageMeasureTextMaxSize;

	public BookmarkListAdapter(BookmarkActivity bookmarkActivity, List<BookmarkListData> objects) {
		super(bookmarkActivity, 0, objects);
		mBookmarkActivity = bookmarkActivity;
		mInflater = (LayoutInflater) bookmarkActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mBookmarkRowPageMeasureTextMaxSize = 0;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		TextView label = null;
		TextView bookmarkPage = null;
		Button editButton = null;
		BookmarkListData listData = getItem(position);

		if (convertView == null) {
			// 1行のレイアウト生成
			convertView = mInflater.inflate(R.layout.epub_viewer_bookmark_row, null);

			// 効率化(再利用)のためにビューを保存
			label = (TextView) convertView.findViewById(R.id.bookmark_row_label);
			bookmarkPage = (TextView) convertView.findViewById(R.id.bookmark_row_page);
			if (mBookmarkRowPageMeasureTextMaxSize == 0) {
				Paint paint = new Paint();
				paint.setTextSize(bookmarkPage.getTextSize());
				mBookmarkRowPageMeasureTextMaxSize = (int) paint.measureText(mBookmarkActivity.getBookmarkInfo().getBookPageCount() + "ページ") + 1;
			}
			LinearLayout.LayoutParams linearLayoutParams = (LinearLayout.LayoutParams) bookmarkPage.getLayoutParams();
			linearLayoutParams.width = mBookmarkRowPageMeasureTextMaxSize;
			bookmarkPage.setLayoutParams(linearLayoutParams);
			editButton = (Button) convertView.findViewById(R.id.bookmark_row_edit_button);
			convertView.setTag(new BookmarkListViewHolder(bookmarkPage, label, editButton));
		} else {
			BookmarkListViewHolder holder = (BookmarkListViewHolder) convertView.getTag();
			label = holder.getLabel();
			bookmarkPage = holder.getBookmarkPage();
			editButton = holder.getEditButton();
		}
		// 毎回セットし直さないとポジションおかしくなるので気をつけること
		label.setText(listData.getLabel());
		bookmarkPage.setText(String.valueOf(listData.getBookmarkPage() + 1) + "ページ");
		editButton.setTag(listData);

		return convertView;
	}
}
