/*
 * UltralightCard.java
 *
 * Copyright 2016-2018 Michael Farrell <micolous+git@gmail.com>
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
package com.jinjerkeihi.nfcfelica.card.ultralight;

import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.support.annotation.Keep;
import android.util.Log;

import com.jinjerkeihi.R;
import com.jinjerkeihi.nfcfelica.card.Card;
import com.jinjerkeihi.nfcfelica.card.CardType;
import com.jinjerkeihi.nfcfelica.card.TagReaderFeedbackInterface;
import com.jinjerkeihi.nfcfelica.card.UnsupportedTagException;
import com.jinjerkeihi.nfcfelica.transit.TransitData;
import com.jinjerkeihi.nfcfelica.transit.TransitIdentity;
import com.jinjerkeihi.nfcfelica.transit.unknown.UnauthorizedUltralightTransitData;
import com.jinjerkeihi.nfcfelica.util.Utils;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


/**
 * Utility class for reading MIFARE Ultralight / Ultralight C
 */
@Root(name = "card")
public class UltralightCard extends Card {
    private static final String TAG = "UltralightCard";

    @ElementList(name = "pages")
    private List<UltralightPage> mPages;

    /**
     * This was previously used for Ultralight type checks in Metrodroid. However, this is based on
     * Android's type checks, which aren't great (see UltralightProtocol.getCardType. This should
     * not be used. This is included so that users with scans in the old XML format won't see an
     * error.
     */
    @SuppressWarnings("unused")
    @Deprecated
    @Keep
    @Attribute(name = "ultralightType", required = false)
    private int mDeprecatedUltralightType;

    @Attribute(name = "cardModel", required = false)
    private String mCardModel;

    private UltralightCard() { /* For XML Serializer */ }

    public UltralightCard(byte[] tagId, Calendar scannedAt, String cardModel, UltralightPage[] pages) {
        super(CardType.MifareUltralight, tagId, scannedAt);
        mCardModel = cardModel;
        mPages = Utils.arrayAsList(pages);
    }

    public static UltralightCard dumpTag(byte[] tagId, Tag tag, TagReaderFeedbackInterface feedbackInterface) throws Exception {
        MifareUltralight tech = null;

        try {
            tech = MifareUltralight.get(tag);
            tech.connect();
            feedbackInterface.updateProgressBar(0, 1);
            feedbackInterface.updateStatusText(Utils.localizeString(R.string.mfu_detect));

            UltralightProtocol p = new UltralightProtocol(tech);
            UltralightProtocol.UltralightType t = p.getCardType();

            if (t.pageCount <= 0) {
                throw new UnsupportedTagException(new String[]{"Ultralight"}, "Unknown Ultralight type");
            }

            feedbackInterface.updateStatusText(Utils.localizeString(R.string.mfu_reading));
            feedbackInterface.showCardType(null);

            // Now iterate through the pages and grab all the datas
            int pageNumber = 0;
            byte[] pageBuffer = new byte[0];
            List<UltralightPage> pages = new ArrayList<>();
            boolean unauthorized = false;
            while (pageNumber <= t.pageCount) {
                if (pageNumber % 4 == 0) {
                    feedbackInterface.updateProgressBar(pageNumber, t.pageCount);
                    // Lets make a new buffer of data. (16 bytes = 4 pages * 4 bytes)
                    try {
                        pageBuffer = tech.readPages(pageNumber);
                        unauthorized = false;
                    } catch (IOException e) {
                        // Transceive failure, maybe authentication problem
                        unauthorized = true;
                        Log.d(TAG, String.format(Locale.ENGLISH, "Unable to read page %d", pageNumber), e);
                    }
                }

                // Now lets stuff this into some pages.
                if (!unauthorized) {
                    pages.add(new UltralightPage(pageNumber, Arrays.copyOfRange(
                            pageBuffer,
                            (pageNumber % 4) * MifareUltralight.PAGE_SIZE,
                            ((pageNumber % 4) + 1) * MifareUltralight.PAGE_SIZE)));
                } else {
                    pages.add(new UnauthorizedUltralightPage(pageNumber));
                }
                pageNumber++;
            }

            // Now we have pages to stuff in the card.
            return new UltralightCard(tagId, GregorianCalendar.getInstance(), t.toString(),
                    pages.toArray(new UltralightPage[pages.size()]));

        } finally {
            if (tech != null && tech.isConnected()) {
                tech.close();
            }
        }
    }


    @Override
    public TransitIdentity parseTransitIdentity() {
        if (UnauthorizedUltralightTransitData.check(this)) {
            // This check must be LAST.
            //
            // This is to throw up a warning whenever there is a card with all locked sectors
            return UnauthorizedUltralightTransitData.parseTransitIdentity(this);
        }

        // The card could not be identified.
        return null;
    }

    @Override
    public TransitData parseTransitData() {
        if (UnauthorizedUltralightTransitData.check(this)) {
            // This check must be LAST.
            //
            // This is to throw up a warning whenever there is a card with all locked sectors
            return new UnauthorizedUltralightTransitData();
        }

        // The card could not be identified.
        return null;
    }

    public UltralightPage[] getPages() {
        return mPages.toArray(new UltralightPage[mPages.size()]);
    }

    public UltralightPage getPage(int index) {
        return mPages.get(index);
    }

    /**
     * Get the model of Ultralight card this is.
     *
     * @return Model of Ultralight card this is.
     */
    public String getCardModel() {
        return mCardModel;
    }
}
