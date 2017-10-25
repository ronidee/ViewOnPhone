package ronidea.viewonphone;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * TCP client to send data to a TCP server running
 * on the related server.
 */


class TCPClient {

    private static Socket          client;
    private static PrintWriter     out;
    private static BufferedReader  in;
    private static final int PORT = 7492; // no reason, just a random number

    TCPClient() {}

    static void send(final String link) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String ip = Prefs.getString(Prefs.KEY_IP_ADR);


                // connect client with server
                try {
                    client = null;
                    client = new Socket(ip, PORT);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }


                // send link to server
                try {
                    out = new PrintWriter(client.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                out.println(link);
                out.close();
                Log.d("TCPClient, send: ", link);

            }
        }).start();

        Log.d("tcpclient", "gesendet");

    }




    static String getInputLine() {
        if (client == null) {
            return null;
        }
        in = null;
        String inputLine = "nothing";
        try {

            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            inputLine = in.readLine();

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }


        return inputLine;
    }
}