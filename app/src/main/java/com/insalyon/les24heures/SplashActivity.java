package com.insalyon.les24heures;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.insalyon.les24heures.service.LiveUpdateService;
import com.insalyon.les24heures.service.impl.ApplicationVersionServiceImpl;
import com.insalyon.les24heures.service.impl.LiveUpdateServiceImpl;


public class SplashActivity extends Activity {
    private static final String TAG = SplashActivity.class.getCanonicalName();

    @Override
    //dans NavigationActivity sauf startRightOutput  
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        PackageInfo pInfo = null;
        try {
            pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ((TextView)findViewById(R.id.app_version)).setText(pInfo.versionName);

        new BackgroundSplashTask().execute();


        ApplicationVersionServiceImpl.getInstance().checkApplicationVersion(getApplicationContext());


    }

    private static final int SPLASH_SHOW_TIME = 200;

    /**
     * Async Task: can be used to load DB, images during which the splash screen
     * is shown to user
     */
    private class BackgroundSplashTask extends AsyncTask<Void, Void, Void> {



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            // I have just given a sleep for this thread
            // if you want to load database, make
            // network calls, load images
            // you can do them here and remove the following
            // sleep

            // do not worry about this Thread.sleep
            // this is an async task, it will not disrupt the UI
            try {
                Thread.sleep(SPLASH_SHOW_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //TODO choisir quel activtiy lancer en fonction de l'heure ici
            Intent i = new Intent(SplashActivity.this,
                    DayActivity.class);
            // any info loaded can during splash_show
            // can be passed to main activity using
            // below
            i.putExtra("loaded_info", " ");
            startActivity(i);
            finish();
        }

    }

}

