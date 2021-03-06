package com.microsoft.azure.mobile.sasquatch.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.azure.mobile.MobileCenter;
import com.microsoft.azure.mobile.analytics.Analytics;
import com.microsoft.azure.mobile.crashes.AbstractCrashesListener;
import com.microsoft.azure.mobile.crashes.Crashes;
import com.microsoft.azure.mobile.crashes.ErrorAttachments;
import com.microsoft.azure.mobile.crashes.model.ErrorAttachment;
import com.microsoft.azure.mobile.crashes.model.ErrorReport;
import com.microsoft.azure.mobile.sasquatch.R;
import com.microsoft.azure.mobile.sasquatch.features.TestFeatures;
import com.microsoft.azure.mobile.sasquatch.features.TestFeaturesListAdapter;


public class MainActivity extends AppCompatActivity {

    static final String APP_SECRET = "45d1d9f6-2492-4e68-bd44-7190351eb5f3";
    static final String APP_SECRET_KEY = "appSecret";
    static final String SERVER_URL_KEY = "serverUrl";

    private static final String LOG_TAG = "MobileCenterSasquatch";

    static SharedPreferences sSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sSharedPreferences = getSharedPreferences("Sasquatch", Context.MODE_PRIVATE);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().build());

        String serverUrl = sSharedPreferences.getString(SERVER_URL_KEY, null);
        if (serverUrl != null) {
            MobileCenter.setServerUrl(serverUrl);
        }
        MobileCenter.setLogLevel(Log.VERBOSE);
        Crashes.setListener(getCrashesListener());
        MobileCenter.start(getApplication(), getAppSecret(), Analytics.class, Crashes.class);

        Log.i(LOG_TAG, "Crashes.hasCrashedInLastSession=" + Crashes.hasCrashedInLastSession());
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                ErrorReport lastSessionCrashReport = Crashes.getLastSessionCrashReport();
                if (lastSessionCrashReport != null) {
                    Log.i(LOG_TAG, "Crashes.getLastSessionCrashReport().getThrowable()=", lastSessionCrashReport.getThrowable());
                }
                return null;
            }
        }.execute();

        ((TextView) findViewById(R.id.package_name)).setText(String.format(getString(R.string.sdk_source_format), getPackageName().substring(getPackageName().lastIndexOf(".") + 1)));
        TestFeatures.initialize(this);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(new TestFeaturesListAdapter(TestFeatures.getAvailableControls()));
        listView.setOnItemClickListener(TestFeatures.getOnItemClickListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return true;
    }

    private String getAppSecret() {
        String appSecret = sSharedPreferences.getString(APP_SECRET_KEY, null);
        if (appSecret == null) {
            SharedPreferences.Editor editor = sSharedPreferences.edit();
            editor.putString(APP_SECRET_KEY, APP_SECRET);
            editor.apply();
            appSecret = sSharedPreferences.getString(APP_SECRET_KEY, null);
        }
        Toast.makeText(this, String.format(getString(R.string.app_secret_toast), appSecret), Toast.LENGTH_SHORT).show();
        return appSecret;
    }

    private AbstractCrashesListener getCrashesListener() {
        return new AbstractCrashesListener() {
            @Override
            public boolean shouldAwaitUserConfirmation() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder
                        .setTitle(R.string.crash_confirmation_dialog_title)
                        .setMessage(R.string.crash_confirmation_dialog_message)
                        .setPositiveButton(R.string.crash_confirmation_dialog_send_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Crashes.notifyUserConfirmation(Crashes.SEND);
                            }
                        })
                        .setNegativeButton(R.string.crash_confirmation_dialog_not_send_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Crashes.notifyUserConfirmation(Crashes.DONT_SEND);
                            }
                        })
                        .setNeutralButton(R.string.crash_confirmation_dialog_always_send_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Crashes.notifyUserConfirmation(Crashes.ALWAYS_SEND);
                            }
                        });
                builder.create().show();
                return true;
            }

            @Override
            public ErrorAttachment getErrorAttachment(ErrorReport report) {
                return ErrorAttachments.attachment("This is a text attachment.", "This is a binary attachment.".getBytes(), "binary.txt", "text/plain");
            }

            @Override
            public void onBeforeSending(ErrorReport report) {
                Toast.makeText(MainActivity.this, R.string.crash_before_sending, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSendingFailed(ErrorReport report, Exception e) {
                Toast.makeText(MainActivity.this, R.string.crash_sent_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSendingSucceeded(ErrorReport report) {

                @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
                String message = String.format("%s\nCrash ID: %s\nThrowable: %s", R.string.crash_sent_succeeded, report.getId(), report.getThrowable().toString());
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        };
    }
}
