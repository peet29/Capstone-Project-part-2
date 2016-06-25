package me.hanthong.capstone;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

import me.hanthong.capstone.sync.SyncUtils;

public class MainActivity extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        SyncUtils.CreateSyncAccount(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean(getString(R.string.pref_sync_on_open_key),true)) {
            SyncUtils.TriggerRefresh();
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.VALUE, "app sync");
            mFirebaseAnalytics.logEvent("sync", bundle);
        }

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, "app open");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
    }
}
