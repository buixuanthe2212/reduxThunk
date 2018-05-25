/*
 * ManlyFastFerryTransitData.java
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
package com.jinjerkeihi.nfcfelica.transit.manly_fast_ferry;

import android.os.Parcel;
import android.os.Parcelable;

import com.jinjerkeihi.nfcfelica.card.classic.ClassicCard;
import com.jinjerkeihi.nfcfelica.transit.TransitIdentity;
import com.jinjerkeihi.nfcfelica.transit.erg.ErgTransitData;
import com.jinjerkeihi.nfcfelica.transit.erg.ErgTrip;
import com.jinjerkeihi.nfcfelica.transit.erg.record.ErgMetadataRecord;
import com.jinjerkeihi.nfcfelica.transit.erg.record.ErgPurseRecord;

import java.util.GregorianCalendar;


/**
 * Transit data type for Manly Fast Ferry Smartcard (Sydney, AU).
 * <p>
 * This transit card is a system made by ERG Group (now Videlli Limited / Vix Technology).
 * <p>
 * Note: This is a distinct private company who run their own ferry service to Manly, separate to
 * Transport for NSW's Manly Ferry service.
 * <p>
 * Documentation of format: https://github.com/micolous/metrodroid/wiki/Manly-Fast-Ferry
 */

public class ManlyFastFerryTransitData extends ErgTransitData {
    public static final String NAME = "Manly Fast Ferry";
    private static final int AGENCY_ID = 0x0227;

    // Parcel
    public static final Parcelable.Creator<ManlyFastFerryTransitData> CREATOR = new Parcelable.Creator<ManlyFastFerryTransitData>() {
        @Override
        public ManlyFastFerryTransitData createFromParcel(Parcel in) {
            return new ManlyFastFerryTransitData(in);
        }

        @Override
        public ManlyFastFerryTransitData[] newArray(int size) {
            return new ManlyFastFerryTransitData[size];
        }
    };

    public ManlyFastFerryTransitData(Parcel parcel) {
        super(parcel);
    }

    public ManlyFastFerryTransitData(ClassicCard card) {
        super(card);
    }

    public static boolean check(ClassicCard card) {
        if (!ErgTransitData.check(card)) {
            return false;
        }

        ErgMetadataRecord metadataRecord = ErgTransitData.getMetadataRecord(card);
        return metadataRecord != null && metadataRecord.getAgency() == AGENCY_ID;
    }

    public static TransitIdentity parseTransitIdentity(ClassicCard card) {
        byte[] file2 = card.getSector(0).getBlock(2).getData();
        ErgMetadataRecord metadata = ErgMetadataRecord.recordFromBytes(file2);
        return new TransitIdentity(NAME, metadata.getCardSerialHex());
    }

    @Override
    protected ErgTrip newTrip(ErgPurseRecord purse, GregorianCalendar epoch) {
        return new ManlyFastFerryTrip(purse, epoch);
    }

    @Override
    public String getCardName() {
        return NAME;
    }

}
