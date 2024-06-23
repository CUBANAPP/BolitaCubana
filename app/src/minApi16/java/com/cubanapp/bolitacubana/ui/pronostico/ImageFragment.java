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

package com.cubanapp.bolitacubana.ui.pronostico;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cubanapp.bolitacubana.BuildConfig;
import com.cubanapp.bolitacubana.MainActivity;
import com.cubanapp.bolitacubana.R;
import com.cubanapp.bolitacubana.databinding.FragmentImageviewerBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ImageFragment extends Fragment {

    private FragmentImageviewerBinding binding;

    private JsonObjectRequest stringRequest;

    private RequestQueue requestQueue;

    private String apiKey;

    private byte[] image;

    private byte[] data;

    private String type;
    private String name;
    private final String DEBUG_TAG = "ImageFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentImageviewerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        apiKey = BuildConfig.API_KEY;

        image = null;
        data = null;
        type = null;
        name = null;

        getParentFragmentManager().setFragmentResultListener("CUBANAPPImage", getViewLifecycleOwner(), (key, bundle) -> {
            buildView(bundle);
        });
        return root;
    }

    private void downloadFiles(String file) {
        if (getActivity() != null && binding != null) {
            requestQueue = Volley.newRequestQueue(getActivity());
        }

        if (requestQueue != null && binding != null && getActivity() != null) {

            //binding.progressbar6.setVisibility(View.GONE);
            String url;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                url = "https://cubanapp.info/api/photo/index.php";
            } else {
                url = "http://cubanapp.info/api/photo/index.php";
            }
            ArrayList<JsonObjectRequest> jsonObjectRequestArrayList = new ArrayList<>();
            //ArrayList<String> filesSuccess = new ArrayList<>();

            JSONObject json = new JSONObject();
            String jsonName = file + "_HD";
            try {
                json.put("apiKey", apiKey);
                json.put("file", jsonName);
            } catch (JSONException e) {
                if (e.getMessage() != null) {
                    Log.e(DEBUG_TAG, e.getMessage());
                }
            }
            // Request a string response from the provided URL.
            stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {
                        try {
                            if (getActivity() != null && binding != null) {
                                if (!response.has("error")) {
                                    if (response.has("base64")) {
                                        //String base64 = response.getString("base64");
                                        cacheData(response.toString(), jsonName);

                                        if (binding != null && getActivity() != null) {
                                            binding.progressBar5.setVisibility(View.GONE);
                                            byte[] image2 = Base64.decode(response.getString("base64"), Base64.DEFAULT);
                                            if (image2 != null) {
                                                binding.imageFragmentViewer.setVisibility(View.VISIBLE);
                                                Bitmap decodedByte2 = BitmapFactory.decodeByteArray(image2, 0, image2.length);
                                                binding.imageFragmentViewer.setImageBitmap(decodedByte2);
                                                binding.progressBar5.setVisibility(View.GONE);
                                            }
                                        }

                                    } else {
                                        if (binding != null) {
                                            binding.progressBar5.setVisibility(View.GONE);
                                        }
                                    }
                                } else {
                                    if (binding != null) {
                                        binding.progressBar5.setVisibility(View.GONE);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            Log.e(DEBUG_TAG, "No response " + e.getMessage());
                            if (binding != null) {
                                binding.progressBar5.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            Log.e(DEBUG_TAG, "No response " + e.getMessage());
                            if (binding != null) {
                                binding.progressBar5.setVisibility(View.GONE);
                            }
                        }


                    }, error -> {
                Log.e(DEBUG_TAG, "ERROR");
                if (binding != null) {
                    binding.progressBar5.setVisibility(View.GONE);
                }
            });

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                    3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);

        }
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
        if (requestQueue != null) {
            try {
                requestQueue.cancelAll(stringRequest);
            } catch (NullPointerException e) {
                if (e.getMessage() != null) {
                    Log.e(DEBUG_TAG, e.getMessage());
                }
            }
            requestQueue.stop();
        }
        binding = null;
        super.onDestroyView();
    }


    public void cacheData(String data, String name) throws IOException {
        if (getActivity() != null && getContext() != null && binding != null) {
            File dataFile = new File(getContext().getCacheDir(), name.concat(".json"));
            OutputStreamWriter objectOutputStream = new OutputStreamWriter(
                    new FileOutputStream(dataFile));
            BufferedWriter bufferedWriter = new BufferedWriter(objectOutputStream);
            bufferedWriter.write(data);
            bufferedWriter.close();
        }

    }

    private void buildView(@Nullable Bundle bundle) {
        if (bundle != null) {
            type = bundle.getString("type", null);
            if (type != null) {
                name = bundle.getString("name", "");
                if (getActivity() != null) {
                    if (((MainActivity) getActivity()).getSupportActionBar() != null) {
                        ((MainActivity) getActivity()).getSupportActionBar().setTitle(name);
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                        if (preferences.getBoolean("copyID", false)) {
                            boolean copy = preferences.getBoolean("copyID", false);
                            Log.d(DEBUG_TAG, "copyID" + copy);
                            copyName(name);
                        }
                    }
                }
                if (type.equals("jpg")) {
                    image = bundle.getByteArray("base64");
                    if (image != null) {
                        binding.imageFragmentViewer.setVisibility(View.VISIBLE);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(image, 0, image.length);
                        binding.imageFragmentViewer.setImageBitmap(decodedByte);
                        String hdName = name + "_HD";
                        String cache = null;
                        try {
                            cache = readData(hdName);

                        } catch (IOException e) {
                            Log.d(DEBUG_TAG, "Error de la CACHE: " + e.getMessage());
                            downloadFiles(name);
                        } catch (Exception e) {
                            Log.d(DEBUG_TAG, "Exception");
                            downloadFiles(name);
                        }
                        if (cache != null) {
                            JSONObject jsonFile = null;
                            try {
                                jsonFile = new JSONObject(cache);
                            } catch (JSONException e) {
                                //throw new RuntimeException(e);
                            }
                            String base64 = null;
                            try {
                                base64 = jsonFile.getString("base64");
                            } catch (JSONException e) {
                                //throw new RuntimeException(e);
                            }
                            byte[] image2 = Base64.decode(base64, Base64.DEFAULT);
                            if (image2 != null) {
                                binding.imageFragmentViewer.setVisibility(View.VISIBLE);
                                Bitmap decodedByte2 = BitmapFactory.decodeByteArray(image2, 0, image2.length);
                                binding.imageFragmentViewer.setImageBitmap(decodedByte2);
                                binding.progressBar5.setVisibility(View.GONE);
                            }
                        } else downloadFiles(name);
                    }
                } else {
                    binding.progressBar5.setVisibility(View.GONE);
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
                                throw new RuntimeException(e);
                            }
                        }
                        if (text != null) {
                            binding.nestcontainer2.setVisibility(View.VISIBLE);
                            binding.textFragmentViewer.setText(text);
                        }
                    }
                }
            }
        }
    }

    public String readData(String name) throws IOException {

        if (getActivity() != null && getContext() != null && binding != null) {
            File dataFile = new File(getContext().getCacheDir(), name.concat(".json"));
            if (!dataFile.exists()) {
                Log.w(DEBUG_TAG, "File do not Exist");
                return null;
            }
            FileInputStream fileInputStream = new FileInputStream(dataFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String data = bufferedReader.readLine();
            bufferedReader.close();
            return data;
        } else
            return null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        try {
            if (image != null)
                outState.putByteArray("base64", image);
            else outState.putByteArray("base64", data);

            if (type != null) outState.putString("type", type);

            if (name != null) outState.putString("name", name);
        } catch (NullPointerException e) { //
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) buildView(savedInstanceState);
        super.onViewStateRestored(savedInstanceState);
    }
}
