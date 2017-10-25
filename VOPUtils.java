package ronidea.viewonphone;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.webkit.URLUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static android.content.Context.WIFI_SERVICE;

/**
 * Some methods needed to do stuff
 */

class VOPUtils {
    private static final String TAG_URL_START = "$URL$";// url
    private static final String TAG_URL_END   = "€URL€";

    private static final String TAG_PW_START  = "$PW$"; // password
    private static final String TAG_PW_END    = "€PW€";

    private static final String TAG_FS_START  = "$FS$"; // fullscreen
    private static final String TAG_FS_END    = "€FS€";

    private static final String TAG_LT_START  = "$LT$"; // websites loading-time
    private static final String TAG_LT_END    = "€LT€";

    private static final String TAG_IMG_START = "$IMG$"; // image
    private static final String TAG_IMG_END   = "€IMG€";

    private static final String TAG_TYP_START = "$TYP$"; // type
    private static final String TAG_TYP_END   = "€TYP€";


    // Encrypting
    // checks if input is an email
    private static boolean isValidEmail(CharSequence email) {
        Log.d("VOPUtils","isValidEmail()");
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.d("isValidEmail", email + " is an email");
            return true;
        }
        Log.d("isValidEmail", email + " is not an email");
        return false;
    }




    /*
     * SECURITY
     */
    // generate secretkey  based on the password
    private static SecretKeySpec getSecretKey() {
        byte[] key;
        MessageDigest sha;

        try {
            key = (Prefs.getPassword()).getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            return null;
        }

        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, "AES");
    }

    static String encrypt(String text) {
        Log.d("VOPUtils","encrypt()");

        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] encrypted = cipher.doFinal(text.getBytes());


            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }

    static String decrypt(String text) {
        Log.d("VOPUtils","decrypt()");
        try {
            // convert base64 String to byte-array
            byte[] crypted = Base64.decode(text, Base64.DEFAULT);

            // decrypt
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] cipherData = cipher.doFinal(crypted);

            return new String(cipherData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // unwrap the password from the string like the VOPP wants it. ;)
    private static String  parsePassword(String input) {
        return VOPUtils.getStringBetween(input, TAG_PW_START, TAG_PW_END);
    }

    // validate content
    private static boolean isValidPassword(String pw) {

        if (pw == null || !pw.equals(Prefs.getPassword())) {
            Log.d("isValidPassword", pw + " is not valid");
            return false;
        }

        Log.d("isValidPassword", pw + " is valid");
        return true;
    }



    /*
     * FILE AND URL OPERATIONS
     */
    static String saveImage(final Bitmap finalBitmap) {
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/" + "Viewonphone";

        File myDir = new File(filepath);
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String filename = "Image-" + n + ".jpg";
        File file = new File(myDir, filename);
        if (file.exists()) file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    // open img
    static void openImg(final String path, final Context c) {
        MediaScannerConnection.scanFile(c,
                new String[] { path         },
                new String[] { "image/jpeg" },
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String s, Uri uri) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse("file://"+path), "image/*");
                        c.startActivity(intent);
                    }
                });



    }

    // open url with ext. app, or tmp save the url, if phone is locked
    static void openUrl(String url, Context c) {
        Log.d("VOPServer", "openURL()");
        KeyguardManager myKM = (KeyguardManager) c.getSystemService(Context.KEYGUARD_SERVICE);
        if (myKM.inKeyguardRestrictedInputMode()) {
            // it is locked
            PhoneUnlockedReceiver.setPendingURL(url);
            Log.d("VOPServer", "openURL(), setting pendingURL");
        } else {
            //it is not locked
            try {
                c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                Log.d("VOPServer", "openURL(), open URL");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // disable some API 24+ changes
    static void disableNougatFileRestrictions() {
        /*
        *   The following code disables the restriction that comes with
        *   Android 6 that doesn't allow to use Uri "file://" inside Intents.
        *   By that it's not required to use FileProvider
        *   (It's new, I don't know it, I don't like it...)
         */

        if (Build.VERSION.SDK_INT<24) {
            return;
        }

        try {
            (StrictMode.class.getMethod("disableDeathOnFileUriExposure")).invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    /*
     *  PARSING
     */
    // create output string in correct format
    static String wrapForDelivery(String url, boolean fs, int loadingTime) {
        String fullscreen = fs ? "1" : "0";
        Log.d("fullscreen= ", fullscreen);
        String wrapped =
                TAG_PW_START    + Prefs.getPassword()   + TAG_PW_END  +
                TAG_URL_START   + url 			        + TAG_URL_END +
                TAG_FS_START    + fullscreen            + TAG_FS_END  +
                TAG_LT_START    + loadingTime           + TAG_LT_END;
        Log.d("VOPUtils, wrap", wrapped);
        return wrapped;
    }

    static Dropping unwrapDelivery(String input) {
        /*
        *   This method generates a dropping object by parsing out
        *   the url or img, the password and setting the droppings type
         */

        Dropping dropping = new Dropping();

        // 1. Get the type
        dropping.setType(Integer.valueOf(getStringBetween(input, TAG_TYP_START, TAG_TYP_END)));

        if (dropping.getType() == Dropping.TYPE_IMG) {
            dropping.setImg(getStringBetween(input, TAG_IMG_START, TAG_IMG_END));
            return dropping;
        }

        if (dropping.getType() == Dropping.TYPE_URL) {
            dropping.setUrl(getStringBetween(input, TAG_URL_START, TAG_URL_END));
            return dropping;
        }

        if (dropping.getType() == Dropping.TYPE_EMAIL) {
            dropping.setUrl(
                    "mailto:?subject="
                    + getStringBetween(input, TAG_URL_START, TAG_URL_END)
                    + "&body=" + "");
            return dropping;
        }

        /*// if it could be a Url but the "http(s)://" is missing, add it,
        // using http:// as default, as some sites are not available in https and
        // most sites will automatically redirect the user from http to https
        if ( url.length() >= 7 && url.substring(0, 4).equals("www.") ) {
            url = "http://" + url;
        }

        // if the url isn't an email-address but a valid url, the browser or certain apps
        // will open (youtube e.g)
        if ( URLUtil.isValidUrl(url) ) {
            dropping.setText(url);
            dropping.setType(Dropping.TYPE_URL);
            return dropping;
        }*/

        Log.d("VOPUtils", "unwrapDelivery: not a valid url/email-address. Returning null!");
        return dropping;
    }

    // returns the string between two certain strings
    private static String  getStringBetween(String string, String a, String b) {
        int start, end;
        start   = string.indexOf(a) + a.length();
        end     = string.indexOf(b);

        if (start>=end) {
            Log.d("getStringBetween", "Error: start==end. Returning null!");
            return null;
        }
        return string.substring(start, end);
    }

    static String getOwnIpAddress(Context context) {
        WifiManager wifiManager =
                (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }
        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e("WIFIIP", "Unable to get host address.");
            ipAddressString = "not connected";
        }
        return ipAddressString;
    }

}
