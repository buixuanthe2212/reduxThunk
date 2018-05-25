/*
 * Card.java
 *
 * Copyright 2011-2014 Eric Butler <eric@codebutler.com>
 * Copyright 2016 Michael Farrell <micolous+git@gmail.com>
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

package com.jinjerkeihi.nfcfelica.card;

import android.nfc.Tag;
import android.util.Log;

import com.jinjerkeihi.R;
import com.jinjerkeihi.nfcfelica.card.cepas.CEPASCard;
import com.jinjerkeihi.nfcfelica.card.classic.ClassicCard;
import com.jinjerkeihi.nfcfelica.card.desfire.DesfireCard;
import com.jinjerkeihi.nfcfelica.card.felica.FelicaCard;
import com.jinjerkeihi.nfcfelica.card.ultralight.UltralightCard;
import com.jinjerkeihi.nfcfelica.transit.TransitData;
import com.jinjerkeihi.nfcfelica.transit.TransitIdentity;
import com.jinjerkeihi.nfcfelica.util.Utils;
import com.jinjerkeihi.nfcfelica.xml.HexString;

import org.apache.commons.lang3.ArrayUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Serializer;

import java.io.StringWriter;
import java.util.Calendar;

public abstract class Card {
    // This must be protected, not private, as otherwise the XML deserialiser fails to read the
    // card.
    @SuppressWarnings("WeakerAccess")
    @Attribute(name = "label", required = false)
    protected String mLabel;
    @Attribute(name = "type")
    private CardType mType;
    @Attribute(name = "id")
    private HexString mTagId;
    @Attribute(name = "scanned_at")
    private Calendar mScannedAt;

    protected Card() {
    }

    protected Card(CardType type, byte[] tagId, Calendar scannedAt) {
        this(type, tagId, scannedAt, null);
    }

    protected Card(CardType type, byte[] tagId, Calendar scannedAt, String label) {
        mType = type;
        mTagId = new HexString(tagId);
        mScannedAt = scannedAt;
        mLabel = label;
    }

    public static Card dumpTag(byte[] tagId, Tag tag, TagReaderFeedbackInterface feedbackInterface) throws Exception {
        final String[] techs = tag.getTechList();
        if (ArrayUtils.contains(techs, "android.nfc.tech.NfcB")) {
            // TODO: Fix Calypso cards
            return CEPASCard.dumpTag(tag);
        }

        if (ArrayUtils.contains(techs, "android.nfc.tech.IsoDep")) {
            feedbackInterface.updateStatusText(Utils.localizeString(R.string.iso14a_detect));

            // ISO 14443-4 card types
            // This also encompasses NfcA (ISO 14443-3A) and NfcB (ISO 14443-3B)
            DesfireCard d = DesfireCard.dumpTag(tag, feedbackInterface);
            if (d != null) {
                return d;
            }

            // Credit cards fall through here...
        }

        if (ArrayUtils.contains(techs, "android.nfc.tech.NfcF")) {
            return FelicaCard.dumpTag(tagId, tag, feedbackInterface);
        }

        if (ArrayUtils.contains(techs, "android.nfc.tech.MifareClassic")) {
            return ClassicCard.dumpTag(tagId, tag, feedbackInterface);
        }


        if (ArrayUtils.contains(techs, "android.nfc.tech.MifareUltralight")) {
            return UltralightCard.dumpTag(tagId, tag, feedbackInterface);
        }

        throw new UnsupportedTagException(techs, Utils.getHexString(tag.getId()));
    }

    public static Card fromXml(Serializer serializer, String xml) {
        try {
            return serializer.read(Card.class, xml);
        } catch (Exception ex) {
            Log.e("Card", "Failed to deserialize", ex);
            throw new RuntimeException(ex);
        }
    }

    public String toXml(Serializer serializer) {
        try {
            StringWriter writer = new StringWriter();
            serializer.write(this, writer);
            return writer.toString();
        } catch (Exception ex) {
            Log.e("Card", "Failed to serialize", ex);
            throw new RuntimeException(ex);
        }
    }

    public CardType getCardType() {
        return mType;
    }

    public byte[] getTagId() {
        return mTagId.getData();
    }

    public Calendar getScannedAt() {
        return mScannedAt;
    }

    public String getLabel() {
        return mLabel;
    }

    /**
     * This is where the "transit identity" is parsed, that is, a combination of the card type,
     * and the card's serial number (according to the operator).
     * @return
     */
    public abstract TransitIdentity parseTransitIdentity();

    /**
     * This is where a card is actually parsed into TransitData compatible data.
     * @return
     */
    public abstract TransitData parseTransitData();
}
