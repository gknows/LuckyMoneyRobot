package com.github.gknows.luckymoneyrobot;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.WindowManager.LayoutParams;

public class LightUp extends Activity {
    WaitForLight wFL;
    boolean running;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED|LayoutParams.FLAG_TURN_SCREEN_ON|LayoutParams.FLAG_DISMISS_KEYGUARD);
        wFL = new WaitForLight();
        wFL.execute();
        running = true;
    }

    @Override
    protected void onNewIntent(Intent intent){
        if( wFL != null && running){
            running = false;
            wFL.cancel(true);
            finish();
            startActivity(intent);
        }else
            super.onNewIntent(intent);
    }

    private class WaitForLight extends AsyncTask<Void,Void,Boolean>{
        @Override
        protected Boolean doInBackground(Void... params) {
            long curTime = System.currentTimeMillis();
            while( !((PowerManager)getSystemService(POWER_SERVICE)).isScreenOn() ){
                if( System.currentTimeMillis() - curTime > 999 ){
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result){
            finish();
            if( !result )
                startActivity(getIntent());
        }
    }
}
