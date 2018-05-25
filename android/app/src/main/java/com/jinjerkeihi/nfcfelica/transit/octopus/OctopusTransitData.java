/*
 * OctopusTransitData.java
 *
 * Copyright 2016 Michael Farrell <micolous+git@gmail.com>
 *
 * Portions based on FelicaCard.java from nfcard project
 * Copyright 2013 Sinpo Wei <sinpowei@gmail.com>
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
package com.jinjerkeihi.nfcfelica.transit.octopus;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.Spanned;
import android.util.Log;

import com.jinjerkeihi.R;
import com.jinjerkeihi.felica.lib.FeliCaLib;
import com.jinjerkeihi.nfcfelica.card.felica.FelicaCard;
import com.jinjerkeihi.nfcfelica.card.felica.FelicaService;
import com.jinjerkeihi.nfcfelica.transit.CardInfo;
import com.jinjerkeihi.nfcfelica.transit.TransitData;
import com.jinjerkeihi.nfcfelica.transit.TransitIdentity;
import com.jinjerkeihi.nfcfelica.ui.HeaderListItem;
import com.jinjerkeihi.nfcfelica.ui.ListItem;
import com.jinjerkeihi.nfcfelica.util.TripObfuscator;
import com.jinjerkeihi.nfcfelica.util.Utils;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Reader for Octopus (Hong Kong)
 * https://github.com/micolous/metrodroid/wiki/Octopus
 */
public class OctopusTransitData extends TransitData {
    public static final String OCTOPUS_NAME = "Octopus";
    public static final String SZT_NAME = "Shenzhen Tong";
    public static final String DUAL_NAME = "Hu Tong Xing";
    public static final Parcelable.Creator<OctopusTransitData> CREATOR = new Parcelable.Creator<OctopusTransitData>() {
        @Override
        public OctopusTransitData createFromParcel(Parcel in) {
            return new OctopusTransitData(in);
        }

        @Override
        public OctopusTransitData[] newArray(int size) {
            return new OctopusTransitData[size];
        }
    };
    private static final String TAG = "OctopusTransitData";
    private int mOctopusBalance = 0;
    private int mShenzhenBalance = 0;
    private boolean mHasOctopus = false;
    private boolean mHasShenzhen = false;

    public OctopusTransitData(FelicaCard card) {
        FelicaService service = null;
        try {
            service = card.getSystem(FeliCaLib.SYSTEMCODE_OCTOPUS).getService(FeliCaLib.SERVICE_OCTOPUS);
        } catch (NullPointerException ignored) {
        }

        if (service != null) {
            byte[] metadata = service.getBlocks().get(0).getData();
            mOctopusBalance = Utils.byteArrayToInt(metadata, 0, 4) - 350;
            mHasOctopus = true;
        }

        service = null;
        try {
            service = card.getSystem(FeliCaLib.SYSTEMCODE_SZT).getService(FeliCaLib.SERVICE_SZT);
        } catch (NullPointerException ignored) {
        }

        if (service != null) {
            byte[] metadata = service.getBlocks().get(0).getData();
            mShenzhenBalance = Utils.byteArrayToInt(metadata, 0, 4) - 350;
            mHasShenzhen = true;
        }
    }

    public OctopusTransitData(Parcel parcel) {
        mOctopusBalance = parcel.readInt();
        mShenzhenBalance = parcel.readInt();
        mHasOctopus = parcel.readInt() == 1;
        mHasShenzhen = parcel.readInt() == 1;
    }

    public static boolean check(FelicaCard card) {
        return (card.getSystem(FeliCaLib.SYSTEMCODE_OCTOPUS) != null) || (card.getSystem(FeliCaLib.SYSTEMCODE_SZT) != null);
    }

    public static CardInfo earlyCheck(int[] systemCodes) {
        // OctopusTransitData is special, because it handles two types of cards.  So we can just
        // directly say which cardInfo matches.
        if (ArrayUtils.contains(systemCodes, FeliCaLib.SYSTEMCODE_OCTOPUS))
            return CardInfo.OCTOPUS; // also dual-mode cards.

        if (ArrayUtils.contains(systemCodes, FeliCaLib.SYSTEMCODE_SZT))
            return CardInfo.SZT;

        return null;
    }

    public static TransitIdentity parseTransitIdentity(FelicaCard card) {
        if (card.getSystem(FeliCaLib.SYSTEMCODE_SZT) != null) {
            if (card.getSystem(FeliCaLib.SYSTEMCODE_OCTOPUS) != null) {
                // Dual-mode card.
                return new TransitIdentity(DUAL_NAME, null);
            } else {
                // SZT-only card.
                return new TransitIdentity(SZT_NAME, null);
            }
        } else {
            // Octopus-only card.
            return new TransitIdentity(OCTOPUS_NAME, null);
        }
    }

    @Override
    @Nullable
    public Integer getBalance() {
        if (mHasOctopus) {
            // Octopus balance takes priority 1
            return mOctopusBalance;
        } else if (mHasShenzhen) {
            // Shenzhen Tong balance takes priority 2
            return mShenzhenBalance;
        } else {
            // Unhandled.
            Log.d(TAG, "Unhandled balance, could not find Octopus or SZT");
            return null;
        }
    }

    @Override
    public Spanned formatCurrencyString(int currency, boolean isBalance) {
        return formatCurrencyString(currency, isBalance, !mHasOctopus);
    }

    public Spanned formatCurrencyString(int currency, boolean isBalance, boolean shenzhen) {
        return Utils.formatCurrencyString(currency, isBalance, shenzhen ? "CNY" : "HKD", 10.);
    }

    @Override
    public String getSerialNumber() {
        // TODO: Find out where this is on the card.
        return null;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mOctopusBalance);
        parcel.writeInt(mShenzhenBalance);
        parcel.writeInt(mHasOctopus ? 1 : 0);
        parcel.writeInt(mHasShenzhen ? 1 : 0);
    }

    @Override
    public String getCardName() {
        if (mHasShenzhen) {
            if (mHasOctopus) {
                return DUAL_NAME;
            } else {
                return SZT_NAME;
            }
        } else {
            return OCTOPUS_NAME;
        }
    }

    @Override
    public List<ListItem> getInfo() {
        ArrayList<ListItem> items = new ArrayList<>();

        if (mHasOctopus && mHasShenzhen) {
            // Dual-mode card, show the CNY balance here.
            items.add(new HeaderListItem(R.string.alternate_purse_balances));
            items.add(new ListItem(R.string.octopus_szt,
                    formatCurrencyString(
                            Math.abs(TripObfuscator.maybeObfuscateFare(mShenzhenBalance)),
                            true, true)));

            return items;
        }
        return null;
    }
}
