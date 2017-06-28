package kr.hkjin.poolsidecrawler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import kr.hkjin.poolsidecrawler.PoolSideContract.CarouselContent;
import kr.hkjin.poolsidecrawler.PoolSideContract.GridContent;

import static android.content.ContentValues.TAG;

public class PoolSideDataSource {
    private SQLiteDatabase database;
    private PoolSideDBHelper dbHelper;

    public PoolSideDataSource(Context context) {
        dbHelper = new PoolSideDBHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void insertCarouselImageUrl(List<String> imageUrls) throws SQLException {
        for (int i = 0; i < imageUrls.size(); i++) {
            insertCarouselImageUrl(i, imageUrls.get(i));
        }
    }

    public long insertCarouselImageUrl(int index, String imageUrl) throws SQLException {
        ContentValues values = new ContentValues();
        values.put(CarouselContent.COLUMN_NAME_INDEX, index);
        values.put(CarouselContent.COLUMN_NAME_IMAGE_URL, imageUrl);

        long newRowId = database.insertOrThrow(CarouselContent.TABLE_NAME, null, values);

        Log.d(TAG, String.format("Carousel inserted[%d]: %s, id %d", index, imageUrl, newRowId));

        return newRowId;
    }

    public List<String> getCarouselImageUrls() {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                CarouselContent.COLUMN_NAME_INDEX,
                CarouselContent.COLUMN_NAME_IMAGE_URL
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                CarouselContent.COLUMN_NAME_INDEX + " ASC";

        Cursor c = database.query(
                CarouselContent.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        ArrayList<String> imageUrls = new ArrayList<>();
        int columnIndex = c.getColumnIndexOrThrow(CarouselContent.COLUMN_NAME_IMAGE_URL);
        Log.d(TAG, "Read Carousel URLs");
        while (c.moveToNext()) {
            String url = c.getString(columnIndex);
            Log.d(TAG, url);
            imageUrls.add(url);
        }

        c.close();

        return imageUrls;
    }

    public void insertGridImageUrl(List<String> imageUrls) throws SQLException {
        for (int i = 0; i < imageUrls.size(); i++) {
            insertGridImageUrl(i, imageUrls.get(i));
        }
    }

    public long insertGridImageUrl(int index, String imageUrl) throws SQLException {
        ContentValues values = new ContentValues();
        values.put(GridContent.COLUMN_NAME_INDEX, index);
        values.put(GridContent.COLUMN_NAME_IMAGE_URL, imageUrl);

        long newRowId = database.insertOrThrow(GridContent.TABLE_NAME, null, values);

        Log.d(TAG, String.format("Grid inserted[%d]: %s, id %d", index, imageUrl, newRowId));

        return newRowId;
    }

    public List<String> getGridImageUrls() {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                GridContent.COLUMN_NAME_INDEX,
                GridContent.COLUMN_NAME_IMAGE_URL
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = GridContent.COLUMN_NAME_INDEX + " ASC";

        Cursor c = database.query(
                GridContent.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        ArrayList<String> imageUrls = new ArrayList<>();
        int columnIndex = c.getColumnIndexOrThrow(GridContent.COLUMN_NAME_IMAGE_URL);
        Log.d(TAG, "Read Grid URLs");
        while (c.moveToNext()) {
            String url = c.getString(columnIndex);
            Log.d(TAG, url);
            imageUrls.add(url);
        }

        c.close();

        return imageUrls;
    }

    public void clear() {
        dbHelper.dropAll();
        dbHelper.onCreate(database);
    }
}
