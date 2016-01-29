
package com.raenarapps.easyweather.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.raenarapps.easyweather.data.WeatherContract.LocationEntry;
import com.raenarapps.easyweather.data.WeatherContract.WeatherEntry;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();


    void deleteTheDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        c = db.rawQuery("PRAGMA table_info(" + LocationEntry.TABLE_NAME + ")"
                ,
                null);
        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(LocationEntry._ID);
        locationColumnHashSet.add(LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(LocationEntry.LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    public void testLocationTable() {
        insertNorthPoleLocationValues(mContext);
    }

    static long insertNorthPoleLocationValues(Context context) {
        WeatherDbHelper dbhelper = new WeatherDbHelper(context);
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        ContentValues cv = TestUtilities.createNorthPoleLocationValues();
        long id = db.insert(LocationEntry.TABLE_NAME, null, cv);
        assertTrue("location row was not inserted", id != -1);

        Cursor c = db.query(LocationEntry.TABLE_NAME, null, LocationEntry._ID + "== ?", new String[]{Long.toString(id)}, null, null, null);
        assertTrue("No rows returned in query", c.moveToFirst());

        String error = LocationEntry.TABLE_NAME;
        TestUtilities.validateCursor(error, c, cv);
        c.close();
        db.close();
        return id;
    }

    public void testWeatherTable() {

        WeatherDbHelper dbhelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        long locationID = insertNorthPoleLocationValues(mContext);

        ContentValues cv = TestUtilities.createWeatherValues(locationID);
        long weatherID = db.insert(WeatherEntry.TABLE_NAME, null, cv);
        assertTrue("weather row was not inserted", weatherID != -1);

        Cursor c = db.query(WeatherEntry.TABLE_NAME, null, WeatherEntry._ID + " =?",
                new String[]{Long.toString(weatherID)}, null, null, null);
        assertTrue("No rows returned in query", c.moveToFirst());

        String error = WeatherEntry.TABLE_NAME;
        TestUtilities.validateCurrentRecord(error, c, cv);
        c.close();
        db.close();
    }

}
