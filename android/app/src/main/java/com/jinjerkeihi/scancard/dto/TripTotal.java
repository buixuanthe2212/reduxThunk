package com.jinjerkeihi.scancard.dto;

import com.jinjerkeihi.nfcfelica.transit.Trip;

import java.util.ArrayList;

/**
 * XUAN_THE on 5/16/2018.
 */

public class TripTotal {

    public ArrayList<Trip> mTrips;

    public void setTrips(ArrayList<Trip> trips) {
        this.mTrips = trips;
    }

    public ArrayList<Trip> getTrips() {
        return mTrips;
    }
}
