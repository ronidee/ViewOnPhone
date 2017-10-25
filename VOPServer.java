package ronidea.viewonphone;


import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;


/**
 * TCP server that accepts links and openes them
 */

public class VOPServer extends Service {
    final static String REQUEST_DISCONNECT = "RQ_DC";

    TCPServer server;
    private static boolean isRunning = false;
    Thread serverThread;
    PhoneUnlockedReceiver unlockListener;

    @Override
    public void onCreate() {
        Log.d("VOPServer", "onCreate()");

        unlockListener = new PhoneUnlockedReceiver(this);
        serverThread = new Thread(r_server);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d("VOPServer", "onStartCommand()");
        new Prefs(this);

        // the server could maybe started twice accidentally. So make sure
        // the thread is only launched once.
        if (isRunning) {
            stopSelf();
            return START_STICKY;
        }

        serverThread.start();
        isRunning = true;
        registerReceiver(unlockListener, new IntentFilter("android.intent.action.USER_PRESENT"));


        VOPUtils.disableNougatFileRestrictions();
        return START_STICKY;
    }


    private Runnable r_server = new Runnable() {
        @Override
        public void run() {
            String input;
            server = new TCPServer();

            while (isRunning) {

                try {

                    /*        RECEIVING INPUT       */
                    server.waitForClient();             // wait for a client to connect
                    if (serverThread.isInterrupted()) break; // if thread is interrupted exit loop
                    input = server.getInputLine();      // get the input from that client
                    input = VOPUtils.decrypt(input);    // decrypt the input

                    /*      HANDLING THE INPUT      */
                    // shortest possible input: $PW$1234€PW€$URL$www.x.x€URL€ (29 chars)
                    // so this acts as an early filter
                    if (input != null && input.length() >= 29) {
                        processInput(input);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void processInput(String input) {
        // create dropping object
        Dropping dropping;
        // generate dropping object from server input
        dropping = VOPUtils.unwrapDelivery(input);
        Log.d("VOPServer r_server", "dropped: " + dropping.getURL());

        // validate password
        if (dropping.getPw().equals(Prefs.getPassword())) {

            // save the clients IPAddress for both-side communication
            Prefs.saveString(Prefs.KEY_IP_ADR, server.getConnectedIPAddress());

            switch (dropping.getType()) {

                // TYPE_NULL = input from computer wasn't valid
                case Dropping.TYPE_NULL:
                    Log.d("VOPServer", "input invalid: " + input);
                    break;

                // TYPE_URL = an URL was sent to be opened on the phone
                case Dropping.TYPE_URL:
                    VOPUtils.openUrl(dropping.getURL(), this);
                    break;

                // TYPE_IMG = an IMG was sent to be opened on the phone
                case Dropping.TYPE_IMG:
                    String filepath = VOPUtils.saveImage(dropping.getImage());
                    VOPUtils.openImg(filepath, this);
                    break;
            }
        }
    }


    // get the status of this service
    static boolean isRunning() {
        return isRunning;
    }


    @Override
    public IBinder onBind(Intent intent) {
        //We don't want to bind
        return null;
    }
    @Override
    public void onDestroy() {
        Log.d("VOPServer", "closed");
        unregisterReceiver(unlockListener); // unregister the unlockListener
        serverThread.interrupt();           // stopping the server manually (just in case ...)
        server.close();                     // close the server
        isRunning = false;                  // updating the service's status
    }
}