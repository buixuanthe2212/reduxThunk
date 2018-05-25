/*
 * ErgTransitData.java
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
package com.jinjerkeihi.nfcfelica.transit.erg;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.Spanned;

import com.jinjerkeihi.R;
import com.jinjerkeihi.nfcfelica.card.classic.ClassicBlock;
import com.jinjerkeihi.nfcfelica.card.classic.ClassicCard;
import com.jinjerkeihi.nfcfelica.card.classic.ClassicSector;
import com.jinjerkeihi.nfcfelica.transit.TransitData;
import com.jinjerkeihi.nfcfelica.transit.TransitIdentity;
import com.jinjerkeihi.nfcfelica.transit.Trip;
import com.jinjerkeihi.nfcfelica.transit.erg.record.ErgBalanceRecord;
import com.jinjerkeihi.nfcfelica.transit.erg.record.ErgMetadataRecord;
import com.jinjerkeihi.nfcfelica.transit.erg.record.ErgPurseRecord;
import com.jinjerkeihi.nfcfelica.transit.erg.record.ErgRecord;
import com.jinjerkeihi.nfcfelica.transit.manly_fast_ferry.ManlyFastFerryTrip;
import com.jinjerkeihi.nfcfelica.ui.HeaderListItem;
import com.jinjerkeihi.nfcfelica.ui.ListItem;
import com.jinjerkeihi.nfcfelica.util.TripObfuscator;
import com.jinjerkeihi.nfcfelica.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Transit data type for ERG/Videlli/Vix MIFARE Classic cards.
 *
 * Wiki: https://github.com/micolous/metrodroid/wiki/ERG-MFC
 */
public class ErgTransitData extends TransitData {
    public static final String NAME = "ERG";
    public static final byte[] SIGNATURE = {
            0x32, 0x32, 0x00, 0x00, 0x00, 0x01, 0x01
    };
    private String mSerialNumber;
    private GregorianCalendar mEpochDate;
    private int mAgencyID;
    private int mBalance;
    private Trip[] mTrips;

    // Parcel
    public static final Parcelable.Creator<ErgTransitData> CREATOR = new Parcelable.Creator<ErgTransitData>() {
        @Override
        public ErgTransitData createFromParcel(Parcel in) {
            return new ErgTransitData(in);
        }

        @Override
        public ErgTransitData[] newArray(int size) {
            return new ErgTransitData[size];
        }
    };

    @SuppressWarnings("UnusedDeclaration")
    public ErgTransitData(Parcel parcel) {
        mSerialNumber = parcel.readString();
        mEpochDate = new GregorianCalendar();
        mEpochDate.setTimeInMillis(parcel.readLong());
        mTrips = parcel.createTypedArray(ManlyFastFerryTrip.CREATOR);
    }

    // Decoder
    public ErgTransitData(ClassicCard card) {
        ArrayList<ErgRecord> records = new ArrayList<>();

        // Iterate through blocks on the card and deserialize all the binary data.
        for (ClassicSector sector : card.getSectors()) {
            for (ClassicBlock block : sector.getBlocks()) {
                if (sector.getIndex() == 0 && block.getIndex() == 0) {
                    continue;
                }

                if (block.getIndex() == 3) {
                    continue;
                }

                ErgRecord record = ErgRecord.recordFromBytes(block.getData(), sector.getIndex(), block.getIndex());

                if (record != null) {
                    records.add(record);
                }
            }
        }

        // Now do a first pass for metadata and balance information.
        ArrayList<ErgBalanceRecord> balances = new ArrayList<>();

        for (ErgRecord record : records) {
            if (record instanceof ErgMetadataRecord) {
                ErgMetadataRecord m = (ErgMetadataRecord)record;
                mSerialNumber = formatSerialNumber(m);
                mEpochDate = m.getEpochDate();
                mAgencyID = m.getAgency();
            } else if (record instanceof ErgBalanceRecord) {
                balances.add((ErgBalanceRecord) record);
            }
        }

        if (balances.size() >= 1) {
            Collections.sort(balances);
            mBalance = balances.get(0).getBalance();
        }

        // Now generate a transaction list.  This has a 1:1 mapping with trips (there is no
        // "tap off").
        //
        // These need the Epoch to be known first.
        ArrayList<Trip> trips = new ArrayList<>();

        for (ErgRecord record : records) {
            if (record instanceof ErgPurseRecord) {
                ErgPurseRecord purseRecord = (ErgPurseRecord) record;
                trips.add(newTrip(purseRecord, mEpochDate));
            }
        }

        Collections.sort(trips, new Trip.Comparator());
        mTrips = trips.toArray(new Trip[trips.size()]);
    }

    /**
     * ERG cards have two identifying marks:
     *
     * 1. A signature in Sector 0, Block 1
     * 2. The agency ID in Sector 0, Block 2 (Readable with ErgMetadataRecord)
     *
     * This check only determines if there is a signature -- subclasses should call this and then
     * perform their own check of the agency ID.
     * @param card MIFARE Classic card data.
     * @return True if this is an ERG card, false otherwise.
     */
    public static boolean check(ClassicCard card) {
        byte[] file1;

        try {
            file1 = card.getSector(0).getBlock(1).getData();
        } catch (Exception ex) {
            // These blocks of the card are not protected.
            // This must not be a ERG smartcard.
            return false;
        }

        // Check for signature
        return Arrays.equals(Arrays.copyOfRange(file1, 0, SIGNATURE.length), SIGNATURE);
    }

    protected static ErgMetadataRecord getMetadataRecord(ClassicCard card) {
        byte[] file2;
        try {
            file2 = card.getSector(0).getBlock(2).getData();
        } catch (Exception ex) {
            // Can't be for us...
            return null;
        }

        return ErgMetadataRecord.recordFromBytes(file2);
    }

    public static TransitIdentity parseTransitIdentity(ClassicCard card) {
        ErgMetadataRecord metadata = getMetadataRecord(card);
        if (metadata == null) {
            return null;
        }
        return new TransitIdentity(NAME, metadata.getCardSerialHex());
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mSerialNumber);
        parcel.writeLong(mEpochDate.getTimeInMillis());
        parcel.writeTypedArray(mTrips, flags);
    }

    @Override
    @Nullable
    public Integer getBalance() {
        return mBalance;
    }

    @Override
    public Spanned formatCurrencyString(int currency, boolean isBalance) {
        // This defaults to AUD (because ERG was an Australian company). Cards with other currencies
        // should override this appropriately.
        return Utils.formatCurrencyString(currency, isBalance, "AUD");
    }

    // Structures
    @Override
    public String getSerialNumber() {
        return mSerialNumber;
    }

    @Override
    public Trip[] getTrips() {
        return mTrips;
    }

    @Override
    public List<ListItem> getInfo() {
        ArrayList<ListItem> items = new ArrayList<>();
        items.add(new HeaderListItem(R.string.general));
        items.add(new ListItem(R.string.card_epoch,
                Utils.longDateFormat(TripObfuscator.maybeObfuscateTS(mEpochDate))));
        items.add(new ListItem(R.string.erg_agency_id,
                "0x" + Long.toHexString(mAgencyID)));
        return items;
    }

    @Override
    public String getCardName() {
        return NAME;
    }

    /**
     * Allows you to override the constructor for new trips, to hook in your own station ID code.
     *
     * @return Subclass of ErgTrip.
     */
    protected ErgTrip newTrip(ErgPurseRecord purse, GregorianCalendar epoch) {
        return new ErgTrip(purse, epoch);
    }

    /**
     * Some cards format the serial number in decimal rather than hex. By default, this uses hex.
     *
     * This can be overridden in subclasses to format the serial number correctly.
     * @param metadataRecord Metadata record for this card.
     * @return Formatted serial number, as string.
     */
    protected String formatSerialNumber(ErgMetadataRecord metadataRecord) {
        return metadataRecord.getCardSerialHex();
    }

}
