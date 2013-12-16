package jp.bpsinc.android.viewer.db;

import jp.bpsinc.android.util.LogUtil;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbConnector {
	/** DBへの接続 */
	private static SQLiteDatabase mDb;
	/** DBへの接続数 */
	private static int mCount;

	public static synchronized SQLiteDatabase getDatabase(Context context) {
		if (mDb == null) {
			mDb = new DbHelper(context).getWritableDatabase();
		}
		mCount++;
		LogUtil.d("Database connection count=%d", mCount);
		return mDb;
	}

	public static synchronized void releaseDatabase() {
		mCount--;
		if (mCount <= 0) {
			mDb.close();
			mDb = null;
		}
		LogUtil.d("Database release connection count=%d", mCount);
	}

	/**
	 * DBに接続する
	 */
	private static class DbHelper extends SQLiteOpenHelper {
		/** データベース名 */
		private static final String DB_NAME = "cho_gazo_viewer";
		/** データベースバージョン */
		private static final int DB_VERSION = 1;

		private DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			LogUtil.v(AutoBookmarkTable.SQL_CREATE);
			db.execSQL(AutoBookmarkTable.SQL_CREATE);
			db.execSQL(BookmarkTable.SQL_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// 過去のバージョンから現在のバージョンまで段階的にDBを更新していく
			for (int i = oldVersion; i < newVersion; i++) {
				switch (oldVersion) {
				case 1:
					break;
				default:
					break;
				}
			}
		}
	}
}
