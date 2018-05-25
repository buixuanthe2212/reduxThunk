/*
 * UltralightPage.java
 *
 * Copyright 2016-2018 Michael Farrell <micolous+git@gmail.com>
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
package com.jinjerkeihi.nfcfelica.card.ultralight;

import com.jinjerkeihi.nfcfelica.xml.Base64String;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;


/**
 * Represents a page of data on a MIFARE Ultralight (4 bytes)
 */
@Root(name = "page")
public class UltralightPage {
    @Attribute(name = "index")
    private int mIndex;
    @Element(name = "data", required = false)
    private Base64String mData;

    public UltralightPage() {
    }

    public UltralightPage(int index, byte[] data) {
        mIndex = index;
        if (data == null) {
            mData = null;
        } else {
            mData = new Base64String(data);
        }
    }

    public static UltralightPage create(int index, byte[] data) {
        return new UltralightPage(index, data);
    }

    public int getIndex() {
        return mIndex;
    }

    public byte[] getData() {
        return mData.getData();
    }


}
