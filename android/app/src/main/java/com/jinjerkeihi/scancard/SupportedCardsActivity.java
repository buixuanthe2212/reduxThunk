/*
 * SupportedCardsActivity.java
 *
 * Copyright 2011 Eric Butler
 * Copyright 2015-2018 Michael Farrell
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
package com.jinjerkeihi.scancard;

import android.app.Activity;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jinjerkeihi.MainApplication;
import com.jinjerkeihi.R;
import com.jinjerkeihi.nfcfelica.card.CardType;
import com.jinjerkeihi.nfcfelica.transit.CardInfo;
import com.jinjerkeihi.nfcfelica.util.Utils;

import java.util.ArrayList;


/**
 * @author Eric Butler, Michael Farrell
 */
public class SupportedCardsActivity extends Activity {

    private ListView mLisViewCard;
    private TextView mTvTitle;
    private ImageView mIvBack;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_card);
        findViewById();
        registerEvent();
        mTvTitle.setText("Support Card");
        CardsAdapter adapter = new CardsAdapter(this);
        mLisViewCard.setAdapter(adapter);

    }

    private void findViewById() {
        mLisViewCard = (ListView) findViewById(R.id.lv_support_card);
        mTvTitle = (TextView) findViewById(R.id.tvTitle);
        mIvBack = (ImageView) findViewById(R.id.ivBack);
    }

    private void registerEvent() {
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private class CardsAdapter extends ArrayAdapter<CardInfo> {
        public CardsAdapter(Context context) {
            super(context, 0, new ArrayList<CardInfo>());
            addAll(CardInfo.ALL_CARDS_ALPHABETICAL);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup group) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.supported_card, null);
            }

            CardInfo info = getItem(position);
            Spanned text = Html.fromHtml(String.format("<b>%s</b><br>%s", info.getName(),
                    getString(info.getLocationId())));

            ((ImageView) convertView.findViewById(R.id.image)).setImageResource(info.getImageId());
            ((TextView) convertView.findViewById(R.id.text)).setText(text);

            String notes = "";

            MainApplication app = MainApplication.getInstance();
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(app);
            boolean nfcAvailable = nfcAdapter != null;

            if (nfcAvailable) {
                if (info.getCardType() == CardType.MifareClassic && !app.getMifareClassicSupport()) {
                    // MIFARE Classic is not supported by this device.
                    notes += Utils.localizeString(R.string.card_not_supported_on_device) + " ";
                }

                if (info.getCardType() == CardType.CEPAS) {
                    // TODO: Implement feature detection for CEPAS like MIFARE Classic.
                    // TODO: It is probably exposed in hasSystemFeature().
                    notes += Utils.localizeString(R.string.card_note_cepas) + " ";
                }
            } else {
                // This device does not support NFC, so all cards are not supported.
                notes += Utils.localizeString(R.string.card_not_supported_on_device) + " ";
            }

            // Keys being required is secondary to the card not being supported.
            if (info.getKeysRequired()) {
                notes += Utils.localizeString(R.string.keys_required) + " ";
            }

            if (info.getPreview()) {
                notes += Utils.localizeString(R.string.card_preview_reader) + " ";
            }

            if (info.getResourceExtraNote() != 0) {
                notes += Utils.localizeString(info.getResourceExtraNote()) + " ";
            }

            ((TextView) convertView.findViewById(R.id.note)).setText(notes);


            return convertView;
        }
    }
}
