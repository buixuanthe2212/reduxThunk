package com.jinjerkeihi;

import android.app.Application;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import com.facebook.react.ReactApplication;
import com.oblador.vectoricons.VectorIconsPackage;
import com.horcrux.svg.SvgPackage;
import com.AlexanderZaytsev.RNI18n.RNI18nPackage;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.jinjerkeihi.felica.lib.FeliCaLib;
import com.jinjerkeihi.nfcfelica.card.Card;
import com.jinjerkeihi.nfcfelica.card.CardType;
import com.jinjerkeihi.nfcfelica.card.classic.ClassicSector;
import com.jinjerkeihi.nfcfelica.card.desfire.files.DesfireFile;
import com.jinjerkeihi.nfcfelica.card.desfire.files.InvalidDesfireFile;
import com.jinjerkeihi.nfcfelica.card.desfire.files.RecordDesfireFile;
import com.jinjerkeihi.nfcfelica.card.desfire.settings.DesfireFileSettings;
import com.jinjerkeihi.nfcfelica.card.ultralight.UltralightPage;
import com.jinjerkeihi.nfcfelica.transit.lax_tap.LaxTapDBUtil;
import com.jinjerkeihi.nfcfelica.transit.ovc.OVChipDBUtil;
import com.jinjerkeihi.nfcfelica.transit.seq_go.SeqGoDBUtil;
import com.jinjerkeihi.nfcfelica.transit.suica.SuicaDBUtil;
import com.jinjerkeihi.nfcfelica.xml.Base64String;
import com.jinjerkeihi.nfcfelica.xml.CardConverter;
import com.jinjerkeihi.nfcfelica.xml.CardTypeTransform;
import com.jinjerkeihi.nfcfelica.xml.ClassicSectorConverter;
import com.jinjerkeihi.nfcfelica.xml.DesfireFileConverter;
import com.jinjerkeihi.nfcfelica.xml.DesfireFileSettingsConverter;
import com.jinjerkeihi.nfcfelica.xml.EpochCalendarTransform;
import com.jinjerkeihi.nfcfelica.xml.FelicaIDmTransform;
import com.jinjerkeihi.nfcfelica.xml.FelicaPMmTransform;
import com.jinjerkeihi.nfcfelica.xml.HexString;
import com.jinjerkeihi.nfcfelica.xml.SkippableRegistryStrategy;
import com.jinjerkeihi.nfcfelica.xml.UltralightPageConverter;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Type;
import org.simpleframework.xml.strategy.Visitor;
import org.simpleframework.xml.strategy.VisitorStrategy;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.NodeMap;
import org.simpleframework.xml.stream.OutputNode;
import org.simpleframework.xml.transform.RegistryMatcher;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class MainApplication extends Application implements ReactApplication {
  private static final String TAG = "MainApplication";
  public static final String PREF_LAST_READ_ID = "last_read_id";
  public static final String PREF_LAST_READ_AT = "last_read_at";
  public static final String PREF_MFC_AUTHRETRY = "pref_mfc_authretry";
  public static final String PREF_MFC_FALLBACK = "pref_mfc_fallback";

  public static final String PREF_HIDE_CARD_NUMBERS = "pref_hide_card_numbers";
  public static final String PREF_OBFUSCATE_TRIP_DATES = "pref_obfuscate_trip_dates";
  public static final String PREF_OBFUSCATE_TRIP_TIMES = "pref_obfuscate_trip_times";
  public static final String PREF_OBFUSCATE_TRIP_FARES = "pref_obfuscate_trip_fares";
  public static final String PREF_OBFUSCATE_BALANCE = "pref_obfuscate_balance";

  public static final String PREF_LOCALISE_PLACES = "pref_localise_places";

  private static MainApplication sInstance;

  private SuicaDBUtil mSuicaDBUtil;
  private OVChipDBUtil mOVChipDBUtil;
  private SeqGoDBUtil mSeqGoDBUtil;
  private LaxTapDBUtil mLaxTapDBUtil;
  private final Serializer mSerializer;
  private boolean mHasNfcHardware = false;
  private boolean mMifareClassicSupport = false;

  public MainApplication() {
    sInstance = this;

    mSuicaDBUtil = new SuicaDBUtil(this);
    mOVChipDBUtil = new OVChipDBUtil(this);
    mSeqGoDBUtil = new SeqGoDBUtil(this);
    mLaxTapDBUtil = new LaxTapDBUtil(this);

    try {
      Visitor visitor = new Visitor() {
        @Override
        public void read(Type type, NodeMap<InputNode> node) throws Exception {
        }

        @Override
        public void write(Type type, NodeMap<OutputNode> node) throws Exception {
          node.remove("class");
        }
      };
      Registry registry = new Registry();
      RegistryMatcher matcher = new RegistryMatcher();
      mSerializer = new Persister(new VisitorStrategy(visitor, new SkippableRegistryStrategy(registry)), matcher);

      DesfireFileConverter desfireFileConverter = new DesfireFileConverter(mSerializer);
      registry.bind(DesfireFile.class, desfireFileConverter);
      registry.bind(RecordDesfireFile.class, desfireFileConverter);
      registry.bind(InvalidDesfireFile.class, desfireFileConverter);

      registry.bind(DesfireFileSettings.class, new DesfireFileSettingsConverter());
      registry.bind(ClassicSector.class, new ClassicSectorConverter());
      registry.bind(UltralightPage.class, new UltralightPageConverter());
      registry.bind(Card.class, new CardConverter(mSerializer));

      matcher.bind(HexString.class, HexString.Transform.class);
      matcher.bind(Base64String.class, Base64String.Transform.class);
      matcher.bind(Calendar.class, EpochCalendarTransform.class);
      matcher.bind(GregorianCalendar.class, EpochCalendarTransform.class);
      matcher.bind(FeliCaLib.IDm.class, FelicaIDmTransform.class);
      matcher.bind(FeliCaLib.PMm.class, FelicaPMmTransform.class);
      matcher.bind(CardType.class, CardTypeTransform.class);

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
    @Override
    public boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
              new ActivityStarterReactPackage(), // This is it!
              new MainReactPackage(),
            new VectorIconsPackage(),
            new SvgPackage(),
            new RNI18nPackage()
      );
    }

    @Override
    protected String getJSMainModuleName() {
      return "index";
    }
  };

  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    SoLoader.init(this, /* native exopackage */ false);
    try {
      detectNfcSupport();
    } catch (Exception e) {
      Log.w(TAG, "Detecting nfc support failed", e);
    }

//        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build());

  }


  public static MainApplication getInstance() {
    return sInstance;
  }

  protected static boolean getBooleanPref(String preference, boolean default_setting) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstance());
    return prefs.getBoolean(preference, false);
  }

  /**
   * Returns true if the user has opted to hide card numbers in the UI.
   *
   * @return true if we should not show any card numbers
   */
  public static boolean hideCardNumbers() {
    return getBooleanPref(PREF_HIDE_CARD_NUMBERS, false);
  }

  public static boolean obfuscateTripDates() {
    return getBooleanPref(PREF_OBFUSCATE_TRIP_DATES, false);
  }

  public static boolean obfuscateTripTimes() {
    return getBooleanPref(PREF_OBFUSCATE_TRIP_TIMES, false);
  }

  public static boolean obfuscateTripFares() {
    return getBooleanPref(PREF_OBFUSCATE_TRIP_FARES, false);
  }

  public static boolean obfuscateBalance() {
    return getBooleanPref(PREF_OBFUSCATE_BALANCE, false);
  }

  public static boolean localisePlaces() {
    return getBooleanPref(PREF_LOCALISE_PLACES, false);
  }


  public OVChipDBUtil getOVChipDBUtil() {
    return mOVChipDBUtil;
  }

  public SuicaDBUtil getSuicaDBUtil() {
    return mSuicaDBUtil;
  }

  public LaxTapDBUtil getLaxTapDBUtil() {
    return mLaxTapDBUtil;
  }

  public SeqGoDBUtil getSeqGoDBUtil() {
    return mSeqGoDBUtil;
  }

  public boolean getMifareClassicSupport() {
    return mMifareClassicSupport;
  }

  public Serializer getSerializer() {
    return mSerializer;
  }

//  @Override
//  public void onCreate() {
//    super.onCreate();
//
//    try {
//      detectNfcSupport();
//    } catch (Exception e) {
//      Log.w(TAG, "Detecting nfc support failed", e);
//    }
//
////        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
//
//    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//            .detectAll()
//            .penaltyLog()
//            .build());
//
//  }

  private void detectNfcSupport() {
    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    mHasNfcHardware = nfcAdapter != null;

    if (!mHasNfcHardware) {
      Log.d(TAG, "Android reports no NFC adapter is available");
      return;
    }

    // TODO: Some devices report MIFARE Classic support, when they actually don't have it.
    //
    // Detecting based on libraries and device nodes doesn't work great either. There's edge
    // cases, and it's still vulnerable to vendors doing silly things.

    // Fallback: Look for com.nxp.mifare feature.
    mMifareClassicSupport = this.getPackageManager().hasSystemFeature("com.nxp.mifare");
    Log.d(TAG, "Falling back to com.nxp.mifare feature detection "
            + (mMifareClassicSupport ? "(found)" : "(missing)"));
  }


}

