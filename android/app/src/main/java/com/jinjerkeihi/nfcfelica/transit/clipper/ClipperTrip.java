/*
 * ClipperTrip.java
 *
 * Copyright 2011 "an anonymous contributor"
 * Copyright 2011-2014 Eric Butler <eric@codebutler.com>
 *
 * Thanks to:
 * An anonymous contributor for reverse engineering Clipper data and providing
 * most of the code here.
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
package com.jinjerkeihi.nfcfelica.transit.clipper;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.jinjerkeihi.nfcfelica.transit.CompatTrip;
import com.jinjerkeihi.nfcfelica.transit.Station;


public class ClipperTrip extends CompatTrip {
    public static final Parcelable.Creator<ClipperTrip> CREATOR = new Parcelable.Creator<ClipperTrip>() {
        public ClipperTrip createFromParcel(Parcel parcel) {
            return new ClipperTrip(parcel);
        }

        public ClipperTrip[] newArray(int size) {
            return new ClipperTrip[size];
        }
    };
    private final long mTimestamp;
    private final long mExitTimestamp;
    private final int mFare;
    private final int mAgency;
    private final int mFrom;
    private final int mTo;
    private final int mRoute;

    public ClipperTrip(long timestamp, long exitTimestamp, int fare, int agency, int from, int to, int route) {
        mTimestamp = timestamp;
        mExitTimestamp = exitTimestamp;
        mFare = fare;
        mAgency = agency;
        mFrom = from;
        mTo = to;
        mRoute = route;
    }

    ClipperTrip(Parcel parcel) {
        mTimestamp = parcel.readLong();
        mExitTimestamp = parcel.readLong();
        mFare = parcel.readInt();
        mAgency = parcel.readInt();
        mFrom = parcel.readInt();
        mTo = parcel.readInt();
        mRoute = parcel.readInt();
    }

    @Override
    public long getTimestamp() {
        return mTimestamp;
    }

    @Override
    public long getExitTimestamp() {
        return mExitTimestamp;
    }

    @Override
    public String getAgencyName() {
        return ClipperTransitData.getAgencyName((int) mAgency);
    }

    @Override
    public String getShortAgencyName() {
        return ClipperTransitData.getShortAgencyName((int) mAgency);
    }

    @Override
    public String getRouteName() {
        if (mAgency == ClipperData.AGENCY_GG_FERRY) {
            return ClipperData.GG_FERRY_ROUTES.get(mRoute);
        } else {
            // FIXME: Need to find bus route #s
            // return "(Route 0x" + Long.toString(mRoute, 16) + ")";
            return null;
        }
    }

    @Override
    @Nullable
    public Integer getFare() {
        return mFare;
    }

    @Override
    public boolean hasFare() {
        return true;
    }

    @Override
    public Station getStartStation() {
        if (mAgency == ClipperData.AGENCY_BART) {
            if (ClipperData.BART_STATIONS.containsKey(mFrom)) {
                return ClipperData.BART_STATIONS.get(mFrom);
            }
        } else if (mAgency == ClipperData.AGENCY_GG_FERRY) {
            if (ClipperData.GG_FERRY_TERIMINALS.containsKey(mFrom)) {
                return ClipperData.GG_FERRY_TERIMINALS.get(mFrom);
            }
        } else if (mAgency == ClipperData.AGENCY_SF_BAY_FERRY) {
            if (ClipperData.SF_BAY_FERRY_TERMINALS.containsKey(mFrom)) {
                return ClipperData.SF_BAY_FERRY_TERMINALS.get(mFrom);
            }
        }
        return null;
    }

    @Override
    public Station getEndStation() {
        if (mAgency == ClipperData.AGENCY_BART) {
            if (ClipperData.BART_STATIONS.containsKey(mTo)) {
                return ClipperData.BART_STATIONS.get(mTo);
            }
        } else if (mAgency == ClipperData.AGENCY_GG_FERRY) {
            if (ClipperData.GG_FERRY_TERIMINALS.containsKey(mTo)) {
                return ClipperData.GG_FERRY_TERIMINALS.get(mTo);
            }
        } else if (mAgency == ClipperData.AGENCY_SF_BAY_FERRY) {
            if (ClipperData.SF_BAY_FERRY_TERMINALS.containsKey(mTo)) {
                return ClipperData.SF_BAY_FERRY_TERMINALS.get(mTo);
            }
        }
        return null;
    }

    @Override
    public String getStartStationName() {
        if (mAgency == ClipperData.AGENCY_BART || mAgency == ClipperData.AGENCY_GG_FERRY || mAgency == ClipperData.AGENCY_SF_BAY_FERRY) {
            Station station = getStartStation();
            if (station != null)
                return station.getShortStationName();
            else
                return "Station #0x" + Long.toString(mFrom, 16);
        } else if (mAgency == ClipperData.AGENCY_MUNI) {
            return null; // Coach number is not collected
        } else if (mAgency == ClipperData.AGENCY_GGT || mAgency == ClipperData.AGENCY_CALTRAIN) {
            return "Zone #" + mFrom;
        } else {
            return "(Unknown Station)";
        }
    }

    @Override
    public String getEndStationName() {
        if (mAgency == ClipperData.AGENCY_BART || mAgency == ClipperData.AGENCY_GG_FERRY || mAgency == ClipperData.AGENCY_SF_BAY_FERRY) {
            Station station = getEndStation();
            if (station != null) {
                return station.getShortStationName();
            } else {
                return "Station #0x" + Long.toString(mTo, 16);
            }
        } else if (mAgency == ClipperData.AGENCY_MUNI) {
            return null; // Coach number is not collected
        } else if (mAgency == ClipperData.AGENCY_GGT || mAgency == ClipperData.AGENCY_CALTRAIN) {
            if (mTo == 0xffff)
                return "(End of line)";
            return "Zone #0x" + Long.toString(mTo, 16);
        } else {
            return "(Unknown Station)";
        }
    }

    @Override
    public Mode getMode() {
        if (mAgency == ClipperData.AGENCY_ACTRAN)
            return Mode.BUS;
        if (mAgency == ClipperData.AGENCY_BART)
            return Mode.METRO;
        if (mAgency == ClipperData.AGENCY_CALTRAIN)
            return Mode.TRAIN;
        if (mAgency == ClipperData.AGENCY_GGT)
            return Mode.BUS;
        if (mAgency == ClipperData.AGENCY_SAMTRANS)
            return Mode.BUS;
        if (mAgency == ClipperData.AGENCY_VTA)
            return Mode.BUS; // FIXME: or Mode.TRAM for light rail
        if (mAgency == ClipperData.AGENCY_MUNI)
            return Mode.BUS; // FIXME: or Mode.TRAM for "Muni Metro"
        if (mAgency == ClipperData.AGENCY_GG_FERRY)
            return Mode.FERRY;
        if (mAgency == ClipperData.AGENCY_SF_BAY_FERRY)
            return Mode.FERRY;
        return Mode.OTHER;
    }

    @Override
    public boolean hasTime() {
        return true;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(mTimestamp);
        parcel.writeLong(mExitTimestamp);
        parcel.writeInt(mFare);
        parcel.writeInt(mAgency);
        parcel.writeInt(mFrom);
        parcel.writeInt(mTo);
        parcel.writeInt(mRoute);
    }

    public int describeContents() {
        return 0;
    }
}
