package com.chriscooke.withinawalk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Chris_Home on 01/12/15.
 */
public class DBhandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MyDB";


    public DBhandler(Context context){
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOCO_TABLE = "CREATE TABLE loco ( " +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, "+
                "address TEXT, "+
                "phone_num TEXT, "+
                "lat REAL, "+
                "lng REAL )";

        db.execSQL(CREATE_LOCO_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS loco");

        // create fresh loco table
        this.onCreate(db);
    }



        //table names
        public static final String TABLE_NAME = "loco";

        //column names
        public static final String KEY_ENTRY_ID = "ID";
        public static final String KEY_PLACE_NAME = "name";
        public static final String KEY_PLACE_ADDRESS = "address";
        public static final String KEY_PLACE_NUM = "phone_num";
        public static final String KEY_LAT = "lat";
        public static final String KEY_LNG = "lng";

        public final String [] COLUMNS = {KEY_ENTRY_ID,KEY_PLACE_NAME,KEY_PLACE_ADDRESS,KEY_PLACE_NUM,KEY_LAT,KEY_LNG};


        public void addloco(loco loco){
            Log.d("addloco", loco.toString());
            // 1. get reference to writable DB
            SQLiteDatabase db = this.getWritableDatabase();

            // 2. create ContentValues to add key "column"/value
            ContentValues values = new ContentValues();
            values.put(KEY_PLACE_NAME, loco.getName()); // get title
            values.put(KEY_PLACE_ADDRESS, loco.getAddress()); // get author
            values.put(KEY_PLACE_NUM, loco.getPhoneNum());
            values.put(KEY_LAT, loco.getLat());
            values.put(KEY_LNG, loco.getLng());
            // 3. insert
            db.insert(TABLE_NAME, // table
                    null, //nullColumnHack
                    values); // key/value -> keys = column names/ values = column values

            // 4. close
            db.close();
        }

        public loco getloco(int id){

            // 1. get reference to readable DB
            SQLiteDatabase db = this.getReadableDatabase();

            // 2. build query
            Cursor cursor =
                    db.query(TABLE_NAME, // a. table
                            COLUMNS, // b. column names
                            " id = ?", // c. selections
                            new String[] { String.valueOf(id) }, // d. selections args
                            null, // e. group by
                            null, // f. having
                            null, // g. order by
                            null); // h. limit

            // 3. if we got results get the first one
            if (cursor != null)
                cursor.moveToFirst();

            // 4. build loco object
            loco loco = new loco();
            loco.setItemID(Integer.parseInt(cursor.getString(0)));
            loco.setName(cursor.getString(1));
            loco.setAddress(cursor.getString(2));
            loco.setPhoneNum(cursor.getString(3));
            loco.setLat(cursor.getDouble(4));
            loco.setLng(cursor.getDouble(5));
            Log.d("getloco(" + id + ")", loco.toString());

            // 5. return loco
            return loco;
        }

        // Get All locos
        public List<loco> getAlllocos() {
            List<loco> locos = new LinkedList<loco>();

            // 1. build the query
            String query = "SELECT  * FROM " + TABLE_NAME;

            // 2. get reference to writable DB
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(query, null);

            // 3. go over each row, build loco and add it to list
            loco loco = null;
            if (cursor.moveToFirst()) {
                do {
                    loco = new loco();
                    loco.setItemID(Integer.parseInt(cursor.getString(0)));
                    loco.setName(cursor.getString(1));
                    loco.setAddress(cursor.getString(2));
                    loco.setPhoneNum(cursor.getString(3));
                    loco.setLat(cursor.getDouble(4));
                    loco.setLng(cursor.getDouble(5));

                    // Add loco to locos
                    locos.add(loco);
                } while (cursor.moveToNext());
            }

            Log.d("getAlllocos()", locos.toString());

            // return locos
            return locos;
        }

        // Updating single loco
        public int updateloco(loco loco) {

            // 1. get reference to writable DB
            SQLiteDatabase db = this.getWritableDatabase();

            // 2. create ContentValues to add key "column"/value
            ContentValues values = new ContentValues();
            values.put("name", loco.getName());
            values.put("address", loco.getAddress());
            values.put("phone_num", loco.getPhoneNum());
            values.put("lat", loco.getLat());
            values.put("lng", loco.getLng());

            // 3. updating row
            int i = db.update(TABLE_NAME, //table
                    values, // column/value
                    KEY_ENTRY_ID+" = ?", // selections
                    new String[] { String.valueOf(loco.getItemID()) }); //selection args

            // 4. close
            db.close();

            return i;

        }

        // Deleting single loco
        public void deleteloco(loco loco) {

            // 1. get reference to writable DB
            SQLiteDatabase db = this.getWritableDatabase();

            // 2. delete
            db.delete(TABLE_NAME,
                    KEY_ENTRY_ID+" = ?",
                    new String[] { String.valueOf(loco.getItemID()) });

            // 3. close
            db.close();

            Log.d("deleteloco", loco.toString());

        }

    void deleteAll()
    {
        SQLiteDatabase db= this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);

    }
    }







