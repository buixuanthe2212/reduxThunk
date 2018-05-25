
package com.jinjerkeihi.scancard.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Base preference
 *
 * @author Pika Long
 */
public abstract class BasePreference {

    /**
     * Context
     */
    protected Context context;

    /**
     * SharedPreferences
     */
    private SharedPreferences preference;

    /**
     * Editor
     */
    private Editor editor;

    /**
     * Save the preferences
     */
    public synchronized void write() {
        editor = preference.edit();
        setData(editor);
        editor.commit();
    }

    /**
     * Initial
     */
    synchronized void init() {
        preference = context.getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE);
        editor = preference.edit();
        getData(preference);
    }
    
    /**
     * Get preference name
     *
     * @return Preference name
     */
    protected abstract String getPreferenceName();

    /**
     * Set data to be saved
     */
    protected abstract void setData(Editor editor);

    /**
     * Read data from preferences
     */
    protected abstract void getData(SharedPreferences preference);


}
