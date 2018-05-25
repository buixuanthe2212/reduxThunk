/*
 * SeqGoDBUtil.java
 *
 * Copyright 2015-2016 Michael Farrell <micolous+git@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jinjerkeihi.nfcfelica.transit.seq_go;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.jinjerkeihi.MainApplication;
import com.jinjerkeihi.nfcfelica.util.DBUtil;

import java.io.IOException;


/**
 * Database functionality for SEQ Go Cards
 */
public class SeqGoDBUtil extends DBUtil {
    public static final String TABLE_NAME = "stops";
    public static final String COLUMN_ROW_ID = "id";
    public static final String COLUMN_ROW_NAME = "name";

    public static final String COLUMN_ROW_LON = "x";
    public static final String COLUMN_ROW_LAT = "y";
    public static final String[] COLUMNS_STATIONDATA = {
            COLUMN_ROW_ID,
            COLUMN_ROW_NAME,
            COLUMN_ROW_LON,
            COLUMN_ROW_LAT,
    };
    private static final String TAG = "SeqGoDBUtil";
    private static final String DB_NAME = "seq_go_stations.db3";

    private static final int VERSION = 4268;

    public SeqGoDBUtil(Context context) {
        super(context);
    }

    private static SeqGoDBUtil getDB() {
        return MainApplication.getInstance().getSeqGoDBUtil();
    }

    public static SeqGoStation getStation(int stationId) {
        if (stationId == 0) {
            return null;
        }

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            try {
                db = getDB().openDatabase();
            } catch (IOException ex) {
                Log.e(TAG, "Error connecting database", ex);
                return null;
            }

            cursor = db.query(
                    SeqGoDBUtil.TABLE_NAME,
                    SeqGoDBUtil.COLUMNS_STATIONDATA,
                    String.format("%s = ?", SeqGoDBUtil.COLUMN_ROW_ID),
                    new String[]{
                            String.valueOf(stationId),
                    },
                    null,
                    null,
                    SeqGoDBUtil.COLUMN_ROW_ID);

            if (!cursor.moveToFirst()) {
                Log.w(TAG, String.format("FAILED get station %s",
                        stationId));

                return null;
            }

            return new SeqGoStation(cursor);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    protected String getDBName() {
        return DB_NAME;
    }

    @Override
    protected int getDesiredVersion() {
        return VERSION;
    }
}
