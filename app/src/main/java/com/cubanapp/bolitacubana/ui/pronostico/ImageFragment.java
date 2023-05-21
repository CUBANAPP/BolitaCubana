/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.pronostico;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cubanapp.bolitacubana.MainActivity;
import com.cubanapp.bolitacubana.databinding.FragmentImageviewerBinding;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class ImageFragment extends Fragment {

    private FragmentImageviewerBinding binding;
    private final String DEBUG_TAG = "ImageFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentImageviewerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getParentFragmentManager().setFragmentResultListener("CUBANAPPImage", getViewLifecycleOwner(), (key, bundle) -> {

            if (bundle != null) {
                String type = bundle.getString("type", "");
                if (type != null) {
                    Log.e(DEBUG_TAG, type);
                    if (type.equals("jpg")) {
                        byte[] image = bundle.getByteArray("base64");
                        if (image != null){
                            binding.imageFragmentViewer.setVisibility(View.VISIBLE);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(image, 0, image.length);
                            binding.imageFragmentViewer.setImageBitmap(decodedByte);
                        }
                    }
                    else {
                        byte[] data = bundle.getByteArray("base64");
                        if(data != null) {
                            String text;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                text = new String(data, StandardCharsets.UTF_8);
                            } else {
                                try {
                                    text = new String(data, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            if (text != null) {
                                binding.nestcontainer2.setVisibility(View.VISIBLE);
                                binding.textFragmentViewer.setText(text);
                                Typeface font = Typeface.createFromAsset(requireContext().getAssets(), "burbank_normal.otf");
                                binding.textFragmentViewer.setTypeface(font);
                            }
                        }
                    }
                    String name = bundle.getString("name", "Error");
                    if (name != null)
                        if(getActivity() != null)
                            if(((MainActivity) getActivity()).getSupportActionBar() != null)
                                ((MainActivity) getActivity()).getSupportActionBar().setTitle(name);
                }
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
