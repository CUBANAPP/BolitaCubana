/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private boolean main;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        main = false;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            main = bundle.getBoolean("main");
        } else {
            main = false;
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(main);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private SwitchPreferenceCompat mpromoChannel;
        private SwitchPreferenceCompat mdefaultChannel;
        private final String TAG = "SettingsFragment";


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public void onConfigurationChanged(@NonNull Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            mpromoChannel = (SwitchPreferenceCompat) getPreferenceManager().findPreference("promoChannel");
            mdefaultChannel = (SwitchPreferenceCompat) getPreferenceManager().findPreference("defaulChannel");
            if (mdefaultChannel != null) {
                mdefaultChannel.setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) (preference, newValue) -> {

                    if (Build.VERSION.SDK_INT >= 19) {
                        FirebaseMessaging mFirebaseMessages = FirebaseMessaging.getInstance();
                        if (Objects.equals(preference.getKey(), "defaulChannel")) {
                            if (newValue.equals(false)) {
                                mFirebaseMessages.unsubscribeFromTopic("Default").addOnCompleteListener(task -> {
                                    String msg = getString(R.string.msg_subscribed);
                                    if (!task.isSuccessful()) {
                                        msg = getString(R.string.msg_subscribe_failed);
                                    }
                                    Log.d(TAG, msg);
                                    //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                mFirebaseMessages.subscribeToTopic("Default").addOnCompleteListener(task -> {
                                    String msg = getString(R.string.msg_subscribed);
                                    if (!task.isSuccessful()) {
                                        msg = getString(R.string.msg_subscribe_failed);
                                    }
                                    Log.d(TAG, msg);
                                    //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                        Log.d(TAG, "Config Changed: " + preference.getKey());
                        return true;
                    } else {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), R.string.sdk19, Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                });
            }
            if (mpromoChannel != null) {
                mpromoChannel.setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) (preference, newValue) -> {
                    if (Build.VERSION.SDK_INT >= 19) {
                        FirebaseMessaging mFirebaseMessages = FirebaseMessaging.getInstance();
                        if (Objects.equals(preference.getKey(), "promoChannel")) {
                            if (newValue.equals(false)) {
                                mFirebaseMessages.unsubscribeFromTopic("Promo").addOnCompleteListener(task -> {
                                    String msg = getString(R.string.msg_subscribed);
                                    if (!task.isSuccessful()) {
                                        msg = getString(R.string.msg_subscribe_failed);
                                    }
                                    Log.d(TAG, msg);
                                    //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                mFirebaseMessages.subscribeToTopic("Promo").addOnCompleteListener(task -> {
                                    String msg = getString(R.string.msg_subscribed);
                                    if (!task.isSuccessful()) {
                                        msg = getString(R.string.msg_subscribe_failed);
                                    }
                                    Log.d(TAG, msg);
                                    //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                        Log.d(TAG, "oOnfig Changed: " + preference.getKey());
                        return true;

                    } else {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), R.string.sdk19, Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                });
            }
        }
    }
}
