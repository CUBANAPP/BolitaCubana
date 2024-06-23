/*
 *      Bolita Cubana
 *      Copyright (C) 2019-2024 CUBANAPP LLC
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Affero General Public License as
 *      published by the Free Software Foundation, either version 3 of the
 *      License, or (at your option) any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Affero General Public License for more details.
 *
 *      You should have received a copy of the GNU Affero General Public License
 *      along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *      Email contact: help@cubanapp.info
 */

package com.cubanapp.bolitacubana.ui.home;

import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.cubanapp.bolitacubana.R;
import com.cubanapp.bolitacubana.databinding.FragmentHomeBinding;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class HomeFragment extends Fragment {

    private long mLastClickTime = 0;
    private FragmentHomeBinding binding;
    private static final String DEBUG_TAG = "HomeFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
       //mLastClickTime = SystemClock.elapsedRealtime();
        binding.buttonFlorida.setOnClickListener(view1a -> {
            if (binding != null && binding.buttonFlorida.isClickable() && getActivity() != null) {
                //binding.buttonGeorgia.setClickable(false);
                //binding.buttonFlorida.setClickable(false);
                //binding.buttonNewYork.setClickable(false);

                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();


                try {
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_navigation_home_to_navigation_florida);
                } catch (IllegalArgumentException e) {
                    if (e.getMessage() != null) {
                        Log.e(DEBUG_TAG, e.getMessage());
                    }
                    if (Build.VERSION.SDK_INT >= 19) {
                        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                        firebaseCrashlytics.sendUnsentReports();
                        firebaseCrashlytics.recordException(e);
                    }
                    //
                }
            }
        });
        binding.buttonGeorgia.setOnClickListener(view1b -> {
            if (binding != null && binding.buttonGeorgia.isClickable() && getActivity() != null) {
                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                try {
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_navigation_home_to_navigation_georgia);
                } catch (IllegalArgumentException e) {
                    if (e.getMessage() != null) {
                        Log.e(DEBUG_TAG, e.getMessage());
                    }
                    if (Build.VERSION.SDK_INT >= 19) {
                        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                        firebaseCrashlytics.sendUnsentReports();
                        firebaseCrashlytics.recordException(e);
                    }
                    //
                }
            }
        });
        binding.buttonNewYork.setOnClickListener(view1c -> {
            if (binding != null && binding.buttonNewYork.isClickable() && getActivity() != null) {
                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                try {
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_navigation_home_to_navigation_newyork);
                } catch (IllegalArgumentException e) {
                    if (e.getMessage() != null) {
                        Log.e(DEBUG_TAG, e.getMessage());
                    }
                    if (Build.VERSION.SDK_INT >= 19) {
                        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                        firebaseCrashlytics.sendUnsentReports();
                        firebaseCrashlytics.recordException(e);
                    }
                }
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
