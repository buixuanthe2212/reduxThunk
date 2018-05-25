/*
 * UnauthorizedUltralightTransitData.java
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
package com.jinjerkeihi.nfcfelica.transit.unknown;


import android.annotation.SuppressLint;

import com.jinjerkeihi.R;
import com.jinjerkeihi.nfcfelica.card.Card;
import com.jinjerkeihi.nfcfelica.card.ultralight.UltralightCard;
import com.jinjerkeihi.nfcfelica.card.ultralight.UltralightPage;
import com.jinjerkeihi.nfcfelica.card.ultralight.UnauthorizedUltralightPage;
import com.jinjerkeihi.nfcfelica.transit.TransitIdentity;
import com.jinjerkeihi.nfcfelica.util.Utils;

/**
 * Handle MIFARE Ultralight with no open pages
 */
@SuppressLint("ParcelCreator")
public class UnauthorizedUltralightTransitData extends UnauthorizedTransitData {
    /**
     * This should be the last executed MIFARE Ultralight check, after all the other checks are done.
     * <p>
     * This is because it will catch others' cards.
     *
     * @param card Card to read.
     * @return true if all sectors on the card are locked.
     */
    public static boolean check(UltralightCard card) {
        // check to see if all sectors are blocked
        for (UltralightPage p : card.getPages()) {
            if (p.getIndex() >= 4) {
                // User memory is page 4 and above
                if (!(p instanceof UnauthorizedUltralightPage)) {
                    // At least one page is "open", this is not for us
                    return false;
                }
            }
        }
        return true;
    }

    public static TransitIdentity parseTransitIdentity(Card card) {
        return new TransitIdentity(Utils.localizeString(R.string.locked_mfu_card), null);
    }

    @Override
    public String getCardName() {
        return Utils.localizeString(R.string.locked_mfu_card);
    }
}
