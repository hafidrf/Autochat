package id.co.kamil.autochat.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharPref {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "SHARPREFAUTOCHAT";
    public static final String STATUS_FLOATING_WIDGET = "status_floating_widget";
    public static final String STATUS_SCREEN_ALWAYS_ON = "status_screen_always_on";
    public static final String STATUS_FOREGROUND_SERVICE = "status_foreground_service";
    public static final String STATUS_SYNC_SERVICE = "status_sync_service";
    public static final String STATUS_BULK_SENDER = "status_bulk_sender";
    public static final String STATUS_ERROR_TRY_AGAIN = "status_error_try_agian";
    public static final String STATUS_BULK_SENDING = "STATUS_BULK_SENDING";
    public static final String BULK_SENDER_ON_SCREEN = "bulk_sender_on_screen";
    public static final String STATUS_TOOLBAR = "enabled_toolbar";
    public static final String STATUS_AUTOTEXT = "enabled_auto_text";
    public static final String DELAY_BULK_SENDER = "delay_bulk_sender";
    public static final String TRY_AGAIN_BULKSENDER = "TRY_AGAIN_BULKSENDER";
    public static final String LINK_TIMWABOT = "LINK_TIMWABOT";
    public static final String LINK_ECOURSE = "LINK_ECOURSE";
    public static final String LINK_TUTORIAL = "LINK_TUTORIAL";
    public static final String LINK_MARKETING_TOOL = "LINK_MARKETING_TOOL";
    public static final String LINK_AKUN = "LINK_AKUN";
    public static final String LINK_PULSA = "LINK_PULSA";
    public static final String LINK_KURIR = "LINK_KURIR";
    public static final String AUTOREPLY_PERSONAL = "AUTOREPLY_PERSONAL";
    public static final String AUTOREPLY_BUSINESS = "AUTOREPLY_BUSINESS";
    public static final String SYNC_CONTACT_WABOT = "SYNC_CONTACT_WABOT";
    public static final String DIR_IMAGE = "DIR_IMAGE";

    public static final String KEY_LIMIT_WAFORM_BASIC = "KEY_LIMIT_WAFORM_BASIC";
    public static final String KEY_LIMIT_WAFORM_PREMIUM = "KEY_LIMIT_WAFORM_PREMIUM";
    public static final String KEY_LIMIT_LINKPAGE_BASIC = "KEY_LIMIT_LINKPAGE_BASIC";
    public static final String KEY_LIMIT_LINKPAGE_PREMIUM = "KEY_LIMIT_LINKPAGE_PREMIUM";
    public static final String KEY_LIMIT_TEMPLATE_BASIC = "KEY_LIMIT_TEMPLATE_BASIC";
    public static final String KEY_LIMIT_TEMPLATE_PREMIUM = "KEY_LIMIT_TEMPLATE_PREMIUM";


    public static final String KEY_LIMIT_KONTAK = "KEY_LIMIT_KONTAK";
    public static final String KEY_LIMIT_PESAN = "KEY_LIMIT_PESAN";
    public static final String KEY_LIMIT_SHORTEN = "KEY_LIMIT_SHORTEN";
    public static final String KEY_LIMIT_AUTO_REPLY = "KEY_LIMIT_AUTO_REPLY";
    public static final String KEY_LIMIT_LEAD_MAGNET_BASIC = "KEY_LIMIT_LEAD_MAGNET_BASIC";
    public static final String KEY_LIMIT_LEAD_MAGNET_PREMIUM = "KEY_LIMIT_LEAD_MAGNET_PREMIUM";

    public static final String REFERRER_URL = "REFERRER_URL";

    // Constructor
    public SharPref(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createSession(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void createSession(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void createSession(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public void createSession(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public void createSession(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public boolean getSessionBool(String key) {
        return pref.getBoolean(key, false);
    }

    public String getSessionStr(String key) {
        return pref.getString(key, "");
    }

    public int getSessionInt(String key) {
        return pref.getInt(key, 0);
    }

    public long getSessionLong(String key) {
        return pref.getLong(key, 0);
    }

    public float getSessionFloat(String key) {
        return pref.getFloat(key, 0);
    }

    public void clearSession() {
        editor.clear();
        editor.commit();
    }

}
