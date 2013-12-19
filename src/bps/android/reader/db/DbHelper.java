
package bps.android.reader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "epub_book.db";

    private static final String TABLE_NAME = "book";

    private static final int DATABASE_VERSION = 1;

    private static final String COLUMN_ID = "book_id";

    private static final String COLUMN_NAME = "book_name";

    private static final String COLUMN_AUTHOR = "book_author";

    private static final String COLUMN_PUBLISHER = "book_publisher";

    private static final String COLUMN_PTIME = "book_ptime";

    private static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID
            + " INTEGER PRIMARY KEY, " + COLUMN_NAME + "  TEXT, " + COLUMN_AUTHOR + " TEXT, "
            + COLUMN_PUBLISHER + " TEXT, " + COLUMN_PTIME + " TEXT);";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public Cursor select() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        return cursor;
    }

    public long insert(String bookname, String bookauthor, String bookpublisher, String bookptime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, bookname);
        cv.put(COLUMN_AUTHOR, bookauthor);
        cv.put(COLUMN_PUBLISHER, bookpublisher);
        cv.put(COLUMN_PTIME, bookptime);
        long row = db.insert(TABLE_NAME, null, cv);
        return row;
    }
    
    public void delete(int id){ 
        SQLiteDatabase db = this.getWritableDatabase();
        String where = COLUMN_ID + " =? ";
        String[] whereArgs = {Integer.toString(id)};
        db.delete(TABLE_NAME, where, whereArgs);
    }
    
    public void update(int id, String bookname, String bookauthor, String bookpublisher, String bookptime){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, bookname);
        cv.put(COLUMN_AUTHOR, bookauthor);
        cv.put(COLUMN_PUBLISHER, bookpublisher);
        cv.put(COLUMN_PTIME, bookptime);
        
        String where = COLUMN_ID + " =? ";
        String[] whereArgs = {Integer.toString(id)};
        db.update(TABLE_NAME, cv, where, whereArgs);
    }

}
