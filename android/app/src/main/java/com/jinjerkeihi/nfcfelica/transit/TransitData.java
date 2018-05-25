/*
 * TransitData.java
 *
 * Copyright (C) 2011 Eric Butler <eric@codebutler.com>
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

package com.jinjerkeihi.nfcfelica.transit;

import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.Spanned;

import com.jinjerkeihi.nfcfelica.ui.ListItem;

import java.util.List;

public abstract class TransitData implements Parcelable {

    /**
     * Balance of the card. The value is passed to getBalanceString for formatting purposes.
     *
     * @return The balance of the card, or null if it is not known.
     */
    @Nullable
    public abstract Integer getBalance();

    /**
     * Formats costs and balances in the appropriate local currency.  Be aware that your
     * implementation should use language-specific formatting and not rely on the system language
     * for that information.
     * <p>
     * For example, if a phone is set to English and travels to Japan, it does not make sense to
     * format their travel costs in dollars.  Instead, it should be shown in Yen, which the Japanese
     * currency formatter does.
     * <p>
     * This is used for formatting both card balances and fares. When a balance is formatted,
     * isBalance=true. When a trip or refill is being displayed, isBalance=false. You may wish to
     * implement your classes such that all credits or refills are shown as negative values.
     * <p>
     * This is an instance method to allow for cards that have multiple currencies (eg: Octopus),
     * or systems deployed in multiple countries with different currencies (eg: Nextfare).
     *
     * @param currency  Currency value
     * @param isBalance If true, a balance value is being formatted. Don't show negative amounts as
     *                  a credit.
     * @return The currency value formatted in the local currency of the card.
     */
    public abstract Spanned formatCurrencyString(int currency, boolean isBalance);

    public abstract String getSerialNumber();

    /**
     * Lists all trips on the card.  May return null if the trip information cannot be read.
     *
     * @return Array of Trip[], or null if not supported.
     */
    public Trip[] getTrips() {
        return null;
    }

    @Deprecated
    public Refill[] getRefills() {
        return null;
    }

    public Subscription[] getSubscriptions() {
        return null;
    }

    /**
     * Allows TransitData implementors to show extra information that doesn't fit within the
     * standard bounds of the interface.  By default, this returns null, so the "Info" tab will not
     * be displayed.
     * <p>
     * Note: in order to support obfuscation / hiding behaviour, if you implement this method, you
     * also need to use some other functionality:
     * <p>
     * - Check for MainApplication.hideCardNumbers whenever you show a card number, or other
     * mark (such as a name) that could be used to identify this card or its holder.
     * <p>
     * - Pass Calendar/Date objects (timestamps) through TripObfuscator.maybeObfuscateTS.  This also
     * works on epoch timestamps (expressed as seconds since UTC).
     * <p>
     * - Pass all currency amounts through formatCurrencyString. This is overridden by
     * ObfuscatedTrip, and will allow you to handle
     *
     * @return
     */
    public List<ListItem> getInfo() {
        return null;
    }

    ;

    public abstract String getCardName();

    /**
     * If a TransitData provider doesn't know some of the stops / stations on a user's card, then
     * it may raise a signal to the user to submit the unknown stations to our web service.
     *
     * @return false if all stations are known (default), true if there are unknown stations
     */
    public boolean hasUnknownStations() {
        return false;
    }

    public final int describeContents() {
        return 0;
    }

    /**
     * You can optionally add a link to an FAQ page for the card.  This will be shown in the ...
     * drop down menu for cards that are supported, and on the main page for subclasses of
     * StubTransitData.
     *
     * @return Uri pointing to an FAQ page, or null if no page is to be supplied.
     */
    public Uri getMoreInfoPage() {
        return null;
    }

    /**
     * You may optionally link to a page which allows you to view the online services for the card.
     *
     * @return Uri pointing to online services page, or null if no page is to be supplied.
     */
    public Uri getOnlineServicesPage() {
        return null;
    }
}
