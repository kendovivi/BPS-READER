package jp.bpsinc.android.viewer.function.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.example.bps_reader.R;

import jp.bpsinc.android.util.LogUtil;
import jp.bpsinc.android.viewer.db.AbstractRow;
import jp.bpsinc.android.viewer.db.BookmarkTable;
import jp.bpsinc.android.viewer.function.adapter.BookmarkListAdapter;
import jp.bpsinc.android.viewer.function.adapter.BookmarkListData;
import jp.bpsinc.android.viewer.function.content.BookmarkInfo;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class BookmarkActivity extends ListActivity {
	/** ビューアから渡されるインテントのキー */
	public static final String INTENT_KEY_BOOKMARK_INFO = "jp.bpsinc.android.viewer.function.activity.INTENT_KEY_BOOKMARK_INFO";
	/** しおり画面からビューアに返すインテント内のキー */
	public static final String INTENT_DATA_KEY_BOOKMARK_PAGE = "jp.bpsinc.android.viewer.function.activity.INTENT_DATA_KEY_BOOKMARK_PAGE";
	/** アダプター生成に使うブックマーク情報リスト */
	private List<BookmarkListData> mList;
	/** リストビューにセットするアダプター */
	private BookmarkListAdapter mAdapter;
	/** しおり情報操作用DBクラス */
	private BookmarkTable mBookmarkTable;
	/** しおり付けるのに必要なビューアから渡される情報 */
	private BookmarkInfo mBookmarkInfo;
	/** 編集ダイアログ用テキスト入力ボックスを文字数制限するためのフィルター */
	private InputFilter[] mInputFilter = new InputFilter[1];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogUtil.v();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.epub_viewer_bookmark);

		Intent intent = getIntent();
		mBookmarkInfo = (BookmarkInfo) intent.getSerializableExtra(INTENT_KEY_BOOKMARK_INFO);
		mBookmarkTable = new BookmarkTable(getApplicationContext(), mBookmarkInfo.getUserId(), mBookmarkInfo.getBookId());

		((Button) findViewById(R.id.bookmark_add_button)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// しおりをつけるボタンの処理
				List<AbstractRow> bookmarkList = mBookmarkTable.getBookmark(mBookmarkInfo.getBookmarkPage());
				if (bookmarkList.size() == 0) {
					// ラベルの初期値は現在時刻
					Calendar calendar = Calendar.getInstance();
					String label = String.format(Locale.getDefault(), "%4d/%02d/%02d %02d:%02d:%02d",
							calendar.get(Calendar.YEAR),
							calendar.get(Calendar.MONTH) + 1,
							calendar.get(Calendar.DATE),
							calendar.get(Calendar.HOUR_OF_DAY),
							calendar.get(Calendar.MINUTE),
							calendar.get(Calendar.SECOND));
					mBookmarkTable.insertBookmark(mBookmarkInfo.getBookmarkPage(), label, label);
					mList.add(new BookmarkListData(mBookmarkInfo.getBookmarkPage(), label));
					mAdapter.notifyDataSetChanged();
				}
			}
		});

		// テキスト入力ボックスの文字数制限を128にしておく
		mInputFilter[0] = new InputFilter.LengthFilter(128);

		// しおり表示に使用するアダプターを生成してリストをセット
		mList = new ArrayList<BookmarkListData>();
		mAdapter = new BookmarkListAdapter(this, mList);
		setListAdapter(mAdapter);
		listInit();
	}

	@Override
	protected void onListItemClick(ListView listView, View v, int position, long id) {
		LogUtil.v("position=%d, id=%d", position, id);
		BookmarkListData bookmarkListData = (BookmarkListData) listView.getItemAtPosition(position);
		Intent intent = new Intent();
		intent.putExtra(INTENT_DATA_KEY_BOOKMARK_PAGE, bookmarkListData.getBookmarkPage());
		setResult(RESULT_OK, intent);
		finish();
	}

	/**
	 * このメソッドはepub_viewer_bookmark_row.xml内でButtonのonClickに指定している<br>
	 * 
	 * @param v リストビューの編集ボタンのビュー
	 */
	public void onEditButtonClick(View v) {
		final BookmarkListData listData = (BookmarkListData) v.getTag();
		LogUtil.v("bookmarkPage=%d, label=%s", listData.getBookmarkPage(), listData.getLabel());

		final EditText labelEditText = new EditText(this);
		labelEditText.setFilters(mInputFilter);
		labelEditText.setText(listData.getLabel());
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.epub_viewer_bookmark_edit_dialog_title);
		builder.setView(labelEditText);
		builder.setPositiveButton(R.string.epub_viewer_bookmark_edit_dialog_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 編集ダイアログでOKボタン押した時の処理(ラベルのアップデート処理)
				String newLabel = labelEditText.getText().toString();
				if (newLabel.length() > 0 && newLabel.equals(listData.getLabel()) == false) {
					mBookmarkTable.updateBookmarkLabel(listData.getBookmarkPage(), newLabel);
					listData.setLabel(newLabel);
					mAdapter.notifyDataSetChanged();
				}
			}
		});
		builder.setNegativeButton(R.string.epub_viewer_bookmark_edit_dialog_delete, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 編集ダイアログで削除ボタン押した時の処理
				mBookmarkTable.deleteBookmark(listData.getBookmarkPage());
				mList.remove(mList.indexOf(listData));
				mAdapter.notifyDataSetChanged();
			}
		});
		builder.show();
	}

	private void listInit() {
		mList.clear();
		List<AbstractRow> bookmarkList = mBookmarkTable.getBookmark();
		for (AbstractRow row : bookmarkList) {
			BookmarkTable.Row bookmarkRow = (BookmarkTable.Row) row;
			mList.add(new BookmarkListData(bookmarkRow.getBookmarkPage(), bookmarkRow.getLabel()));
		}
		mAdapter.notifyDataSetChanged();
	}

	public BookmarkInfo getBookmarkInfo() {
		return mBookmarkInfo;
	}
}
