package jp.bpsinc.android.viewer.db;

import java.util.ArrayList;
import java.util.List;

import jp.bpsinc.android.util.LogUtil;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class AbstractTable {
	public static final int INVALID_INTEGER = -1;

	private SQLiteDatabase mDb;

	public AbstractTable(Context context) {
		mDb = DbConnector.getDatabase(context);
	}

	@Override
	public void finalize() {
		DbConnector.releaseDatabase();
	}

	protected List<AbstractRow> findAll(String selection, String[] selectionArgs) {
		return findAll(selection, selectionArgs, null, null, null);
	}

	protected List<AbstractRow> findAll(String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
		Cursor cursor = null;
		try {
			ArrayList<AbstractRow> list = new ArrayList<AbstractRow>();
			cursor = mDb.query(getTableName(), null, selection, selectionArgs, groupBy, having, orderBy);
			if (cursor.moveToFirst()) {
				do {
					list.add(buildRow(cursor));
				} while (cursor.moveToNext());
            }
			return list;
		} catch (Exception e) {
			LogUtil.e(e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	protected long insert(AbstractRow row) {
		if (row == null) {
			return INVALID_INTEGER;
		}
		long ret;
		mDb.beginTransaction();
		ret = mDb.insert(getTableName(), null, row.toContentValues());
		mDb.setTransactionSuccessful();
		mDb.endTransaction();
		return ret;
	}

	protected int update(ContentValues values, String whereClause, String[] whereArgs) {
		int ret;
		mDb.beginTransaction();
		ret = mDb.update(getTableName(), values, whereClause, whereArgs);
		mDb.setTransactionSuccessful();
		mDb.endTransaction();
		return ret;
	}

	protected int delete(String whereClause, String[] whereArgs) {
		int ret;
		mDb.beginTransaction();
		ret = mDb.delete(getTableName(), whereClause, whereArgs);
		mDb.setTransactionSuccessful();
		mDb.endTransaction();
		return ret;
	}

	protected long replace(AbstractRow row) {
		if (row == null) {
			return INVALID_INTEGER;
		}
		long ret;
		mDb.beginTransaction();
		ret = mDb.replace(getTableName(), null, row.toContentValues());
		mDb.setTransactionSuccessful();
		mDb.endTransaction();
		return ret;
	}

	protected abstract String getTableName();
	protected abstract AbstractRow buildRow(Cursor cursor);
}
