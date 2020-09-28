package id.co.kamil.autochat.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {


    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "KONTAXPREF";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_CUST_ID = "u_id"; //username
    public static final String KEY_CUST_GROUP = "username"; //username
    public static final String KEY_FIRSTNAME = "name"; //username
    public static final String KEY_LASTNAME = "nick_name"; //username
    public static final String KEY_EMAIL = "email"; //username
    public static final String KEY_PHONE = "phone"; //username
    public static final String KEY_TOKEN = "token"; // Token User
    public static final String KEY_CHILD = "child"; // is Child
    public static final String KEY_AFFILIATION = "affiliation";
    public static final String KEY_PARENT_ID = "key_parent_id";

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(String cust_id, String token, String firstname, String lastname, String email, String phone, String cust_group, boolean is_child, String parent_id) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_CUST_ID, cust_id);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_FIRSTNAME, firstname);
        editor.putString(KEY_LASTNAME, lastname);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_CUST_GROUP, cust_group);
        editor.putBoolean(KEY_CHILD, is_child);
        editor.putString(KEY_PARENT_ID, parent_id);
        editor.commit();
    }

    public void setToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.commit();
    }

    public void setKeyCustGroup(String id) {
        editor.putString(KEY_CUST_GROUP, id);
        editor.commit();
    }

    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {

        HashMap<String, String> user = new HashMap<String, String>();

        user.put(KEY_CUST_ID, pref.getString(KEY_CUST_ID, null));
        user.put(KEY_TOKEN, pref.getString(KEY_TOKEN, null));
        user.put(KEY_FIRSTNAME, pref.getString(KEY_FIRSTNAME, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(KEY_LASTNAME, pref.getString(KEY_LASTNAME, null));
        user.put(KEY_PHONE, pref.getString(KEY_PHONE, null));
        user.put(KEY_CUST_GROUP, pref.getString(KEY_CUST_GROUP, null));
        user.put(KEY_CHILD, String.valueOf(pref.getBoolean(KEY_CHILD, false)));
        user.put(KEY_PARENT_ID, String.valueOf(pref.getString(KEY_PARENT_ID, "")));
        return user;
    }

    public String getValue(String key) {
        return pref.getString(key, "");
    }

    public void setValue(String key, String val) {
        editor.putString(key, val);
        editor.commit();
    }

    public void clearData() {
        editor.clear();
        editor.commit();
        String affiliation = pref.getString(KEY_AFFILIATION, "");
        editor.putBoolean(IS_LOGIN, false);
        editor.putString(KEY_CUST_ID, "");
        editor.putString(KEY_TOKEN, "");
        editor.putString(KEY_AFFILIATION, affiliation);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

}
