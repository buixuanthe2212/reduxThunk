package com.jinjerkeihi.scancard.preference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Save information user
 *
 * @author XuanThe.
 */
public class JinjerKeihiPreference extends BasePreference {

    /**
     * Preference name
     */
    private static final String PREFERENCE_NAME = "encryption_preference";
    /**
     * Token key
     */
    private static final String KEY_TOKEN = "token";

    public String token;

    public JinjerKeihiPreference(Context context) {
        super();
        this.context = context;
        init();
    }

    @Override
    protected String getPreferenceName() {
        return PREFERENCE_NAME;
    }

    @Override
    protected void setData(SharedPreferences.Editor editor) {
        editor.putString(KEY_TOKEN, token);
    }

    @Override
    protected void getData(SharedPreferences preference) {
        token = preference.getString(KEY_TOKEN, "");
    }

}
