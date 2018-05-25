/*
 * DesfireCard.java
 *
 * Copyright 2011-2015 Eric Butler <eric@codebutler.com>
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

package com.jinjerkeihi.nfcfelica.card.desfire;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import com.jinjerkeihi.R;
import com.jinjerkeihi.nfcfelica.card.Card;
import com.jinjerkeihi.nfcfelica.card.CardType;
import com.jinjerkeihi.nfcfelica.card.TagReaderFeedbackInterface;
import com.jinjerkeihi.nfcfelica.card.desfire.files.DesfireFile;
import com.jinjerkeihi.nfcfelica.card.desfire.files.InvalidDesfireFile;
import com.jinjerkeihi.nfcfelica.card.desfire.files.UnauthorizedDesfireFile;
import com.jinjerkeihi.nfcfelica.card.desfire.settings.DesfireFileSettings;
import com.jinjerkeihi.nfcfelica.card.desfire.settings.StandardDesfireFileSettings;
import com.jinjerkeihi.nfcfelica.card.desfire.settings.ValueDesfireFileSettings;
import com.jinjerkeihi.nfcfelica.transit.CardInfo;
import com.jinjerkeihi.nfcfelica.transit.TransitData;
import com.jinjerkeihi.nfcfelica.transit.TransitIdentity;
import com.jinjerkeihi.nfcfelica.transit.clipper.ClipperTransitData;
import com.jinjerkeihi.nfcfelica.transit.hsl.HSLTransitData;
import com.jinjerkeihi.nfcfelica.transit.myki.MykiTransitData;
import com.jinjerkeihi.nfcfelica.transit.opal.OpalTransitData;
import com.jinjerkeihi.nfcfelica.transit.orca.OrcaTransitData;
import com.jinjerkeihi.nfcfelica.transit.stub.AdelaideMetrocardStubTransitData;
import com.jinjerkeihi.nfcfelica.transit.stub.AtHopStubTransitData;
import com.jinjerkeihi.nfcfelica.transit.unknown.UnauthorizedDesfireTransitData;
import com.jinjerkeihi.nfcfelica.util.Utils;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


@Root(name = "card")
public class DesfireCard extends Card {
    private static final String TAG = "DesfireCard";

    @Element(name = "manufacturing-data")
    private DesfireManufacturingData mManfData;
    @ElementList(name = "applications")
    private List<DesfireApplication> mApplications;

    private DesfireCard() { /* For XML Serializer */ }

    public DesfireCard(byte[] tagId, Calendar scannedAt, DesfireManufacturingData manfData, DesfireApplication[] apps) {
        super(CardType.MifareDesfire, tagId, scannedAt);
        mManfData = manfData;
        mApplications = Utils.arrayAsList(apps);
    }

    /**
     * Dumps a DESFire tag in the field.
     * @param tag Tag to dump.
     * @return DesfireCard of the card contents. Returns null if an unsupported card is in the
     *         field.
     * @throws Exception On communication errors.
     */
    public static DesfireCard dumpTag(Tag tag, TagReaderFeedbackInterface feedbackInterface) throws Exception {
        List<DesfireApplication> apps = new ArrayList<>();

        IsoDep tech = IsoDep.get(tag);

        tech.connect();

        DesfireManufacturingData manufData;
        DesfireApplication[] appsArray;

        try {
            DesfireProtocol desfireTag = new DesfireProtocol(tech);

            try {
                manufData = desfireTag.getManufacturingData();
            } catch (IllegalArgumentException e) {
                // Credit cards tend to fail at this point.
                Log.w(TAG, "Card responded with invalid response, may not be DESFire?", e);
                return null;
            }

            feedbackInterface.updateStatusText(Utils.localizeString(R.string.mfd_reading));
            feedbackInterface.updateProgressBar(0, 1);

            int[] appIds = desfireTag.getAppList();
            int maxProgress = appIds.length;
            int progress = 0;

            CardInfo i = parseEarlyCardInfo(appIds);
            if (i != null) {
                Log.d(TAG, String.format(Locale.ENGLISH, "Early Card Info: %s", i.getName()));
                feedbackInterface.updateStatusText(Utils.localizeString(R.string.card_reading_type, i.getName()));
                feedbackInterface.showCardType(i);
            }

            // Uncomment this to test the card type display.
            //Thread.sleep(5000);

            for (int appId : appIds) {
                feedbackInterface.updateProgressBar(progress, maxProgress);
                desfireTag.selectApp(appId);
                progress++;

                List<DesfireFile> files = new ArrayList<>();

                int[] fileIds = desfireTag.getFileList();
                maxProgress += fileIds.length;
                for (int fileId : fileIds) {
                    feedbackInterface.updateProgressBar(progress, maxProgress);
                    DesfireFileSettings settings = null;
                    try {
                        settings = desfireTag.getFileSettings(fileId);
                        byte[] data;
                        if (settings instanceof StandardDesfireFileSettings) {
                            data = desfireTag.readFile(fileId);
                        } else if (settings instanceof ValueDesfireFileSettings) {
                            data = desfireTag.getValue(fileId);
                        } else {
                            data = desfireTag.readRecord(fileId);
                        }
                        files.add(DesfireFile.create(fileId, settings, data));
                    } catch (AccessControlException ex) {
                        files.add(new UnauthorizedDesfireFile(fileId, ex.getMessage(), settings));
                    } catch (IOException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        files.add(new InvalidDesfireFile(fileId, ex.toString(), settings));
                    }
                    progress++;
                }

                DesfireFile[] filesArray = new DesfireFile[files.size()];
                files.toArray(filesArray);

                apps.add(new DesfireApplication(appId, filesArray));
            }

            appsArray = new DesfireApplication[apps.size()];
            apps.toArray(appsArray);
        } finally {
            if (tech.isConnected())
                tech.close();
        }

        return new DesfireCard(tag.getId(), GregorianCalendar.getInstance(), manufData, appsArray);
    }

    /**
     * DESFire has well-known application IDs.  If those application IDs are sufficient to detect
     * a particular type of card (or at least have a really good guess at it), then we should send
     * back a CardInfo.
     *
     * If we have no idea, then send back "null".
     *
     * Each of these checks should be really cheap to run, because this blocks further card
     * reads.
     * @param appIds An array of DESFire application IDs that are present on the card.
     * @return A CardInfo about the card, or null if we have no idea.
     */
    static CardInfo parseEarlyCardInfo(int[] appIds) {
        if (OrcaTransitData.earlyCheck(appIds))
            return CardInfo.ORCA;
        if (ClipperTransitData.earlyCheck(appIds))
            return CardInfo.CLIPPER;
        if (HSLTransitData.earlyCheck(appIds))
            return CardInfo.HSL;
        if (OpalTransitData.earlyCheck(appIds))
            return CardInfo.OPAL;
        if (MykiTransitData.earlyCheck(appIds))
            return CardInfo.MYKI;

        return null;
    }

    @Override
    public TransitIdentity parseTransitIdentity() {
        if (OrcaTransitData.check(this))
            return OrcaTransitData.parseTransitIdentity(this);
        if (ClipperTransitData.check(this))
            return ClipperTransitData.parseTransitIdentity(this);
        if (HSLTransitData.check(this))
            return HSLTransitData.parseTransitIdentity(this);
        if (OpalTransitData.check(this))
            return OpalTransitData.parseTransitIdentity(this);
        if (MykiTransitData.check(this))
            return MykiTransitData.parseTransitIdentity(this);

        // Stub card types go last
        if (AdelaideMetrocardStubTransitData.check(this))
            return AdelaideMetrocardStubTransitData.parseTransitIdentity(this);
        if (AtHopStubTransitData.check(this))
            return AtHopStubTransitData.parseTransitIdentity(this);

        if (UnauthorizedDesfireTransitData.check(this))
            return UnauthorizedDesfireTransitData.parseTransitIdentity(this);
        return null;
    }

    @Override
    public TransitData parseTransitData() {
        if (OrcaTransitData.check(this))
            return new OrcaTransitData(this);
        if (ClipperTransitData.check(this))
            return new ClipperTransitData(this);
        if (HSLTransitData.check(this))
            return new HSLTransitData(this);
        if (OpalTransitData.check(this))
            return new OpalTransitData(this);
        if (MykiTransitData.check(this))
            return new MykiTransitData(this);

        // Stub card types go last
        if (AdelaideMetrocardStubTransitData.check(this))
            return new AdelaideMetrocardStubTransitData(this);
        if (AtHopStubTransitData.check(this))
            return new AtHopStubTransitData(this);

        if (UnauthorizedDesfireTransitData.check(this))
            return new UnauthorizedDesfireTransitData();
        return null;
    }

    public List<DesfireApplication> getApplications() {
        return mApplications;
    }

    public DesfireApplication getApplication(int appId) {
        for (DesfireApplication app : mApplications) {
            if (app.getId() == appId)
                return app;
        }
        return null;
    }

    public DesfireManufacturingData getManufacturingData() {
        return mManfData;
    }
}
