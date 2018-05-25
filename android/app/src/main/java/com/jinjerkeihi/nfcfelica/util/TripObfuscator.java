package com.jinjerkeihi.nfcfelica.util;

import android.util.Log;

import com.jinjerkeihi.MainApplication;
import com.jinjerkeihi.nfcfelica.transit.Trip;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Obfuscates trip dates
 */

public final class TripObfuscator {
    private static final String TAG = "TripObfuscator";
    private static final GregorianCalendar UNIX_EPOCH = new GregorianCalendar(1970, Calendar.JANUARY, 1);

    private static final TripObfuscator singleton = new TripObfuscator();

    private static final SecureRandom mRNG = new SecureRandom();

    /**
     * Remaps days of the year to a different day of the year.
     */
    private static List<Integer> mCalendarMapping = new ArrayList<>();

    static {
        // Generate list of ints from 0-366 (each day in year).
        for (int x = 0; x < 366; x++) {
            mCalendarMapping.add(x);
        }

        Collections.shuffle(mCalendarMapping);
    }

    /**
     * Maybe obfuscates a timestamp
     *
     * @param input          Calendar representing the time to obfuscate
     * @param obfuscateDates true if dates should be obfuscated
     * @param obfuscateTimes true if times should be obfuscated
     * @return maybe obfuscated value
     */
    public static Calendar maybeObfuscateTS(Calendar input, boolean obfuscateDates, boolean obfuscateTimes) {
        if (!obfuscateDates && !obfuscateTimes) {
            return input;
        }

        if (input == null) {
            return null;
        }

        int today = GregorianCalendar.getInstance().get(Calendar.DAY_OF_YEAR);

        // Clone the input before we start messing with it.
        Calendar newDate = GregorianCalendar.getInstance();
        newDate.setTimeInMillis(input.getTimeInMillis());

        if (obfuscateDates) {
            int dayOfYear = newDate.get(Calendar.DAY_OF_YEAR);
            if (dayOfYear < mCalendarMapping.size()) {
                dayOfYear = mCalendarMapping.get(dayOfYear);
            } else {
                // Shouldn't happen...
                Log.w(TAG, String.format("Oops, got out of range day-of-year (%d)", dayOfYear));
            }

            newDate.set(Calendar.DAY_OF_YEAR, dayOfYear);

            // Adjust for the time of year
            if (dayOfYear >= today) {
                newDate.add(Calendar.YEAR, -1);
            }
        }

        if (obfuscateTimes) {
            // Reduce resolution of timestamps to 5 minutes.
            newDate.setTimeInMillis((newDate.getTimeInMillis() / 300000) * 300000);

            // Add a deviation of up to 20,000 seconds (5.5 hours) earlier or later.
            newDate.add(Calendar.SECOND, mRNG.nextInt(40000) - 20000);
        }

        return newDate;
    }

    /**
     * Maybe obfuscates a timestamp
     *
     * @param input          seconds since UNIX epoch (1970-01-01)
     * @param obfuscateDates true if dates should be obfuscated
     * @param obfuscateTimes true if times should be obfuscated
     * @return maybe obfuscated value
     */
    @Deprecated
    public static long maybeObfuscateTS(long input, boolean obfuscateDates, boolean obfuscateTimes) {
        if (!obfuscateDates && !obfuscateTimes) {
            return input;
        }

        Calendar s = GregorianCalendar.getInstance();
        s.setTimeInMillis(UNIX_EPOCH.getTimeInMillis() + (input * 1000));

        return maybeObfuscateTS(s, obfuscateDates, obfuscateTimes).getTimeInMillis() / 1000;
    }

    @Deprecated
    public static long maybeObfuscateTS(long input) {
        return maybeObfuscateTS(input, MainApplication.obfuscateTripDates(),
                MainApplication.obfuscateTripTimes());
    }

    public static Calendar maybeObfuscateTS(Calendar input) {
        return maybeObfuscateTS(input, MainApplication.obfuscateTripDates(),
                MainApplication.obfuscateTripTimes());
    }


    public static TripObfuscator getInstance() {
        return singleton;
    }

    /**
     * Obfuscates a fare-like number, if "obfuscate fares" is enabled. This is useful for if your
     * TransitData implementation shows additional transactional information in getInfo(). This
     * isn't required for either balances, trips or refills to use this, as they are wrapped
     * automatically.
     *
     * @param fare input fare
     * @return adjusted fare
     */
    public static int maybeObfuscateFare(int fare) {
        if (!MainApplication.obfuscateTripFares()) {
            return fare;
        }

        int newFare = (int) ((fare + mRNG.nextInt(100) - 50) * ((mRNG.nextDouble() * 0.4) + 0.8));

        if ((fare >= 0 && newFare < 0) || (fare < 0 && newFare > 0)) {
            newFare *= -1;
        }

        return newFare;
    }

    public static List<Trip> obfuscateTrips(List<Trip> trips, boolean obfuscateDates, boolean obfuscateTimes, boolean obfuscateFares) {
        List<Trip> newTrips = new ArrayList<>();
        for (Trip trip : trips) {
            Calendar start = trip.getStartTimestamp();
            long timeDelta = 0;
            int fareOffset = 0;
            double fareMultiplier = 1.0;

            if (start != null) {
                timeDelta = maybeObfuscateTS(start, obfuscateDates, obfuscateTimes).getTimeInMillis() - start.getTimeInMillis();
            } else {
                timeDelta = 0;
            }

            if (obfuscateFares) {
                // These are unique for each fare
                fareOffset = mRNG.nextInt(100) - 50;

                // Multiplier may be 0.8 ~ 1.2
                fareMultiplier = (mRNG.nextDouble() * 0.4) + 0.8;
            }

            newTrips.add(new ObfuscatedTrip(trip, timeDelta, fareOffset, fareMultiplier));
        }
        return newTrips;
    }
}
