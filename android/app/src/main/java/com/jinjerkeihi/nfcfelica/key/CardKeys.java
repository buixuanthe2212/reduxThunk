/*
 * CardKeys.java
 *
 * Copyright (C) 2012 Eric Butler
 *
 * Authors:
 * Eric Butler <eric@codebutler.com>
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

package com.jinjerkeihi.nfcfelica.key;

import android.database.Cursor;
import android.net.Uri;

import com.jinjerkeihi.MainApplication;
import com.jinjerkeihi.nfcfelica.provider.CardKeyProvider;
import com.jinjerkeihi.nfcfelica.provider.KeysTableColumns;
import com.jinjerkeihi.nfcfelica.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;


public abstract class CardKeys {
    public static CardKeys forTagId(byte[] tagId) throws Exception {
        String tagIdString = Utils.getHexString(tagId);
        MainApplication app = MainApplication.getInstance();
        Cursor cursor = app.getContentResolver().query(Uri.withAppendedPath(CardKeyProvider.CONTENT_URI, tagIdString), null, null, null, null);
        if (cursor.moveToFirst()) {
            return CardKeys.fromCursor(cursor);
        } else {
            return null;
        }
    }

    private static CardKeys fromCursor(Cursor cursor) throws JSONException {
        String cardType = cursor.getString(cursor.getColumnIndex(KeysTableColumns.CARD_TYPE));
        String keyData = cursor.getString(cursor.getColumnIndex(KeysTableColumns.KEY_DATA));

        JSONObject keyJSON = new JSONObject(keyData);

        if (cardType.equals("MifareClassic")) {
            return ClassicCardKeys.fromJSON(keyJSON);
        }

        throw new IllegalArgumentException("Unknown card type for key: " + cardType);
    }

    public abstract JSONObject toJSON() throws JSONException;
}
