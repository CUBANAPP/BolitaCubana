/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.home;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.cubanapp.bolitacubana.R;
import com.cubanapp.bolitacubana.databinding.FragmentHomeBinding;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static final String DEBUG_TAG = "HomeFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        /*HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
*/
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.buttonFlorida.setOnClickListener(view1a -> {
            if (binding != null && binding.buttonFlorida.isClickable() && getActivity() != null) {
                binding.buttonFlorida.setClickable(false);
                binding.buttonNewYork.setClickable(false);
                binding.buttonGeorgia.setClickable(false);
                try {
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_home_to_florida);
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
                binding.buttonGeorgia.setClickable(false);
                binding.buttonFlorida.setClickable(false);
                binding.buttonNewYork.setClickable(false);
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
                binding.buttonNewYork.setClickable(false);
                binding.buttonFlorida.setClickable(false);
                binding.buttonGeorgia.setClickable(false);
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
                    //
                }
            }
        });


        //binding.textDashboard;
        /*final Button b = root.findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.d(DEBUG_TAG, "Clicked Button1");
                FloridaFragment nextFrag= new FloridaFragment();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.homelayout, nextFrag, "findThisFragment")
                        .addToBackStack(null)
                        .commit();
            }
        });*/

        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
