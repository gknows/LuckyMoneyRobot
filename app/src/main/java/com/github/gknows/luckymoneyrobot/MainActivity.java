package com.github.gknows.luckymoneyrobot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


public class MainActivity extends Activity {
    Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonOpenService).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openService();
            }
        });

        findViewById(R.id.buttonSetDelay).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setDelayTime();
            }
        });

        prefs = new Prefs(this);

        initMainView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.main_menu_about) {
            ViewGroup about = (ViewGroup) ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.main_menu_about, null);
            ((TextView)about.getChildAt(0)).setMovementMethod(LinkMovementMethod.getInstance());
            new AlertDialog.Builder(this).setView(about).setTitle(R.string.main_menu_about_title)
                    .setPositiveButton(R.string.main_menu_about_ok_button, null).show();
            return true;
        }

        if (id == R.id.main_menu_register) {
            final View view = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.main_menu_register, null);
            final EditText editSerialNum = (EditText)view.findViewById(R.id.main_menu_register_number);
            editSerialNum.setText(prefs.getDeviceNum());
            final TextView textView = (TextView)view.findViewById(R.id.main_menu_register);
            if(!prefs.isRegisterd()) {
                textView.setText(R.string.main_menu_register_try);
            }
            new AlertDialog.Builder(this).setView(view).setPositiveButton(R.string.main_menu_register_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (prefs.setSerialNum(editSerialNum.getText().toString())) {
                        Toast.makeText(MainActivity.this, R.string.main_menu_register_success, Toast.LENGTH_LONG).show();
                    }
                }
            }).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initMainView() {
        EditText editTextDelay = (EditText)findViewById(R.id.editTextDelay);
        editTextDelay.setText(""+prefs.getDelayTimems());
    }
	
    private void openService() {
        try{
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setDelayTime() {
        int delayTime =  Integer.parseInt(((EditText)findViewById(R.id.editTextDelay)).getText().toString());
        prefs.setDelayTimems(delayTime);
    }
}
