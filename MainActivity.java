package ronidea.viewonphone;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {

    TextView    tv_ip;
    TextView    tv_pw;
    Switch      sw_power;
    EditText    et_url;
    ImageButton ib_send;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        new Prefs(this);
        initViews();
        createListeners();
        updateIp();
        tv_pw.setText("password: " + Prefs.getPassword());
        sw_power.setChecked(VOPServer.isRunning());

    }


    private void updateIp() {
        tv_ip.setText("ip address: " + VOPUtils.getOwnIpAddress(this));
    }

    public void touchTheSwitch(View v) {
        sw_power.setChecked(!sw_power.isChecked());
    }

    public void debug_method(View v) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        String txt = VOPUtils.encrypt("PENIS");
        Log.i("Mainactivity", "PENIS encrypted="+txt);
    }




    private void initViews() {
        tv_ip       = findViewById(R.id.tv_ip);
        tv_pw       = findViewById(R.id.tv_pw);
        sw_power    = findViewById(R.id.sw_power);
        ib_send     = findViewById(R.id.ib_send);
        et_url      = findViewById(R.id.et_url);
    }

    private void createListeners() {
        sw_power.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b)
                    startBackgroundService();
                else
                    stopBackgroundService();

            }
        });

        ib_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TCPClient.send(VOPUtils.wrapForDelivery(et_url.getText().toString(), false, 0));
                et_url.setText("http://www.wikipedia.org");
                et_url.setSelection(et_url.getText().length());
            }
        });
    }

    private void startBackgroundService() {
        startService(new Intent(this, VOPServer.class));
    }
    private void stopBackgroundService() {
        stopService(new Intent(this, VOPServer.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity", "onStop()");
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause()");
    }
    @Override
    protected void onDestroy() {
        Log.d("MainActivity", "onDestroy()");
        super.onDestroy();
    }
}
