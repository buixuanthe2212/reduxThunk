package com.jinjerkeihi.nfcfelica.transit.unknown;

import android.annotation.SuppressLint;

import com.jinjerkeihi.R;
import com.jinjerkeihi.nfcfelica.card.Card;
import com.jinjerkeihi.nfcfelica.card.desfire.DesfireApplication;
import com.jinjerkeihi.nfcfelica.card.desfire.DesfireCard;
import com.jinjerkeihi.nfcfelica.card.desfire.files.DesfireFile;
import com.jinjerkeihi.nfcfelica.card.desfire.files.UnauthorizedDesfireFile;
import com.jinjerkeihi.nfcfelica.transit.TransitIdentity;
import com.jinjerkeihi.nfcfelica.util.Utils;

/**
 * Handle MIFARE DESFire with no open sectors
 */

@SuppressLint("ParcelCreator")
public class UnauthorizedDesfireTransitData extends UnauthorizedTransitData {
    /**
     * This should be the last executed MIFARE DESFire check, after all the other checks are done.
     * <p>
     * This is because it will catch others' cards.
     *
     * @param card Card to read.
     * @return true if all sectors on the card are locked.
     */
    public static boolean check(DesfireCard card) {
        for (DesfireApplication app : card.getApplications()) {
            for (DesfireFile f : app.getFiles()) {
                if (!(f instanceof UnauthorizedDesfireFile)) {
                    // At least one file is "open", this is not for us.
                    return false;
                }
            }
        }

        // No file had open access.
        return true;
    }

    public static TransitIdentity parseTransitIdentity(Card card) {
        return new TransitIdentity(Utils.localizeString(R.string.locked_mfd_card), null);
    }

    @Override
    public String getCardName() {
        return Utils.localizeString(R.string.locked_mfd_card);
    }

}
