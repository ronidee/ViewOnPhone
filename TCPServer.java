package ronidea.viewonphone;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.io.IOException;
import java.net.Socket;


/**
 * It's cool
 */

class TCPServer {
    private static ServerSocket server;
    private static Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private static final int PORT = 6321; // no reason, just a random number

    TCPServer() {
        start();
    }


    private void start() {
        Log.d("TCPServer","start()");
        try {
            server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void send (String outputLine) {
        out.println(outputLine);
        close();
    }


    boolean waitForClient() {
        Log.d("TCPServer","waitForClient()");
        // firstly: get a client
        client = null;
        try {
            client = server.accept();
        } catch (IOException e) {
            client = null;
            e.printStackTrace();
            return false;
        }

        // return false if client did not connect successfully
        if (!client.isConnected()) {
            return false;
        }

        // secondly: put a Reader and Writer onto the newly connected client
        try {
            in  = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
            out = new PrintWriter   (client.getOutputStream (), true);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    String getInputLine() {
        Log.d("TCPServer","getInputLine()");
        String inputLine = null;
        try {
            inputLine = "" + in.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return inputLine;
    }

    String getConnectedIPAddress() {
        return client.getInetAddress().toString().replace("/", "");
    }

    void close() {
        Log.d("TCPServer", "close()");
        try {
            server.close();
            server = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}