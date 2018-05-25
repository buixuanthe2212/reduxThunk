/*
 * OpalTransitData.java
 *
 * Copyright 2015-2018 Michael Farrell <micolous+git@gmail.com>
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
package com.jinjerkeihi.nfcfelica.transit.opal;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.text.Spanned;

import com.jinjerkeihi.MainApplication;
import com.jinjerkeihi.R;
import com.jinjerkeihi.nfcfelica.card.Card;
import com.jinjerkeihi.nfcfelica.card.desfire.DesfireCard;
import com.jinjerkeihi.nfcfelica.transit.Subscription;
import com.jinjerkeihi.nfcfelica.transit.TransitData;
import com.jinjerkeihi.nfcfelica.transit.TransitIdentity;
import com.jinjerkeihi.nfcfelica.ui.HeaderListItem;
import com.jinjerkeihi.nfcfelica.ui.ListItem;
import com.jinjerkeihi.nfcfelica.util.TripObfuscator;
import com.jinjerkeihi.nfcfelica.util.Utils;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


/**
 * Transit data type for Opal (Sydney, AU).
 * <p>
 * This uses the publicly-readable file on the card (7) in order to get the data.
 * <p>
 * Documentation of format: https://github.com/micolous/metrodroid/wiki/Opal
 */
@SuppressLint("ParcelCreator")
public class OpalTransitData extends TransitData {
    public static final String NAME = "Opal";
    public static final int APP_ID = 0x314553;
    public static final int FILE_ID = 0x7;

    private static final GregorianCalendar OPAL_EPOCH = new GregorianCalendar(1980, Calendar.JANUARY, 1);
    private static final OpalSubscription OPAL_AUTOMATIC_TOP_UP = new OpalSubscription();
    private int mSerialNumber;
    private int mBalance; // cents
    private int mChecksum;
    private int mWeeklyTrips;
    private boolean mAutoTopup;
    private int mActionType;
    private int mVehicleType;
    private int mMinute;
    private int mDay;
    private int mTransactionNumber;
    private int mLastDigit;


    @SuppressWarnings("UnusedDeclaration")
    public OpalTransitData(Parcel parcel) {
        mSerialNumber = parcel.readInt();
        mBalance = parcel.readInt();
        mChecksum = parcel.readInt();
        mWeeklyTrips = parcel.readInt();
        mAutoTopup = parcel.readByte() == 0x01;
        mActionType = parcel.readInt();
        mVehicleType = parcel.readInt();
        mMinute = parcel.readInt();
        mDay = parcel.readInt();
        mTransactionNumber = parcel.readInt();
        mLastDigit = parcel.readInt();
    }

    public OpalTransitData(Card card) {
        DesfireCard desfireCard = (DesfireCard) card;
        byte[] data = desfireCard.getApplication(APP_ID).getFile(FILE_ID).getData();
        int iRawBalance;

        data = Utils.reverseBuffer(data, 0, 16);

        try {
            mChecksum = Utils.getBitsFromBuffer(data, 0, 16);
            mWeeklyTrips = Utils.getBitsFromBuffer(data, 16, 4);
            mAutoTopup = Utils.getBitsFromBuffer(data, 20, 1) == 0x01;
            mActionType = Utils.getBitsFromBuffer(data, 21, 4);
            mVehicleType = Utils.getBitsFromBuffer(data, 25, 3);
            mMinute = Utils.getBitsFromBuffer(data, 28, 11);
            mDay = Utils.getBitsFromBuffer(data, 39, 15);
            iRawBalance = Utils.getBitsFromBuffer(data, 54, 21);
            mTransactionNumber = Utils.getBitsFromBuffer(data, 75, 16);
            // Skip bit here
            mLastDigit = Utils.getBitsFromBuffer(data, 92, 4);
            mSerialNumber = Utils.getBitsFromBuffer(data, 96, 32);
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing Opal data", ex);
        }

        mBalance = Utils.unsignedToTwoComplement(iRawBalance, 20);
    }

    public static boolean check(Card card) {
        return (card instanceof DesfireCard) && (((DesfireCard) card).getApplication(0x314553) != null);
    }

    public static boolean earlyCheck(int[] appIds) {
        return ArrayUtils.contains(appIds, APP_ID);
    }

    private static String formatSerialNumber(int serialNumber, int lastDigit) {
        return String.format(Locale.ENGLISH, "308522%09d%01d", serialNumber, lastDigit);
    }

    public static TransitIdentity parseTransitIdentity(Card card) {
        DesfireCard desfireCard = (DesfireCard) card;
        byte[] data = desfireCard.getApplication(APP_ID).getFile(FILE_ID).getData();
        data = Utils.reverseBuffer(data, 0, 5);

        int lastDigit = Utils.getBitsFromBuffer(data, 4, 4);
        int serialNumber = Utils.getBitsFromBuffer(data, 8, 32);
        return new TransitIdentity(NAME, formatSerialNumber(serialNumber, lastDigit));
    }

    public static String getVehicleType(int vehicleType) {
        if (OpalData.VEHICLES.containsKey(vehicleType)) {
            return Utils.localizeString(OpalData.VEHICLES.get(vehicleType));
        }
        return Utils.localizeString(R.string.unknown_format, "0x" + Long.toString(vehicleType, 16));
    }

    public static String getActionType(int actionType) {
        if (OpalData.ACTIONS.containsKey(actionType)) {
            return Utils.localizeString(OpalData.ACTIONS.get(actionType));
        }

        return Utils.localizeString(R.string.unknown_format, "0x" + Long.toString(actionType, 16));
    }

    @Override
    public String getCardName() {
        return NAME;
    }

    @Nullable
    @Override
    public Integer getBalance() {
        return mBalance;
    }

    @Override
    public Spanned formatCurrencyString(int currency, boolean isBalance) {
        return Utils.formatCurrencyString(currency, isBalance, "AUD");
    }

    @Override
    public String getSerialNumber() {
        return formatSerialNumber(mSerialNumber, mLastDigit);
    }

    private Calendar getLastTransactionTime() {
        Calendar cLastTransaction = GregorianCalendar.getInstance();
        cLastTransaction.setTimeInMillis(OPAL_EPOCH.getTimeInMillis());
        cLastTransaction.add(Calendar.DATE, mDay);
        cLastTransaction.add(Calendar.MINUTE, mMinute);
        return cLastTransaction;
    }

    @Override
    public List<ListItem> getInfo() {
        ArrayList<ListItem> items = new ArrayList<>();

        items.add(new HeaderListItem(R.string.general));
        items.add(new ListItem(R.string.opal_weekly_trips, Integer.toString(mWeeklyTrips)));
        if (!MainApplication.hideCardNumbers()) {
            items.add(new ListItem(R.string.checksum, Integer.toString(mChecksum)));
        }

        items.add(new HeaderListItem(R.string.last_transaction));
        if (!MainApplication.hideCardNumbers()) {
            items.add(new ListItem(R.string.transaction_sequence, Integer.toString(mTransactionNumber)));
        }
        Calendar cLastTransactionTime = TripObfuscator.maybeObfuscateTS(getLastTransactionTime());
        items.add(new ListItem(R.string.date, Utils.longDateFormat(cLastTransactionTime)));
        items.add(new ListItem(R.string.time, Utils.timeFormat(cLastTransactionTime)));
        items.add(new ListItem(R.string.vehicle_type, getVehicleType(mVehicleType)));
        items.add(new ListItem(R.string.transaction_type, getActionType(mActionType)));

        return items;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(mSerialNumber);
        parcel.writeInt(mBalance);
        parcel.writeInt(mChecksum);
        parcel.writeInt(mWeeklyTrips);
        parcel.writeByte((byte) (mAutoTopup ? 0x01 : 0x00));
        parcel.writeInt(mActionType);
        parcel.writeInt(mVehicleType);
        parcel.writeInt(mMinute);
        parcel.writeInt(mDay);
        parcel.writeInt(mTransactionNumber);
        parcel.writeInt(mLastDigit);
    }

    @Override
    public Subscription[] getSubscriptions() {
        // Opal has no concept of "subscriptions" (travel pass), only automatic top up.
        if (mAutoTopup) {
            return new Subscription[]{OPAL_AUTOMATIC_TOP_UP};
        }
        return new Subscription[]{};
    }

    @Override
    public Uri getOnlineServicesPage() {
        return Uri.parse("https://m.opal.com.au/");
    }
}
