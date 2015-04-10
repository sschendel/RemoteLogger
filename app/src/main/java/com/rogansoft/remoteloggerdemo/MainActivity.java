package com.rogansoft.remoteloggerdemo;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.rogansoft.remotelogger.RemoteLogger;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button) findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoteLogger.appendLog(MainActivity.this, "Button clicked!");
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // Set logging menu item to proper text based on logging state
        MenuItem logStartStop = menu.findItem(R.id.menu_opt_log_startstop);
        if(RemoteLogger.isLogging(this)) {
            logStartStop.setTitle("Stop logging");
        } else {
            logStartStop.setTitle("Start logging");
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result;
        int id = item.getItemId();

        switch(id) {
            case R.id.menu_opt_log_startstop:
                if (!RemoteLogger.toggleLogging(this)) {
                    RemoteLogger.launchSendLogWithAttachment(this);
                    item.setTitle("Start logging");
                } else {
                    item.setTitle("Stop logging");
                }
                result = true;
                break;
            case R.id.menu_opt_deletelog:
                RemoteLogger.deleteLog(this);
                Toast toast = Toast.makeText(this, "Log file deleted.", Toast.LENGTH_SHORT);
                toast.show();
                result = true;
                break;
            default:
                result = super.onOptionsItemSelected(item);

        }
        return result;
    }
}
