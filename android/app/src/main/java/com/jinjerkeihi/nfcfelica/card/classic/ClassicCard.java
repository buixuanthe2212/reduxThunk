/*
 * ClassicCard.java
 *
 * Copyright 2012-2015 Eric Butler <eric@codebutler.com>
 * Copyright 2012 Wilbert Duijvenvoorde <w.a.n.duijvenvoorde@gmail.com>
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

package com.jinjerkeihi.nfcfelica.card.classic;

import android.content.SharedPreferences;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jinjerkeihi.MainApplication;
import com.jinjerkeihi.R;
import com.jinjerkeihi.nfcfelica.card.Card;
import com.jinjerkeihi.nfcfelica.card.CardType;
import com.jinjerkeihi.nfcfelica.card.TagReaderFeedbackInterface;
import com.jinjerkeihi.nfcfelica.key.CardKeys;
import com.jinjerkeihi.nfcfelica.key.ClassicCardKeys;
import com.jinjerkeihi.nfcfelica.key.ClassicSectorKey;
import com.jinjerkeihi.nfcfelica.transit.TransitData;
import com.jinjerkeihi.nfcfelica.transit.TransitIdentity;
import com.jinjerkeihi.nfcfelica.transit.bilhete_unico.BilheteUnicoSPTransitData;
import com.jinjerkeihi.nfcfelica.transit.chc_metrocard.ChcMetrocardTransitData;
import com.jinjerkeihi.nfcfelica.transit.erg.ErgTransitData;
import com.jinjerkeihi.nfcfelica.transit.lax_tap.LaxTapTransitData;
import com.jinjerkeihi.nfcfelica.transit.manly_fast_ferry.ManlyFastFerryTransitData;
import com.jinjerkeihi.nfcfelica.transit.nextfare.NextfareTransitData;
import com.jinjerkeihi.nfcfelica.transit.ovc.OVChipTransitData;
import com.jinjerkeihi.nfcfelica.transit.seq_go.SeqGoTransitData;
import com.jinjerkeihi.nfcfelica.transit.smartrider.SmartRiderTransitData;
import com.jinjerkeihi.nfcfelica.transit.unknown.UnauthorizedClassicTransitData;
import com.jinjerkeihi.nfcfelica.util.Utils;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

@Root(name = "card")

public class ClassicCard extends Card {
    public static final byte[] PREAMBLE_KEY = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00};

    static final byte[][] WELL_KNOWN_KEYS = {
            PREAMBLE_KEY,
            MifareClassic.KEY_DEFAULT,
            MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY,
            MifareClassic.KEY_NFC_FORUM
    };

    private static final String TAG = "ClassicCard";
    @ElementList(name = "sectors")
    private List<ClassicSector> mSectors;

    private ClassicCard() { /* For XML Serializer */ }

    public ClassicCard(byte[] tagId, Calendar scannedAt, ClassicSector[] sectors) {
        super(CardType.MifareClassic, tagId, scannedAt);
        mSectors = Utils.arrayAsList(sectors);
    }

    public static ClassicCard dumpTag(byte[] tagId, Tag tag, TagReaderFeedbackInterface feedbackInterface) throws Exception {
        feedbackInterface.updateStatusText(Utils.localizeString(R.string.mfc_reading));
        feedbackInterface.showCardType(null);

        MifareClassic tech = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
        final int retryLimit = prefs.getInt(MainApplication.PREF_MFC_AUTHRETRY, 5);
        int retriesLeft;

        try {
            try {
                tech = MifareClassic.get(tag);
            } catch (NullPointerException e) {
                Log.d(TAG, "Working around broken Android NFC on HTC devices (and others)", e);
                tech = MifareClassic.get(patchTag(tag));
            }
            tech.connect();

            ClassicCardKeys keys = (ClassicCardKeys) CardKeys.forTagId(tagId);

            List<ClassicSector> sectors = new ArrayList<>();
            final int maxProgress = tech.getSectorCount() * 5;

            for (int sectorIndex = 0; sectorIndex < tech.getSectorCount(); sectorIndex++) {
                try {
                    byte[] correctKey = null;
                    feedbackInterface.updateProgressBar(sectorIndex * 5, maxProgress);

                    if (keys != null) {
                        feedbackInterface.updateStatusText(Utils.localizeString(R.string.mfc_have_key, sectorIndex));
                        // Try to authenticate with the sector multiple times, in case we have
                        // impaired communications with the card.
                        retriesLeft = retryLimit;

                        while (correctKey == null && retriesLeft-- > 0) {
                            // If we have a known key for the sector on the card, try this first.
                            Log.d(TAG, "Attempting authentication on sector " + sectorIndex + ", " + retriesLeft + " tries remain...");
                            ClassicSectorKey sectorKey = keys.keyForSector(sectorIndex);
                            if (sectorKey != null) {
                                if (sectorKey.getType().equals(ClassicSectorKey.TYPE_KEYA)) {
                                    if (tech.authenticateSectorWithKeyA(sectorIndex, sectorKey.getKey())) {
                                        correctKey = sectorKey.getKey();
                                    } else if (tech.authenticateSectorWithKeyB(sectorIndex, sectorKey.getKey())) {
                                        correctKey = sectorKey.getKey();
                                    }
                                } else {
                                    if (tech.authenticateSectorWithKeyB(sectorIndex, sectorKey.getKey())) {
                                        correctKey = sectorKey.getKey();
                                    } else if (tech.authenticateSectorWithKeyA(sectorIndex, sectorKey.getKey())) {
                                        correctKey = sectorKey.getKey();
                                    }
                                }
                            }
                        }
                    }

                    // Try with the other keys
                    retriesLeft = retryLimit;

                    if (correctKey == null) {
                        feedbackInterface.updateProgressBar((sectorIndex * 5) + 1, maxProgress);

                        while (correctKey == null && (retriesLeft-- > 0)) {
                            Log.d(TAG, "Attempting authentication with other keys on sector " + sectorIndex + ", " + retriesLeft + " tries remain...");

                            // Attempt authentication with alternate keys
                            if (keys != null) {
                                feedbackInterface.updateStatusText(Utils.localizeString(R.string.mfc_other_key, sectorIndex));

                                // Be a little more forgiving on the key list.  Lets try all the keys!
                                //
                                // This takes longer, of course, but means that users aren't scratching
                                // their heads when we don't get the right key straight away.
                                ClassicSectorKey[] cardKeys = keys.keys();

                                for (int keyIndex = 0; keyIndex < cardKeys.length; keyIndex++) {
                                    if (keyIndex == sectorIndex) {
                                        // We tried this before
                                        continue;
                                    }

                                    if (cardKeys[keyIndex].getType().equals(ClassicSectorKey.TYPE_KEYA)) {
                                        if (tech.authenticateSectorWithKeyA(sectorIndex, cardKeys[keyIndex].getKey())) {
                                            correctKey = cardKeys[keyIndex].getKey();
                                        }
                                    } else {
                                        if (tech.authenticateSectorWithKeyB(sectorIndex, cardKeys[keyIndex].getKey())) {
                                            correctKey = cardKeys[keyIndex].getKey();
                                        }
                                    }

                                    if (correctKey != null) {
                                        // Jump out if we have the key
                                        Log.d(TAG, String.format("Authenticated successfully to sector %d with key for sector %d. "
                                                + "Fix the key file to speed up authentication", sectorIndex, keyIndex));
                                        break;
                                    }
                                }
                            }

                            // Try the default keys last.  If these are the only keys we have, the other steps will be skipped.
                            if (correctKey == null) {
                                feedbackInterface.updateProgressBar((sectorIndex * 5) + 2, maxProgress);

                                feedbackInterface.updateStatusText(Utils.localizeString(R.string.mfc_default_key, sectorIndex));
                                for (byte[] wkKey : WELL_KNOWN_KEYS) {
                                    if (tech.authenticateSectorWithKeyA(sectorIndex, wkKey)) {
                                        correctKey = wkKey;
                                        break;
                                    } else if (tech.authenticateSectorWithKeyB(sectorIndex, wkKey)) {
                                        correctKey = wkKey;
                                        break;
                                    }
                                }
                            }

                        }
                    }

                    feedbackInterface.updateProgressBar((sectorIndex * 5) + 3, maxProgress);

                    // Hopefully we have a key by now...
                    if (correctKey != null) {
                        Log.d(TAG, "Authenticated successfully for sector " + sectorIndex);
                        feedbackInterface.updateStatusText(Utils.localizeString(R.string.mfc_reading_blocks, sectorIndex));
                        List<ClassicBlock> blocks = new ArrayList<>();
                        // FIXME: First read trailer block to get type of other blocks.
                        int firstBlockIndex = tech.sectorToBlock(sectorIndex);
                        for (int blockIndex = 0; blockIndex < tech.getBlockCountInSector(sectorIndex); blockIndex++) {
                            byte[] data = tech.readBlock(firstBlockIndex + blockIndex);
                            String type = ClassicBlock.TYPE_DATA; // FIXME
                            blocks.add(ClassicBlock.create(type, blockIndex, data));
                        }
                        sectors.add(new ClassicSector(sectorIndex, blocks.toArray(new ClassicBlock[blocks.size()]), correctKey));

                        feedbackInterface.updateProgressBar((sectorIndex * 5) + 4, maxProgress);
                    } else {
                        Log.d(TAG, "Authentication unsuccessful for sector " + sectorIndex + ", giving up");
                        sectors.add(new UnauthorizedClassicSector(sectorIndex));
                    }
                } catch (IOException ex) {
                    sectors.add(new InvalidClassicSector(sectorIndex, Utils.getErrorMessage(ex)));
                }
            }

            return new ClassicCard(tagId, GregorianCalendar.getInstance(), sectors.toArray(new ClassicSector[sectors.size()]));

        } finally {
            if (tech != null && tech.isConnected()) {
                tech.close();
            }
        }
    }

    /**
     * Patch the broken Tag object of HTC One (m7/m8) devices with Android 5.x.
     * <p>
     * Also observed on Galaxy Nexus running Cyanogenmod 13.
     * <p>
     * "It seems, the reason of this bug is TechExtras of NfcA is null.
     * However, TechList contains MIFAREClassic." -- bildin.
     * <p>
     * This patch will fix this. For more information please refer to
     * https://github.com/ikarus23/MIFAREClassicTool/issues/52
     * <p>
     * This patch was provided by bildin (https://github.com/bildin).
     *
     * @param tag The broken tag.
     * @return The fixed tag.
     */
    private static Tag patchTag(Tag tag) {
        if (tag == null) {
            return null;
        }

        String[] sTechList = tag.getTechList();
        Parcel oldParcel;
        Parcel newParcel;
        oldParcel = Parcel.obtain();
        tag.writeToParcel(oldParcel, 0);
        oldParcel.setDataPosition(0);

        int len = oldParcel.readInt();
        byte[] id = new byte[0];
        if (len >= 0) {
            id = new byte[len];
            oldParcel.readByteArray(id);
        }
        int[] oldTechList = new int[oldParcel.readInt()];
        oldParcel.readIntArray(oldTechList);
        Bundle[] oldTechExtras = oldParcel.createTypedArray(Bundle.CREATOR);
        int serviceHandle = oldParcel.readInt();
        int isMock = oldParcel.readInt();
        IBinder tagService;
        if (isMock == 0) {
            tagService = oldParcel.readStrongBinder();
        } else {
            tagService = null;
        }
        oldParcel.recycle();

        int nfcaIdx = -1;
        int mcIdx = -1;
        for (int idx = 0; idx < sTechList.length; idx++) {
            if (sTechList[idx].equals(NfcA.class.getName())) {
                nfcaIdx = idx;
            } else if (sTechList[idx].equals(MifareClassic.class.getName())) {
                mcIdx = idx;
            }
        }

        if (nfcaIdx >= 0 && mcIdx >= 0 && oldTechExtras[mcIdx] == null) {
            oldTechExtras[mcIdx] = oldTechExtras[nfcaIdx];
        } else {
            return tag;
        }

        newParcel = Parcel.obtain();
        newParcel.writeInt(id.length);
        newParcel.writeByteArray(id);
        newParcel.writeInt(oldTechList.length);
        newParcel.writeIntArray(oldTechList);
        newParcel.writeTypedArray(oldTechExtras, 0);
        newParcel.writeInt(serviceHandle);
        newParcel.writeInt(isMock);
        if (isMock == 0) {
            newParcel.writeStrongBinder(tagService);
        }
        newParcel.setDataPosition(0);
        Tag newTag = Tag.CREATOR.createFromParcel(newParcel);
        newParcel.recycle();

        return newTag;
    }

    public static String getFallbackReader() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
        return prefs.getString(MainApplication.PREF_MFC_FALLBACK, "null").toLowerCase();
    }

    @Override
    public TransitIdentity parseTransitIdentity() {
        // All .check() methods should work without a key, and throw an UnauthorizedException
        // Otherwise UnauthorizedClassicTransitData will not trigger
        if (OVChipTransitData.check(this)) {
            return OVChipTransitData.parseTransitIdentity(this);
        } else if (ErgTransitData.check(this)) {
            // Search through ERG on MIFARE Classic compatibles.
            if (ManlyFastFerryTransitData.check(this)) {
                return ManlyFastFerryTransitData.parseTransitIdentity(this);
            } else if (ChcMetrocardTransitData.check(this)) {
                return ChcMetrocardTransitData.parseTransitIdentity(this);
            } else {
                // Fallback
                return ErgTransitData.parseTransitIdentity(this);
            }
        } else if (NextfareTransitData.check(this)) {
            // Search through Nextfare on MIFARE Classic compatibles.
            if (SeqGoTransitData.check(this)) {
                return SeqGoTransitData.parseTransitIdentity(this);
            } else if (LaxTapTransitData.check(this)) {
                return LaxTapTransitData.parseTransitIdentity(this);
            } else {
                // Fallback
                return NextfareTransitData.parseTransitIdentity(this);
            }
        } else if (SmartRiderTransitData.check(this)) {
            return SmartRiderTransitData.parseTransitIdentity(this);
        } else if (UnauthorizedClassicTransitData.check(this)) {
            // This check must be SECOND TO LAST.
            //
            // This is to throw up a warning whenever there is a card with all locked sectors
            return UnauthorizedClassicTransitData.parseTransitIdentity(this);
        } else {
            // This check must be LAST.
            //
            // This is for agencies who don't have identifying "magic" in their card.
            String fallback = getFallbackReader();
            if (fallback.equals("bilhete_unico")) {
                return BilheteUnicoSPTransitData.parseTransitIdentity(this);
            } else if (fallback.equals("myway") || fallback.equals("smartrider")) {
                // This has a proper check now, but is included for legacy reasons.
                //
                // Before the introduction of key-based detection for these cards, Metrodroid did
                // not record the key inside the ClassicCard XML structure.
                return SmartRiderTransitData.parseTransitIdentity(this);
            }
        }

        // The card could not be identified, but has some open sectors.
        return null;
    }

    @Override
    public TransitData parseTransitData() {
        if (OVChipTransitData.check(this)) {
            return new OVChipTransitData(this);
        } else if (ErgTransitData.check(this)) {
            // Search through ERG on MIFARE Classic compatibles.
            if (ManlyFastFerryTransitData.check(this)) {
                return new ManlyFastFerryTransitData(this);
            } else if (ChcMetrocardTransitData.check(this)) {
                return new ChcMetrocardTransitData(this);
            } else {
                // Fallback
                return new ErgTransitData(this);
            }
        } else if (NextfareTransitData.check(this)) {
            // Search through Nextfare on MIFARE Classic compatibles.
            if (SeqGoTransitData.check(this)) {
                return new SeqGoTransitData(this);
            } else if (LaxTapTransitData.check(this)) {
                return new LaxTapTransitData(this);
            } else {
                // Fallback
                return new NextfareTransitData(this);
            }
        } else if (SmartRiderTransitData.check(this)) {
            return new SmartRiderTransitData(this);
        } else if (UnauthorizedClassicTransitData.check(this)) {
            // This check must be SECOND TO LAST.
            //
            // This is to throw up a warning whenever there is a card with all locked sectors
            return new UnauthorizedClassicTransitData();
        } else {
            // This check must be LAST.
            //
            // This is for agencies who don't have identifying "magic" in their card.
            String fallback = getFallbackReader();
            if (fallback.equals("bilhete_unico")) {
                return new BilheteUnicoSPTransitData(this);
            } else if (fallback.equals("myway")) {
                // TODO: Replace this with a proper check, and take out of fallback mode.
                return new SmartRiderTransitData(this);
            }
        }

        // The card could not be identified, but has some open sectors.
        return null;
    }

    public List<ClassicSector> getSectors() {
        return mSectors;
    }

    public ClassicSector getSector(int index) {
        return mSectors.get(index);
    }
}
