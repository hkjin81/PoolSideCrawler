package kr.hkjin.poolsidecrawler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import kr.hkjin.poolsidecrawler.PoolSideContract.CarouselContent;
import kr.hkjin.poolsidecrawler.PoolSideContract.GridContent;

public class PoolSideDBHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_CAROUSEL_CONTENT =
            "CREATE TABLE " + CarouselContent.TABLE_NAME + " (" +
                    CarouselContent._ID + " INTEGER PRIMARY KEY," +
                    CarouselContent.COLUMN_NAME_INDEX + INTEGER_TYPE + COMMA_SEP +
                    CarouselContent.COLUMN_NAME_IMAGE_URL + TEXT_TYPE + " )";
    private static final String SQL_DELETE_CAROUSEL_CONTENT =
            "DROP TABLE IF EXISTS " + CarouselContent.TABLE_NAME;

    private static final String SQL_CREATE_GRID_CONTENT =
            "CREATE TABLE " + GridContent.TABLE_NAME + " (" +
                    GridContent._ID + " INTEGER PRIMARY KEY," +
                    GridContent.COLUMN_NAME_INDEX + INTEGER_TYPE + COMMA_SEP +
                    GridContent.COLUMN_NAME_IMAGE_URL + TEXT_TYPE + " )";
    private static final String SQL_DELETE_GRID_CONTENT =
            "DROP TABLE IF EXISTS " + GridContent.TABLE_NAME;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "PoolSide.db";

    public PoolSideDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CAROUSEL_CONTENT);
        db.execSQL(SQL_CREATE_GRID_CONTENT);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_CAROUSEL_CONTENT);
        db.execSQL(SQL_DELETE_GRID_CONTENT);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void dropAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_CAROUSEL_CONTENT);
        db.execSQL(SQL_DELETE_GRID_CONTENT);
    }

}
