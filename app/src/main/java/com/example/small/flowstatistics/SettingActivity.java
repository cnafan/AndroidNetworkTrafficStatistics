package com.example.small.flowstatistics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.small.flowstatistics.R.xml.preferences;

/**
 * Created by small on 2016/11/16.
 */

public class SettingActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private EditTextPreference CheckEditTextPreference;
    private SwitchPreference ShowNotificationSwitchPreference;
    private SwitchPreference LogSwitchPreference;
    private SwitchPreference AutomaticCheckSwitchPreference;
    private ListPreference RemonthListPreference;


    private void initPreferences() {
        CheckEditTextPreference = (EditTextPreference) findPreference("check");
        ShowNotificationSwitchPreference = (SwitchPreference) findPreference("ShowNotification");
        AutomaticCheckSwitchPreference = (SwitchPreference) findPreference("AutomaticCheck");
        LogSwitchPreference = (SwitchPreference) findPreference("log");
        RemonthListPreference = (ListPreference) findPreference("remonth");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(preferences);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.activity_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("log")) {
            //Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            Log.d("qiang", "log change");
            try {
                writeFile(this,"log","");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        Log.d("qiang", "change");

    }

    //写数据
    void writeFile(Context context, String fileName, String writestr) throws IOException {
        try {/*
            BufferedWriter fout = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName, true)));
            /*** 追加文件：使用FileOutputStream，在构造FileOutputStream时，把第二个参数设为true

            fout.write(writestr);
            */

            FileOutputStream fout = context.openFileOutput(fileName, MODE_PRIVATE);
            byte[] bytes = writestr.getBytes();
            fout.write(bytes);

            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
