package ronidea.viewonphone;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;

/**
 * Store stuff
 */

public class Prefs {
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor editor;


    static final String KEY_PASSWORD = "password";
    static final String KEY_IP_ADR   = "ip_address";


    Prefs(Context context) {
        prefs = context.getSharedPreferences("generalPrefs", 0);
    }

    static String getString(String key) {
        return prefs.getString(key, "");
    }

    static void saveString(String key, String value) {
        editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    static String getPassword() {

        String pw = prefs.getString(KEY_PASSWORD, "");
        if (pw.equals("")) {

            Random random = new Random();
            for (int i = 0; i < 6; i++) {
                pw += random.nextInt(10);
            }

            editor = prefs.edit();
            editor.putString(KEY_PASSWORD, pw);
            editor.apply();
        }
        return pw;
    }

}
