package ronidea.viewonphone;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class PhoneUnlockedReceiver extends BroadcastReceiver {
    // if a URL is sent to the phone while phone is locked,
    // this pending URL will be stored in this variable.
    private static String pendingURL = null;
    private Context c = null;

    public PhoneUnlockedReceiver(Context c) {
        Log.d("PhoneUnlockedReceiver", "erstelt");
        this.c = c;
    }

    // default Constructor
    public PhoneUnlockedReceiver() {
    }

    static void setPendingURL(String url) {
        pendingURL = url;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("lol", "onReceive()");

        KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.isKeyguardSecure()) {
            if (pendingURL != null) {
                c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(pendingURL)));
                pendingURL = null;
                Log.d("PhoneUnlockedReceiver", "pendingURL = " + pendingURL);
            }


        }
    }
}

