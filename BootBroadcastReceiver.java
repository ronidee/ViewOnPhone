package ronidea.viewonphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by roni on 12.08.17.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // BOOT_COMPLETED start Service
        if (intent.getAction().equals(ACTION)) {
            context.startService(new Intent(context, VOPServer.class));
        }
    }
}
