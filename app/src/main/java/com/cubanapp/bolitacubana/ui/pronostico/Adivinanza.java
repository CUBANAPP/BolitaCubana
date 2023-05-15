/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.pronostico;

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
import android.widget.ImageView;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cubanapp.bolitacubana.BuildConfig;
import com.cubanapp.bolitacubana.MainActivity;
import com.cubanapp.bolitacubana.R;
import com.cubanapp.bolitacubana.databinding.FragmentAdivinanzasBinding;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdapterResponseInfo;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class Adivinanza extends Fragment {

    private FragmentAdivinanzasBinding binding;

    //private InterstitialAd mInterstitialAd;

    private boolean adReady = false;

    private SharedPreferences sharedPref;

    private Snackbar mySnackbar;
    private String apiKey;
    private JsonObjectRequest stringRequest;

    private ArrayList<String> keyNames;
    private RequestQueue requestQueue;

    private JSONObject filenames;

    private static final String DEBUG_TAG = "Adivinanza";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAdivinanzasBinding.inflate(inflater, container, false);
        apiKey = BuildConfig.API_KEY;
        keyNames = new ArrayList<>();
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Bundle bundle = getIntent().getExtras();
        // ca-app-pub-3940256099942544/1033173712
        AdRequest adRequest = new AdRequest.Builder().build();
        if(getActivity() != null) {
            sharedPref = getActivity().getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            /*if(Build.VERSION.SDK_INT >= 19) {
                //ca-app-pub-3940256099942544/1033173712
                InterstitialAd.load(getActivity().getApplicationContext(), "ca-app-pub-3940256099942544/1033173712", adRequest,
                        new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                // The mInterstitialAd reference will be null until
                                // an ad is loaded.
                                mInterstitialAd = interstitialAd;
                                //mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, bundle);
                                Log.i(DEBUG_TAG, "onAdLoaded");
                                if (getActivity() != null) {
                                    configureInterstitial();
                                }
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                // Handle the error
                                Log.d(DEBUG_TAG, loadAdError.toString());
                                mInterstitialAd = null;
                            }
                        });
            }*/
            try {
                getSaves();
            } catch (JSONException e) {
                //throw new RuntimeException(e);
            } catch (UnsupportedEncodingException e) {
                //throw new RuntimeException(e);
            }
            downloadData();
        }

    }

    private void getSaves() throws JSONException, UnsupportedEncodingException {
        String saves = sharedPref.getString("filenames", null);
        if(saves != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                byte[] data = Base64.decode(saves, Base64.DEFAULT);
                String text = new String(data, StandardCharsets.UTF_8);
                Log.d(DEBUG_TAG, "SAVED: " + text);
                filenames = new JSONObject(text);
            } else {
                byte[] data = Base64.decode(saves, Base64.DEFAULT);
                String text = new String(data, "UTF-8");
                Log.d(DEBUG_TAG, "SAVED: " + text);
                filenames = new JSONObject(text);
            }
        }
    }
    private void setSaves(JSONObject files) throws UnsupportedEncodingException {
        SharedPreferences.Editor edit = sharedPref.edit();
        String text = files.toString();
        String base64;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            byte[] data = text.getBytes(StandardCharsets.UTF_8);
            base64 = Base64.encodeToString(data, Base64.DEFAULT);
        } else {
            byte[] data = text.getBytes("UTF-8");
            base64 = Base64.encodeToString(data, Base64.DEFAULT);
        }
        filenames = files;
        edit.putString("filenames", base64);
        edit.apply();
    }

    private boolean needUpdateFile(String x, Integer z) throws JSONException {
        boolean res = false;
        if(filenames != null){
            JSONArray jsonArray = filenames.names();
            if(jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    String key = jsonArray.getString(i);
                    if(Objects.equals(x, key)) {
                        if (z != null) {
                            int value = filenames.getInt(key);
                            if (value < z) {
                                res = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        else{
            res = true;
        }
        return res;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mySnackbar != null) {
            if (mySnackbar.isShown())
                mySnackbar.dismiss();
        }
        if (requestQueue != null) {
            requestQueue.stop();
            if (stringRequest != null) {
                stringRequest.cancel();
            }
        }
        binding = null;
        //mInterstitialAd = null;
    }
    private void downloadData(){
        if (getActivity() != null) {
            keyNames.clear();
            requestQueue = Volley.newRequestQueue(getActivity());
        }
        //}
            /*catch (Exception e){
                Log.e(DEBUG_TAG, "Volley Error : " + e.getMessage());
                //throw new RuntimeException(e);
            }*/
        if (requestQueue != null && binding != null) {
            binding.progressbar6.setVisibility(View.VISIBLE);
            String url;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                url = "https://cubanapp.info/api/photo/index.php";
            }else{
                url = "http://cubanapp.info/api/photo/index.php";
            }
            //String url = "https://cubanapp.info/api/suserinfo.php";
            JSONObject json = new JSONObject();

            try {
                json.put("apiKey", apiKey);
            } catch (JSONException e) {
                //try {
                if (getActivity() != null) {
                    mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                            getString(R.string.errorData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> downloadData());
                    mySnackbar.show();
                }
                //} catch (Exception ei) {
                // Log.e(DEBUG_TAG, "SnackbarError1 : " + ei.getMessage());
                //}
                Log.e(DEBUG_TAG, "JSONException : " + e.getMessage());
                //throw new RuntimeException(e);
                //startLaunch(false);
                binding.progressbar6.setVisibility(View.GONE);
            }
            // Request a string response from the provided URL.
            stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {
                        try {
                            if (getActivity() != null && binding != null) {
                                if (response.toString() != null) {
                                    Iterator<String> keys = response.keys();
                                    boolean error = false;
                                    while (keys.hasNext()) {
                                        String key = keys.next();
                                        if (key.equals("error")) {
                                            error = true;
                                            mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                                    response.getString("msg"), Snackbar.LENGTH_LONG);
                                            mySnackbar.show();
                                            binding.progressbar6.setVisibility(View.GONE);
                                            break;
                                        } else {
                                            if(filenames != null) {
                                                if (filenames.has(key)) {
                                                    if (needUpdateFile(key, response.getInt(key))) {
                                                        keyNames.add(key);
                                                        Log.i(DEBUG_TAG, "File Updated: " + key);
                                                    }

                                                } else {
                                                    Log.i(DEBUG_TAG, "File Added: " + key);
                                                    keyNames.add(key);
                                                }
                                            }
                                            else {
                                                Log.i(DEBUG_TAG, "No savegame, File Added: " + key);
                                                keyNames.add(key);
                                            }
                                        }
                                    }
                                    if(!error) {
                                        showAds();
                                        if(keyNames.size() > 0) {
                                            Log.e(DEBUG_TAG, "Descarga nueva");
                                            // TODO: Guardar sólo los archivos descargados correctamente si no se omite.
                                            setSaves(response);
                                            downloadFiles(keyNames);
                                        }else{ // Nada nuevo que descargar
                                            Log.i(DEBUG_TAG, "Nada nuevo que descargar");
                                            binding.progressbar6.setVisibility(View.GONE);
                                        }

                                    }


                                    Log.d(DEBUG_TAG, "Response is: " + response);

                                } else {
                                    if (getActivity() != null) {
                                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                                getString(R.string.internalerror), Snackbar.LENGTH_LONG);
                                        mySnackbar.show();
                                    }
                                    if (binding != null) {
                                        binding.progressbar6.setVisibility(View.GONE);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            if (binding != null)
                                binding.progressbar6.setVisibility(View.GONE);
                            if (getActivity() != null) {
                                mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                        getString(R.string.generateData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> downloadData());
                                mySnackbar.show();
                            }
                            Log.e(DEBUG_TAG, "JSONException2 : " + e.getMessage());
                        } catch (UnsupportedEncodingException e) {
                            if (binding != null)
                                binding.progressbar6.setVisibility(View.GONE);
                            if (getActivity() != null) {
                                mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                        getString(R.string.generateData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> downloadData());
                                mySnackbar.show();
                            }
                            Log.e(DEBUG_TAG, "Exception3 : " + e.getMessage());
                        }

                    }, error -> {
                if (binding != null)
                    binding.progressbar6.setVisibility(View.GONE);

                if (error instanceof TimeoutError) {
                    //try {
                    if (getActivity() != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                getString(R.string.slowconn), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> downloadData());
                        mySnackbar.show();
                    }
                    //} catch (Exception e) {
                    //  Log.e(DEBUG_TAG, "SnackbarError4 : " + e.getMessage());
                    //}

                } else {
                    //try {
                    if (getActivity() != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                getString(R.string.lostsvr), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> downloadData());
                        mySnackbar.show();
                    }
                    //} catch (Exception e) {
                    //   Log.e(DEBUG_TAG, "SnackbarError5 : " + e.getMessage());
                    //}
                }
                Log.e(DEBUG_TAG, "ERROR");
            });

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                    3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Add the request to the RequestQueue.
            requestQueue.add(stringRequest);
            if (binding != null)
                binding.progressbar6.setProgress(20);
        }
    }
    private void downloadFiles(ArrayList<String> files){
        if (getActivity() != null) {
            requestQueue = Volley.newRequestQueue(getActivity());
        }
        //}
            /*catch (Exception e){
                Log.e(DEBUG_TAG, "Volley Error : " + e.getMessage());
                //throw new RuntimeException(e);
            }*/
        if (requestQueue != null && binding != null) {
            binding.progressbar6.setVisibility(View.GONE);
            String url;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                url = "https://cubanapp.info/api/photo/index.php";
            }else{
                url = "http://cubanapp.info/api/photo/index.php";
            }
            JSONObject json = new JSONObject();

            try {
                json.put("apiKey", apiKey);
                json.put("file", files.get(0));
            } catch (JSONException e) {
                Log.e(DEBUG_TAG, "JSONException : " + e.getMessage());
            }
            // Request a string response from the provided URL.
            stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {
                        try {
                            if (getActivity() != null && binding != null) {
                                if (!response.has("error")) {
                                    if(response.has("type")){
                                        String type = response.getString("type");
                                        Log.e(DEBUG_TAG, type);
                                        String base64 = response.getString("base64");
                                        Log.e(DEBUG_TAG, base64);
                                        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
                                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                        ImageView nes = new ImageView(getContext());
                                        nes.setImageBitmap(decodedByte);
                                        ImageView nes1 = new ImageView(getContext());
                                        nes1.setImageBitmap(decodedByte);
                                        ImageView nes2 = new ImageView(getContext());
                                        nes2.setImageBitmap(decodedByte);
                                        ImageView nes3 = new ImageView(getContext());
                                        nes3.setImageBitmap(decodedByte);
                                        ImageView nes4 = new ImageView(getContext());
                                        nes4.setImageBitmap(decodedByte);
                                        ImageView nes5 = new ImageView(getContext());
                                        nes5.setImageBitmap(decodedByte);
                                        ImageView nes6 = new ImageView(getContext());
                                        nes6.setImageBitmap(decodedByte);
                                        ImageView nes7 = new ImageView(getContext());
                                        nes7.setImageBitmap(decodedByte);
                                        ImageView nes8 = new ImageView(getContext());
                                        nes8.setImageBitmap(decodedByte);
                                        //nes.setScaleType(ImageView.ScaleType.FIT_XY); nes.setAdjustViewBounds(true);
                                        //nes.setOnClickListener(v -> );
                                        binding.tablerow.addView(nes);
                                        binding.tablerow.addView(nes1);
                                        binding.tablerow.addView(nes2);
                                        TableRow s5 = new TableRow(getContext());
                                        binding.tablelayout.addView(s5);
                                    }

                                } else {
                                    Log.e(DEBUG_TAG, "No response");
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(DEBUG_TAG, "JSONException2 : " + e.getMessage());
                        }

                    }, error -> Log.e(DEBUG_TAG, "ERROR"));

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                    1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Add the request to the RequestQueue.
            requestQueue.add(stringRequest);
            if (binding != null)
                binding.progressbar6.setVisibility(View.GONE);
        }
    }

    /*private void configureInterstitial(){
        if(mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                    // Called when a click is recorded for an ad.
                    Log.d(DEBUG_TAG, "Ad was clicked.");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    Log.d(DEBUG_TAG, "Ad dismissed fullscreen content.");
                    mInterstitialAd = null;
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when ad fails to show.
                    Log.e(DEBUG_TAG, "Ad failed to show fullscreen content.");
                    mInterstitialAd = null;
                }

                @Override
                public void onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    Log.d(DEBUG_TAG, "Ad recorded an impression.");
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Log.d(DEBUG_TAG, "Ad showed fullscreen content.");
                }
            });
        }
    }*/
    private void showAds(){
        if (getActivity() != null) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if(mainActivity != null) {
                if(mainActivity.mInterstitialAd != null)
                    mainActivity.mInterstitialAd.show(getActivity());
                else
                    Log.d(DEBUG_TAG, "AD Still Not Loaded");
            }else
                Log.e(DEBUG_TAG, "MainActivity NOT FOUND");
        }else
            Log.e(DEBUG_TAG, "NO ACTIVITY FOUND");
    }
}
