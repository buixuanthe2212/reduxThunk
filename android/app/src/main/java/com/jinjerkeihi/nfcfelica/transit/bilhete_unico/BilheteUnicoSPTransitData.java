/*
 * BilheteUnicoSPTransitData.java
 *
 * Copyright 2013 Marcelo Liberato <mliberato@gmail.com>
 * Copyright 2014 Eric Butler <eric@codebutler.com>
 * Copyright 2015 Michael Farrell <micolous+git@gmail.com>
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

package com.jinjerkeihi.nfcfelica.transit.bilhete_unico;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.Spanned;

import com.jinjerkeihi.nfcfelica.card.Card;
import com.jinjerkeihi.nfcfelica.card.UnauthorizedException;
import com.jinjerkeihi.nfcfelica.card.classic.ClassicCard;
import com.jinjerkeihi.nfcfelica.transit.TransitData;
import com.jinjerkeihi.nfcfelica.transit.TransitIdentity;
import com.jinjerkeihi.nfcfelica.transit.Trip;
import com.jinjerkeihi.nfcfelica.transit.ovc.OVChipCredit;
import com.jinjerkeihi.nfcfelica.util.Utils;


public class BilheteUnicoSPTransitData extends TransitData {

    public static final Parcelable.Creator<BilheteUnicoSPTransitData> CREATOR = new Parcelable.Creator<BilheteUnicoSPTransitData>() {
        public BilheteUnicoSPTransitData createFromParcel(Parcel parcel) {
            return new BilheteUnicoSPTransitData(parcel);
        }

        public BilheteUnicoSPTransitData[] newArray(int size) {
            return new BilheteUnicoSPTransitData[size];
        }
    };
    private static final String NAME = "Bilhete Único";
    // This is a generic Fudan Microelectronics FM11RF08 manufacturer signature.  Not all cards use
    // this, but there's no standard header.
    private static final byte[] MANUFACTURER = {
            (byte) 0x62,
            (byte) 0x63,
            (byte) 0x64,
            (byte) 0x65,
            (byte) 0x66,
            (byte) 0x67,
            (byte) 0x68,
            (byte) 0x69
    };
    private final BilheteUnicoSPCredit mCredit;

    public BilheteUnicoSPTransitData(Parcel parcel) {
        mCredit = parcel.readParcelable(OVChipCredit.class.getClassLoader());
    }

    public BilheteUnicoSPTransitData(ClassicCard card) {
        mCredit = new BilheteUnicoSPCredit(card.getSector(8).getBlock(1).getData());
    }

    public static boolean check(ClassicCard card) {
        try {
            // Try to get the block where the balance is.
            byte[] blockData = card.getSector(8).getBlock(1).getData();
            return true;
        } catch (UnauthorizedException ex) {
            // TODO: implement a better way to handle identifying this card without a key
            return false;
        }
    }

    public static TransitIdentity parseTransitIdentity(Card card) {
        return new TransitIdentity(NAME, null);
    }

    @Override
    public String getCardName() {
        return NAME;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(mCredit, flags);
    }

    @Override
    @Nullable
    public Integer getBalance() {
        if (mCredit == null) {
            return null;
        }
        return mCredit.getCredit();
    }

    @Override
    public Spanned formatCurrencyString(int currency, boolean isBalance) {
        return Utils.formatCurrencyString(currency, isBalance, "BRL");
    }

    @Override
    public String getSerialNumber() {
        return null;
    }

    @Override
    public Trip[] getTrips() {
        return null;
    }

}
