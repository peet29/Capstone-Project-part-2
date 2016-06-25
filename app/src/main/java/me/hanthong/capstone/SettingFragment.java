package me.hanthong.capstone;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import me.hanthong.capstone.data.NewsProvider;
import me.hanthong.capstone.sync.AuthenticatorService;


public class SettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equals(getString(R.string.pref_sync_key)))
        {
            String CONTENT_AUTHORITY = NewsProvider.AUTHORITY;
            String ACCOUNT_TYPE = "hanthong.me";
            Account account = AuthenticatorService.GetAccount(ACCOUNT_TYPE);

            String defSyncTime = getString(R.string.pref_title_sync_frequency_default);
            String SyncKey = getString(R.string.pref_sync_key);
            String syncTime = sharedPreferences.getString(SyncKey,defSyncTime);
            long SYNC_FREQUENCY = 60 * Long.valueOf(syncTime);

            ContentResolver.addPeriodicSync(
                    account, CONTENT_AUTHORITY, new Bundle(), SYNC_FREQUENCY);
            
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
