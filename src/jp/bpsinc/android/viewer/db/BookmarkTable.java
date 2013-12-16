package jp.bpsinc.android.viewer.db;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class BookmarkTable extends AbstractTable {
	public static final String TABLE_NAME = "bookmark";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_BOOK_ID = "book_id";
	public static final String COLUMN_BOOKMARK_PAGE = "bookmark_page";
	public static final String COLUMN_LABEL = "label";
	public static final String COLUMN_DATE = "date";
	public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ COLUMN_USER_ID + " TEXT,"
			+ COLUMN_BOOK_ID + " TEXT,"
			+ COLUMN_BOOKMARK_PAGE + " INTEGER NOT NULL,"
			+ COLUMN_LABEL + " TEXT NOT NULL,"
			+ COLUMN_DATE + " TEXT NOT NULL,"
			+ "CONSTRAINT bookmark_pkey PRIMARY KEY (" + COLUMN_USER_ID + "," + COLUMN_BOOK_ID + "," + COLUMN_BOOKMARK_PAGE +")"
			+ ");";
	public static final String SQL_WHERE_PRIMARY = COLUMN_USER_ID + " = ? AND " + COLUMN_BOOK_ID + " = ? AND " + COLUMN_BOOKMARK_PAGE + " = ?";
	public static final String SQL_WHERE_USER_BOOK = COLUMN_USER_ID + " = ? AND " + COLUMN_BOOK_ID + " = ?";

	private final String mUserId;
	private final String mBookId;
	private final String[] mSelectionArgsPrimary;
	private final String[] mSelectionArgsUserBook;

	public BookmarkTable (Context context, String userId, String bookId) {
		super(context);
		mUserId = userId;
		mBookId = bookId;
		mSelectionArgsPrimary = new String[3];
		mSelectionArgsPrimary[0] = mUserId;
		mSelectionArgsPrimary[1] = mBookId;
		mSelectionArgsUserBook = new String[] {mUserId, mBookId};
	}

	public List<AbstractRow> getBookmark() {
		return findAll(SQL_WHERE_USER_BOOK, mSelectionArgsUserBook, null, null, COLUMN_DATE);
	}

	public List<AbstractRow> getBookmark(int pageIndex) {
		mSelectionArgsPrimary[2] = String.valueOf(pageIndex);
		return findAll(SQL_WHERE_PRIMARY, mSelectionArgsPrimary);
	}

	public void insertBookmark(int pageIndex, String label, String date) {
		Row row = new Row();
		row.mUserId = mUserId;
		row.mBookId = mBookId;
		row.mBookmarkPage = pageIndex;
		row.mLabel = label;
		row.mDate = date;
		insert(row);
	}

	public void updateBookmarkLabel(int pageIndex, String label) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_LABEL, label);
		mSelectionArgsPrimary[2] = String.valueOf(pageIndex);
		update(values, SQL_WHERE_PRIMARY, mSelectionArgsPrimary);
	}

	public void deleteBookmark(int pageIndex) {
		mSelectionArgsPrimary[2] = String.valueOf(pageIndex);
		delete(SQL_WHERE_PRIMARY, mSelectionArgsPrimary);
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
		private int mBookmarkPage;
		private String mLabel;
		private String mDate;

		public static Row build(Cursor cursor) {
			Row row = new Row();
			row.mUserId = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID));
			row.mBookId = cursor.getString(cursor.getColumnIndex(COLUMN_BOOK_ID));
			row.mBookmarkPage = cursor.getInt(cursor.getColumnIndex(COLUMN_BOOKMARK_PAGE));
			row.mLabel = cursor.getString(cursor.getColumnIndex(COLUMN_LABEL));
			row.mDate = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
			return row;
		}

		@Override
		public ContentValues toContentValues() {
			ContentValues values = new ContentValues();
			values.put(COLUMN_USER_ID, mUserId);
			values.put(COLUMN_BOOK_ID, mBookId);
			values.put(COLUMN_BOOKMARK_PAGE, mBookmarkPage);
			values.put(COLUMN_LABEL, mLabel);
			values.put(COLUMN_DATE, mDate);
			return values;
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

		public String getLabel() {
			return mLabel;
		}

		public String getDate() {
			return mDate;
		}
	}
}
