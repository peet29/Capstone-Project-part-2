package me.hanthong.capstone;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.hanthong.capstone.sync.SyncUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SyncUtils.CreateSyncAccount(this);

        SyncUtils.TriggerRefresh();
    }
}
