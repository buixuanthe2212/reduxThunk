package com.jinjerkeihi.scancard;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.jinjerkeihi.BuildConfig;
import com.jinjerkeihi.MainApplication;
import com.jinjerkeihi.R;
import com.jinjerkeihi.nfcfelica.card.Card;
import com.jinjerkeihi.nfcfelica.card.TagReaderFeedbackInterface;
import com.jinjerkeihi.nfcfelica.card.UnsupportedTagException;
import com.jinjerkeihi.nfcfelica.transit.CardInfo;
import com.jinjerkeihi.nfcfelica.transit.TransitData;
import com.jinjerkeihi.nfcfelica.util.Utils;
import com.jinjerkeihi.scancard.dialog.LoadingDialog;
import com.jinjerkeihi.scancard.dialog.ScanErrorDialog;
import com.jinjerkeihi.scancard.preference.JinjerKeihiPreference;
import com.jinjerkeihi.scancard.resultcard.ResultICCardActivity;

import java.util.GregorianCalendar;

/**
 * Dell on 3/16/2018.
 */

public class ScanICCardActivity extends AppCompatActivity implements TagReaderFeedbackInterface, ScanErrorDialog.IListener {
    private PendingIntent mPendingIntent;
    private NfcAdapter mNfcAdapter;
    private String[][] mTechLists = new String[][]{
            new String[]{IsoDep.class.getName()},
            new String[]{MifareClassic.class.getName()},
            new String[]{MifareUltralight.class.getName()},
            new String[]{NfcF.class.getName()}
    };
    private Card mCard;
    private TransitData mTransitData;
    private TextView mTvTitle, mTvDescription;
    private ImageView mIvBack;
    private LoadingDialog mLoadingDialog;
    private String mIdCard;
    private JinjerKeihiPreference mPreference;

    @Override
    public void updateStatusText(final String msg) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ic_card);
        mPreference = new JinjerKeihiPreference(getApplicationContext());
        mPreference.token = getIntent().getStringExtra("TOKEN");
        mPreference.write();
        findViewById();
        mLoadingDialog = new LoadingDialog(this);
        mTvTitle.setText(getString(R.string.title_start_card));

        addListener();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter != null) {
            Utils.checkNfcEnabled(this, mNfcAdapter);

            Intent intent = new Intent(this, ScanICCardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            mPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        }

        resolveIntent(getIntent());

        updateObfuscationNotice(mNfcAdapter != null);
    }

    private void findViewById() {
        mTvTitle = findViewById(R.id.tvTitle);
        mIvBack = findViewById(R.id.ivBack);
        mTvDescription = findViewById(R.id.tv_description);
    }

    private void addListener() {
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        try {
            final Tag tag = intent.getParcelableExtra("android.nfc.extra.TAG");
            final byte[] tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String lastReadId = prefs.getString(MainApplication.PREF_LAST_READ_ID, "");
            long lastReadAt = prefs.getLong(MainApplication.PREF_LAST_READ_AT, 0);

            // Prevent reading the same card again right away.
            // This was especially a problem with FeliCa cards.

            if (Utils.getHexString(tagId).equals(lastReadId) && (GregorianCalendar.getInstance().getTimeInMillis() - lastReadAt) < 5000) {
                finish();
                return;
            }
            mLoadingDialog.show();
            ReadingTagTask t = new ReadingTagTask();
            ReadingTagTaskEventArgs a = new ReadingTagTaskEventArgs(tagId, tag);
            t.execute(a);

        } catch (Exception ex) {
        }
    }

    @Override
    public void updateProgressBar(int progress, int max) {

    }

    @Override
    public void showCardType(final CardInfo cardInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AccessibilityManager man = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
                if (man != null && man.isEnabled()) {
                    AccessibilityEvent e = AccessibilityEvent.obtain();
                    e.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                    man.sendAccessibilityEvent(e);
                }
            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    class ReadingTagTaskEventArgs {
        byte[] tagId;
        Tag tag;

        public ReadingTagTaskEventArgs(byte[] tagId, Tag tag) {
            this.tagId = tagId;
            this.tag = tag;
        }
    }


    class ReadingTagTask extends AsyncTask<ReadingTagTaskEventArgs, String, Uri> {

        private Exception mException;
        private boolean isNoData = false;

        @Override
        protected Uri doInBackground(ReadingTagTaskEventArgs... params) {
            ReadingTagTaskEventArgs a = params[0];
            try {
                Card card = Card.dumpTag(a.tagId, a.tag, ScanICCardActivity.this);

                String cardXml = card.toXml(MainApplication.getInstance().getSerializer());

                if (BuildConfig.DEBUG) {
                    Log.d("ReadingTagActivity", "Dumped card successfully!");
                    for (String line : cardXml.split("\n")) {
                        Log.d("ReadingTagActivity", "XML: " + line);
                    }
                }

                mCard = Card.fromXml(MainApplication.getInstance().getSerializer(), cardXml);
                byte[] id = mCard.getTagId();

                mIdCard = getHex(id);
                mTransitData = mCard.parseTransitData();

                return null;
            } catch (Exception ex) {
                mException = ex;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Uri cardUri) {
            mLoadingDialog.hide();
            if (mException == null) {
                Intent intent = new Intent(ScanICCardActivity.this, ResultICCardActivity.class);
                intent.putExtra("DATA", mTransitData);
                intent.putExtra("ID", mIdCard);
                startActivity(intent);
                return;
            }
            if (mException instanceof UnsupportedTagException) {
                UnsupportedTagException ex = (UnsupportedTagException) mException;
                new ScanErrorDialog(ScanICCardActivity.this, "", ScanICCardActivity.this).show();
            } else {
                new ScanErrorDialog(ScanICCardActivity.this, "", ScanICCardActivity.this).show();
            }

        }
    }

    @Override
    public void onCancel() {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateObfuscationNotice((mNfcAdapter != null));
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, mTechLists);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void updateObfuscationNotice(boolean hasNfc) {
        int obfuscationFlagsOn =
                (MainApplication.hideCardNumbers() ? 1 : 0) +
                        (MainApplication.obfuscateBalance() ? 1 : 0) +
                        (MainApplication.obfuscateTripDates() ? 1 : 0) +
                        (MainApplication.obfuscateTripFares() ? 1 : 0) +
                        (MainApplication.obfuscateTripTimes() ? 1 : 0);

        if (obfuscationFlagsOn > 0) {
            mTvDescription.setText(Utils.localizePlural(R.plurals.obfuscation_mode_notice,
                    obfuscationFlagsOn, obfuscationFlagsOn));
        } else if (!hasNfc) {
            mTvDescription.setText(R.string.nfc_unavailable);
            new AlertDialog.Builder(ScanICCardActivity.this)
                    .setMessage(getString(R.string.nfc_unavailable))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    })
                    .show();
        } else {
            mTvDescription.setText(R.string.tab_card);
        }
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }

        return sb.toString();
    }

}
