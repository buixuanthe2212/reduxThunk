/*
 * OVChipDBUtil.java
 *
 * Copyright 2012 Wilbert Duijvenvoorde <w.a.n.duijvenvoorde@gmail.com>
 * Copyright 2012 Eric Butler <eric@codebutler.com>
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

package com.jinjerkeihi.nfcfelica.transit.ovc;

import android.content.Context;

import com.jinjerkeihi.nfcfelica.util.DBUtil;


public class OVChipDBUtil extends DBUtil {
    public static final String TABLE_NAME = "stations_data";
    public static final String COLUMN_ROW_COMPANY = "company";
    public static final String COLUMN_ROW_OVCID = "ovcid";
    public static final String COLUMN_ROW_NAME = "name";
    public static final String COLUMN_ROW_CITY = "city";
    public static final String COLUMN_ROW_LONGNAME = "longname";
    public static final String COLUMN_ROW_HALTENR = "haltenr";
    public static final String COLUMN_ROW_ZONE = "zone";
    public static final String COLUMN_ROW_LON = "lon";
    public static final String COLUMN_ROW_LAT = "lat";

    public static final String[] COLUMNS_STATIONDATA = {
            COLUMN_ROW_COMPANY,
            COLUMN_ROW_OVCID,
            COLUMN_ROW_NAME,
            COLUMN_ROW_CITY,
            COLUMN_ROW_LONGNAME,
            COLUMN_ROW_HALTENR,
            COLUMN_ROW_ZONE,
            COLUMN_ROW_LON,
            COLUMN_ROW_LAT,
    };

    private static final String DB_NAME = "ovc_stations.db3";

    private static final int VERSION = 2;

    public OVChipDBUtil(Context context) {
        super(context);
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