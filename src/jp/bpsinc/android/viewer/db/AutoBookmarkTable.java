package jp.bpsinc.android.viewer.db;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class AutoBookmarkTable extends AbstractTable {
	public static final String TABLE_NAME = "auto_bookmark";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_BOOK_ID = "book_id";
	public static final String COLUMN_LAST_PAGE = "last_page";
	public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ COLUMN_USER_ID + " TEXT,"
			+ COLUMN_BOOK_ID + " TEXT,"
			+ COLUMN_LAST_PAGE + " INTEGER NOT NULL,"
			+ "CONSTRAINT auto_bookmark_pkey PRIMARY KEY (" + COLUMN_USER_ID + "," + COLUMN_BOOK_ID + ")"
			+ ");";
	public static final String SQL_WHERE_PRIMARY = COLUMN_USER_ID + " = ? AND " + COLUMN_BOOK_ID + " = ?";

	private final String mUserId;
	private final String mBookId;
	private final String[] mSelectionArgsPrimary;

	public AutoBookmarkTable(Context context, String userId, String bookId) {
		super(context);
		mUserId = userId;
		mBookId = bookId;
		mSelectionArgsPrimary = new String[] {mUserId, mBookId};
	}

	public Row getAutoBookmark() {
		Row row = null;
		List<AbstractRow> list = findAll(SQL_WHERE_PRIMARY, mSelectionArgsPrimary);
		if (list.size() > 0) {
			row = (Row) list.get(0);
		}
		return row;
	}

	public void setAutoBookmark(int pageIndex) {
		Row row = new Row();
		row.mUserId = mUserId;
		row.mBookId = mBookId;
		row.mLastPage = pageIndex;
		replace(row);
	}

	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}

	@Override
	protected AbstractRow buildRow(Cursor cursor) {
		return Row.build(cursor);
	}

	/**
	 * TABLEの1行を表現
	 */
	public static class Row extends AbstractRow {
		private String mUserId;
		private String mBookId;
		private int mLastPage;

		public static Row build(Cursor cursor) {
			Row row = new Row();
			row.mUserId = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID));
			row.mBookId = cursor.getString(cursor.getColumnIndex(COLUMN_BOOK_ID));
			row.mLastPage = cursor.getInt(cursor.getColumnIndex(COLUMN_LAST_PAGE));
			return row;
		}

		@Override
		public ContentValues toContentValues() {
			ContentValues values = new ContentValues();
			values.put(COLUMN_USER_ID, mUserId);
			values.put(COLUMN_BOOK_ID, mBookId);
			values.put(COLUMN_LAST_PAGE, mLastPage);
			return values;
		}

		public String getUserId() {
			return mUserId;
		}

		public String getBookId() {
			return mBookId;
		}

		public int getLastPage() {
			return mLastPage;
		}
	}
}
