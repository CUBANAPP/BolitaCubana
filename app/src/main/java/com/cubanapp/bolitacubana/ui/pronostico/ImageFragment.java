/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.pronostico;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.cubanapp.bolitacubana.MainActivity;
import com.cubanapp.bolitacubana.R;
import com.cubanapp.bolitacubana.databinding.FragmentImageviewerBinding;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ImageFragment extends Fragment {

    private FragmentImageviewerBinding binding;
    private final String DEBUG_TAG = "ImageFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentImageviewerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getParentFragmentManager().setFragmentResultListener("CUBANAPPImage", getViewLifecycleOwner(), (key, bundle) -> {

            if (bundle != null) {
                String type = bundle.getString("type", null);
                if (type != null) {
                    Log.e(DEBUG_TAG, type);
                    String name = bundle.getString("name", null);
                    if (getActivity() != null) {
                        if (((MainActivity) getActivity()).getSupportActionBar() != null) {
                            ((MainActivity) getActivity()).getSupportActionBar().setTitle(Objects.requireNonNullElse(name, "ERROR"));
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                            if (preferences.getBoolean("copyID", false)) {
                                boolean copy = preferences.getBoolean("copyID", false);
                                Log.e(DEBUG_TAG, "copyID" + copy);
                                copyName(Objects.requireNonNullElse(name, ""));
                            }
                        }
                    }
                    if (type.equals("jpg")) {
                        byte[] image = bundle.getByteArray("base64");
                        if (image != null) {
                            binding.imageFragmentViewer.setVisibility(View.VISIBLE);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(image, 0, image.length);
                            binding.imageFragmentViewer.setImageBitmap(decodedByte);
                        }
                    } else {
                        byte[] data = bundle.getByteArray("base64");
                        if (data != null) {
                            String text;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                text = new String(data, StandardCharsets.UTF_8);
                            } else {
                                try {
                                    text = new String(data, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    if (e.getMessage() != null) {
                                        Log.e(DEBUG_TAG, e.getMessage());
                                    }
                                    if (Build.VERSION.SDK_INT >= 19) {
                                        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                        firebaseCrashlytics.sendUnsentReports();
                                        firebaseCrashlytics.recordException(e);
                                    }
                                    throw new RuntimeException(e);
                                }
                            }
                            if (text != null) {
                                binding.nestcontainer2.setVisibility(View.VISIBLE);
                                binding.textFragmentViewer.setText(text);
                                //Typeface font = Typeface.createFromAsset(requireContext().getAssets(), "burbank_normal.otf");
                                //binding.textFragmentViewer.setTypeface(font);
                            }
                        }
                    }
                }
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void copyName(String text) {
        if (getActivity() != null) {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Bolita Cubana ID", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), getString(R.string.copied), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
