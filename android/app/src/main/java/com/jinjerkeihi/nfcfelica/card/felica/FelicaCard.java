/*
 * FelicaCard.java
 *
 * Copyright 2011 Eric Butler <eric@codebutler.com>
 * Copyright 2016-2018 Michael Farrell <micolous+git@gmail.com>
 *
 * Octopus reading code based on FelicaCard.java from nfcard project
 * Copyright 2013 Sinpo Wei <sinpowei@gmail.com>
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

package com.jinjerkeihi.nfcfelica.card.felica;

import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.util.Log;

import com.jinjerkeihi.R;
import com.jinjerkeihi.felica.FeliCaTag;
import com.jinjerkeihi.felica.command.ReadResponse;
import com.jinjerkeihi.felica.lib.FeliCaLib;
import com.jinjerkeihi.nfcfelica.card.Card;
import com.jinjerkeihi.nfcfelica.card.CardType;
import com.jinjerkeihi.nfcfelica.card.TagReaderFeedbackInterface;
import com.jinjerkeihi.nfcfelica.transit.CardInfo;
import com.jinjerkeihi.nfcfelica.transit.TransitData;
import com.jinjerkeihi.nfcfelica.transit.TransitIdentity;
import com.jinjerkeihi.nfcfelica.transit.edy.EdyTransitData;
import com.jinjerkeihi.nfcfelica.transit.octopus.OctopusTransitData;
import com.jinjerkeihi.nfcfelica.transit.suica.SuicaTransitData;
import com.jinjerkeihi.nfcfelica.util.Utils;

import org.apache.commons.lang3.ArrayUtils;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


@Root(name = "card")
public class FelicaCard extends Card {
    private static final String TAG = "FelicaCard";

    @Element(name = "idm")
    private FeliCaLib.IDm mIDm;
    @Element(name = "pmm")
    private FeliCaLib.PMm mPMm;
    @ElementList(name = "systems")
    private List<FelicaSystem> mSystems;

    private FelicaCard() { /* For XML Serializer */ }

    public FelicaCard(byte[] tagId, Calendar scannedAt, FeliCaLib.IDm idm, FeliCaLib.PMm pmm, FelicaSystem[] systems) {
        super(CardType.FeliCa, tagId, scannedAt);
        mIDm = idm;
        mPMm = pmm;
        mSystems = Utils.arrayAsList(systems);
    }

    // https://github.com/tmurakam/felicalib/blob/master/src/dump/dump.c
    // https://github.com/tmurakam/felica2money/blob/master/src/card/Suica.cs
    public static FelicaCard dumpTag(byte[] tagId, Tag tag, TagReaderFeedbackInterface feedbackInterface) throws Exception {
        NfcF nfcF = NfcF.get(tag);
        Log.d(TAG, "Default system code: " + Utils.getHexString(nfcF.getSystemCode()));

        boolean octopusMagic = false;
        boolean sztMagic = false;

        FeliCaTag ft = new FeliCaTag(tag);

        FeliCaLib.IDm idm = ft.pollingAndGetIDm(FeliCaLib.SYSTEMCODE_ANY);
        FeliCaLib.PMm pmm = ft.getPMm();

        if (idm == null)
            throw new Exception("Failed to read IDm");

        List<FelicaSystem> systems = new ArrayList<>();

        // FIXME: Enumerate "areas" inside of systems ???
        List<FeliCaLib.SystemCode> codes = Arrays.asList(ft.getSystemCodeList());

        // Check if we failed to get a System Code
        if (codes.size() == 0) {
            // Lets try to ping for an Octopus anyway
            FeliCaLib.IDm octopusSystem = ft.pollingAndGetIDm(FeliCaLib.SYSTEMCODE_OCTOPUS);
            if (octopusSystem != null) {
                Log.d(TAG, "Detected Octopus card");
                // Octopus has a special knocking sequence to allow unprotected reads, and does not
                // respond to the normal system code listing.
                codes.add(new FeliCaLib.SystemCode(FeliCaLib.SYSTEMCODE_OCTOPUS));
                octopusMagic = true;
                feedbackInterface.showCardType(CardInfo.OCTOPUS);
            }

            FeliCaLib.IDm sztSystem = ft.pollingAndGetIDm(FeliCaLib.SYSTEMCODE_SZT);
            if (sztSystem != null) {
                Log.d(TAG, "Detected Shenzhen Tong card");
                // Because Octopus and SZT are similar systems, use the same knocking sequence in
                // case they have the same bugs with system code listing.
                codes.add(new FeliCaLib.SystemCode(FeliCaLib.SYSTEMCODE_SZT));
                sztMagic = true;
                feedbackInterface.showCardType(CardInfo.SZT);
            }
        }

        // Convert the system codes to a flat list
        // TODO: Push this into FeliCaTag instead
        int[] systemCodes = new int[codes.size()];
        for (int i=0; i<codes.size(); i++) {
            systemCodes[i] = codes.get(i).getCode();
        }

        CardInfo i = parseEarlyCardInfo(systemCodes);
        if (i != null) {
            Log.d(TAG, String.format(Locale.ENGLISH, "Early Card Info: %s", i.getName()));
            feedbackInterface.updateStatusText(Utils.localizeString(R.string.card_reading_type, i.getName()));
            feedbackInterface.showCardType(i);
        }

        for (FeliCaLib.SystemCode code : codes) {
            Log.d(TAG, "Got system code: " + Utils.getHexString(code.getBytes()));

            int systemCode = code.getCode();
            //ft.polling(systemCode);

            FeliCaLib.IDm thisIdm = ft.pollingAndGetIDm(systemCode);

            Log.d(TAG, " - Got IDm: " + Utils.getHexString(thisIdm.getBytes()) + "  compare: "
                    + Utils.getHexString(idm.getBytes()));

            byte[] foo = idm.getBytes();
            ArrayUtils.reverse(foo);
            Log.d(TAG, " - Got Card ID? " + Utils.byteArrayToInt(idm.getBytes(), 2, 6) + "  "
                    + Utils.byteArrayToInt(foo, 2, 6));

            Log.d(TAG, " - Got PMm: " + Utils.getHexString(ft.getPMm().getBytes()) + "  compare: "
                    + Utils.getHexString(pmm.getBytes()));

            List<FelicaService> services = new ArrayList<>();
            FeliCaLib.ServiceCode[] serviceCodes;

            if (octopusMagic && code.getCode() == FeliCaLib.SYSTEMCODE_OCTOPUS) {
                Log.d(TAG, "Stuffing in Octopus magic service code");
                serviceCodes = new FeliCaLib.ServiceCode[]{new FeliCaLib.ServiceCode(FeliCaLib.SERVICE_OCTOPUS)};
            } else if (sztMagic && code.getCode() == FeliCaLib.SYSTEMCODE_SZT) {
                Log.d(TAG, "Stuffing in SZT magic service code");
                serviceCodes = new FeliCaLib.ServiceCode[]{new FeliCaLib.ServiceCode(FeliCaLib.SERVICE_SZT)};
            } else {
                serviceCodes = ft.getServiceCodeList();
            }

            // Brute Forcer (DEBUG ONLY)
            //if (octopusMagic)
            //for (int serviceCodeInt=0; serviceCodeInt<0xffff; serviceCodeInt++) {
            //    Log.d(TAG, "Trying to read from service code " + serviceCodeInt);
            //    FeliCaLib.ServiceCode serviceCode = new FeliCaLib.ServiceCode(serviceCodeInt);

            for (FeliCaLib.ServiceCode serviceCode : serviceCodes) {
                byte[] bytes = serviceCode.getBytes();
                ArrayUtils.reverse(bytes);
                int serviceCodeInt = Utils.byteArrayToInt(bytes);
                serviceCode = new FeliCaLib.ServiceCode(serviceCode.getBytes());

                List<FelicaBlock> blocks = new ArrayList<>();

                ft.polling(systemCode);

                byte addr = 0;
                ReadResponse result = ft.readWithoutEncryption(serviceCode, addr);
                while (result != null && result.getStatusFlag1() == 0) {
                    blocks.add(new FelicaBlock(addr, result.getBlockData()));
                    addr++;
                    result = ft.readWithoutEncryption(serviceCode, addr);
                }

                if (blocks.size() > 0) { // Most service codes appear to be empty...
                    FelicaBlock[] blocksArray = blocks.toArray(new FelicaBlock[blocks.size()]);
                    services.add(new FelicaService(serviceCodeInt, blocksArray));
                    Log.d(TAG, "- Service code " + serviceCodeInt + " had " + blocks.size() + " blocks");
                }
            }

            FelicaService[] servicesArray = services.toArray(new FelicaService[services.size()]);
            systems.add(new FelicaSystem(code.getCode(), servicesArray));
        }

        FelicaSystem[] systemsArray = systems.toArray(new FelicaSystem[systems.size()]);
        return new FelicaCard(tagId, GregorianCalendar.getInstance(), idm, pmm, systemsArray);
    }

    public FeliCaLib.IDm getIDm() {
        return mIDm;
    }

    public FeliCaLib.PMm getPMm() {
        return mPMm;
    }

    // FIXME: Getters that parse IDm...

    // date ????
    /*
    public int getManufactureCode() {

    }

    public int getCardIdentification() {

    }

    public int getROMType() {

    }

    public int getICType() {

    }

    public int getTimeout() {

    }
    */

    public List<FelicaSystem> getSystems() {
        return mSystems;
    }

    public FelicaSystem getSystem(int systemCode) {
        for (FelicaSystem system : mSystems) {
            if (system.getCode() == systemCode) {
                return system;
            }
        }
        return null;
    }

    /**
     * Felica has well-known system IDs.  If those system IDs are sufficient to detect
     * a particular type of card (or at least have a really good guess at it), then we should send
     * back a CardInfo.
     *
     * If we have no idea, then send back "null".
     *
     * Each of these checks should be really cheap to run, because this blocks further card
     * reads.
     * @param systemCodes The system codes that exist on the card.
     * @return A CardInfo about the card, or null if we have no idea.
     */
    static CardInfo parseEarlyCardInfo(int[] systemCodes) {
        if (SuicaTransitData.earlyCheck(systemCodes))
            return CardInfo.SUICA;
        if (EdyTransitData.earlyCheck(systemCodes))
            return CardInfo.EDY;

        // Do Octopus last -- it returns null if it's not a supported Octopus derivative.
        return OctopusTransitData.earlyCheck(systemCodes);
    }

    @Override
    public TransitIdentity parseTransitIdentity() {
        if (SuicaTransitData.check(this))
            return SuicaTransitData.parseTransitIdentity(this);
        else if (EdyTransitData.check(this))
            return EdyTransitData.parseTransitIdentity(this);
        else if (OctopusTransitData.check(this))
            return OctopusTransitData.parseTransitIdentity(this);
        return null;
    }

    @Override
    public TransitData parseTransitData() {
        if (SuicaTransitData.check(this))
            return new SuicaTransitData(this);
        else if (EdyTransitData.check(this))
            return new EdyTransitData(this);
        else if (OctopusTransitData.check(this))
            return new OctopusTransitData(this);
        return null;
    }
}
