/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana;

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

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
        private SwitchPreferenceCompat mnewyorkChannel;
        private SwitchPreferenceCompat mgeorgiaChannel;

        private ListPreference listPreference;

        private ListPreference erase;
        private ListPreference mdarkmode;

        private static final String TAG = "SettingsFragment";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            erase = (ListPreference) getPreferenceManager().findPreference("erase");

            listPreference = (ListPreference) getPreferenceManager().findPreference("languagepreference");
            mpromoChannel = (SwitchPreferenceCompat) getPreferenceManager().findPreference("promoChannel");
            mdefaultChannel = (SwitchPreferenceCompat) getPreferenceManager().findPreference("defaulChannel");
            mnewyorkChannel = (SwitchPreferenceCompat) getPreferenceManager().findPreference("newyorkChannel");
            mgeorgiaChannel = (SwitchPreferenceCompat) getPreferenceManager().findPreference("georgiaChannel");
            mdarkmode = (ListPreference) getPreferenceManager().findPreference("thememodeselector");
            if(erase != null){
                erase.setOnPreferenceChangeListener((preference, newvelue) -> {
                    boolean response = clearData();
                    if (getActivity() != null) {
                        if (response)
                            Toast.makeText(getActivity(), R.string.completed, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
                    }
                    return response;
                });
            }
            if(listPreference != null) {
                listPreference.setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) (preference, newValue) -> {
                    if(getActivity() != null) {
                        Locale config = new Locale(newValue.toString());
                        Locale.setDefault(config);
                        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(newValue.toString());
                        // Call this on the main thread as it may require Activity.restart()
                        AppCompatDelegate.setApplicationLocales(appLocale);
                    }
                    return true;
                });
            }
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
            if (mgeorgiaChannel != null) {
                mgeorgiaChannel.setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) (preference, newValue) -> {
                    if (Build.VERSION.SDK_INT >= 19) {
                        FirebaseMessaging mFirebaseMessages = FirebaseMessaging.getInstance();
                        if (Objects.equals(preference.getKey(), "georgiaChannel")) {
                            if (newValue.equals(false)) {
                                mFirebaseMessages.unsubscribeFromTopic("Georgia").addOnCompleteListener(task -> {
                                    String msg = getString(R.string.msg_subscribed);
                                    if (!task.isSuccessful()) {
                                        msg = getString(R.string.msg_subscribe_failed);
                                    }
                                    Log.d(TAG, msg);
                                    //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                mFirebaseMessages.subscribeToTopic("Georgia").addOnCompleteListener(task -> {
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
            if (mnewyorkChannel != null) {
                mnewyorkChannel.setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) (preference, newValue) -> {
                    if (Build.VERSION.SDK_INT >= 19) {
                        FirebaseMessaging mFirebaseMessages = FirebaseMessaging.getInstance();
                        if (Objects.equals(preference.getKey(), "newyorkChannel")) {
                            if (newValue.equals(false)) {
                                mFirebaseMessages.unsubscribeFromTopic("NewYork").addOnCompleteListener(task -> {
                                    String msg = getString(R.string.msg_subscribed);
                                    if (!task.isSuccessful()) {
                                        msg = getString(R.string.msg_subscribe_failed);
                                    }
                                    Log.d(TAG, msg);
                                    //Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                mFirebaseMessages.subscribeToTopic("NewYork").addOnCompleteListener(task -> {
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
            if (mdarkmode != null) {
                mdarkmode.setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) (preference, newValue) -> {
                    if (getActivity() != null) {

                        if (Build.VERSION.SDK_INT >= 17) {
                            if (Objects.equals(newValue, "light")) {
                                if (Build.VERSION.SDK_INT >= 31) {
                                    UiModeManager uiManager = (UiModeManager) requireActivity().getSystemService(Context.UI_MODE_SERVICE);

                                    //uiManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
                                    uiManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
                                } else {
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                }
                                return true;
                            } else if (Objects.equals(newValue, "dark")) {
                                if (Build.VERSION.SDK_INT >= 31) {
                                    UiModeManager uiManager = (UiModeManager) requireActivity().getSystemService(Context.UI_MODE_SERVICE);

                                    //uiManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
                                    uiManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
                                } else {
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                }
                                return true;
                            } else if (Objects.equals(newValue, "system")) {
                                if (Build.VERSION.SDK_INT >= 31) {
                                    UiModeManager uiManager = (UiModeManager) requireActivity().getSystemService(Context.UI_MODE_SERVICE);

                                    //uiManager.setNightMode(UiModeManager.MODE_NIGHT_AUTO);
                                    uiManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO);
                                } else {
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                                }
                                return true;
                            } else
                                return false;

                        } else {
                            if (getActivity() != null) {
                                Toast.makeText(getActivity(), R.string.sdk17, Toast.LENGTH_LONG).show();
                            }
                            return false;
                        }
                    }
                    else
                        return false;
                });
            }
        }
        @SuppressLint("ApplySharedPref")
        public boolean clearData(){
            if(requireActivity().getCacheDir() != null) {
                deleteCache(requireActivity());
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(
                        getString(R.string.preference_file_key2), Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.clear();

                TimeZone tz = TimeZone.getTimeZone("America/New_York");
                TimeZone.setDefault(tz);

                Calendar fecha = Calendar.getInstance(TimeZone.getTimeZone(TimeZone.getDefault().getID()), Locale.US);

                fecha.add(Calendar.SECOND, 5);

                edit.putLong("checkUpdateImages", fecha.getTimeInMillis());
                edit.commit();
                SharedPreferences sharedPreferences2 = requireActivity().getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor edit2 = sharedPreferences2.edit();
                edit2.clear();
                edit2.commit();
                Log.d(TAG, "clearData: Success");
                return true;
            }else{
                Log.d(TAG, "clearData: FAIL");
                return false;
            }
        }
        public static void deleteCache(Context context) {
            try {
                File dir = context.getCacheDir();
                deleteDir(dir);
            } catch (Exception e) {}
        }
        public static boolean deleteDir(File dir) {
            if (dir != null && dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
                return dir.delete();
            } else if (dir != null && dir.isFile()) {
                return dir.delete();
            } else {
                return false;
            }
        }
    }
}
