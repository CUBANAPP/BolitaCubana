/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.search;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cubanapp.bolitacubana.BuildConfig;
import com.cubanapp.bolitacubana.R;
import com.cubanapp.bolitacubana.databinding.FragmentSearchBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private long mLastClickTime = 0;
    private FragmentSearchBinding binding;
    private AlertDialog builder;
    private String apiKey;

    private JsonObjectRequest stringRequest;

    private RequestQueue requestQueue;

    private Snackbar mySnackbar;
    private static final String DEBUG_TAG = "SearchDate";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        apiKey = BuildConfig.API_KEY;
        binding.button4.setOnClickListener(this::openDate);
        return root;
    }

    private void openDate(View v) {
        // mis-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();


        builder = new AlertDialog.Builder(v.getContext())
                .create();
        View datepickerView = getLayoutInflater().inflate(R.layout.calendar_view, binding.getRoot(), false);
        DatePicker datePickers = datepickerView.findViewById(R.id.datepicker);
        Calendar fecha = Calendar.getInstance();
        fecha.add(Calendar.DATE, -1);
        Date currentTimes = fecha.getTime();
        datePickers.setMaxDate(currentTimes.getTime());
        CalendarView calendarViews = datePickers.getCalendarView();
        calendarViews.setMaxDate(currentTimes.getTime());
        builder.setView(datepickerView);

        builder.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.open), (dialog, which) -> {
            DatePicker datePicker = builder.findViewById(R.id.datepicker);
            if (datePicker != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    datePicker.setFirstDayOfWeek(2);
                }
                CalendarView calendarView = datePicker.getCalendarView();
                searchDate(calendarView.getDate());
            }
        });
        builder.setButton(Dialog.BUTTON_NEGATIVE, getString(R.string.dismiss), (dialog, which) -> builder.dismiss());
        builder.show();
    }

    private void searchDate(Long date) {
        if (binding == null)
            return;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Date format = new Date(date);
        String dateString = dateFormat.format(format);

        if (getActivity() != null && binding != null) {
            requestQueue = Volley.newRequestQueue(getActivity());
        }
        //}
            /*catch (Exception e){
                Log.e(DEBUG_TAG, "Volley Error : " + e.getMessage());
                //throw new RuntimeException(e);
            }*/
        if (requestQueue != null && binding != null) {

            binding.progressBar4.setProgress(0);

            binding.progressBar4.setVisibility(View.VISIBLE);
            String url;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                url = "https://cubanapp.info/api/searchcache/index.php";
            } else {
                url = "http://cubanapp.info/api/searchcache/index.php";
            }
            JSONObject json = new JSONObject();

            try {
                json.put("apiKey", apiKey);
                json.put("f", dateString);
                Log.d(DEBUG_TAG, "Date to Search: " + dateString);
            } catch (JSONException e) {
                if (getActivity() != null) {
                    mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                            getString(R.string.errorData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date));
                    mySnackbar.show();
                }
                if (e.getMessage() != null) {
                    Log.e(DEBUG_TAG, e.getMessage());
                }
                if (Build.VERSION.SDK_INT >= 19) {
                    FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                    firebaseCrashlytics.sendUnsentReports();
                    firebaseCrashlytics.recordException(e);
                }
                if (binding != null)
                    binding.progressBar4.setVisibility(View.GONE);
            }

            stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {

                        try {

                            boolean error = (Boolean) response.get("error");
                            if (!error) {
                                if (getActivity() != null && binding != null) {
                                    binding.progressBar4.setProgress(100);

                                    String fijos = (String) response.get("fijos");
                                    String corridos = (String) response.get("corridos");

                                    binding.D1s.setText((String) response.get("date"));
                                    binding.Ds.setText(getString(R.string.dia));
                                    binding.SDs.setText((String) response.get("semana"));
                                    Log.e(DEBUG_TAG, "FIJO: " + fijos);
                                    try {
                                        binding.F10s.setText(fijos.substring(0, 1));
                                        binding.F11s.setText(fijos.substring(1, 3));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        if (e.getMessage() != null) {
                                            Log.e(DEBUG_TAG, e.getMessage());
                                        }
                                        if (Build.VERSION.SDK_INT >= 19) {
                                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                            firebaseCrashlytics.sendUnsentReports();
                                            firebaseCrashlytics.recordException(e);
                                        }
                                        launchError();
                                        return;
                                    }


                                    try {
                                        binding.C11s.setText(corridos.substring(0, 2));
                                        binding.C12s.setText(corridos.substring(2, 4));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        if (e.getMessage() != null) {
                                            Log.e(DEBUG_TAG, e.getMessage());
                                        }
                                        if (Build.VERSION.SDK_INT >= 19) {
                                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                            firebaseCrashlytics.sendUnsentReports();
                                            firebaseCrashlytics.recordException(e);
                                        }
                                        launchError();
                                        return;
                                    }

                                    binding.N1s.setText((String) response.get("date"));
                                    binding.Ns.setText(getString(R.string.noche));
                                    binding.SNs.setText((String) response.get("semana"));

                                    try {
                                        binding.F20s.setText(fijos.substring(3, 4));
                                        binding.F21s.setText(fijos.substring(4, 6));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        if (e.getMessage() != null) {
                                            Log.e(DEBUG_TAG, e.getMessage());
                                        }
                                        if (Build.VERSION.SDK_INT >= 19) {
                                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                            firebaseCrashlytics.sendUnsentReports();
                                            firebaseCrashlytics.recordException(e);
                                        }
                                        launchError();
                                        return;
                                    }

                                    try {
                                        binding.C21s.setText(corridos.substring(4, 6));
                                        binding.C22s.setText(corridos.substring(6, 8));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        if (e.getMessage() != null) {
                                            Log.e(DEBUG_TAG, e.getMessage());
                                        }
                                        if (Build.VERSION.SDK_INT >= 19) {
                                            FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                            firebaseCrashlytics.sendUnsentReports();
                                            firebaseCrashlytics.recordException(e);
                                        }
                                        launchError();
                                        return;
                                    }
                                    binding.progressBar4.setVisibility(View.GONE);
                                }
                            } else {
                                if (binding != null)
                                    binding.progressBar4.setVisibility(View.GONE);
                                if (getActivity() != null) {
                                    mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                            getString(R.string.internalerror), Snackbar.LENGTH_LONG);
                                    mySnackbar.show();
                                }
                            }

                        } catch (JSONException e) {
                            if (binding != null)
                                binding.progressBar4.setVisibility(View.GONE);
                            if (getActivity() != null) {
                                mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                        getString(R.string.generateData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date));
                                mySnackbar.show();
                            }
                            if (e.getMessage() != null) {
                                Log.e(DEBUG_TAG, e.getMessage());
                            }
                            if (Build.VERSION.SDK_INT >= 19) {
                                FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                                firebaseCrashlytics.sendUnsentReports();
                                firebaseCrashlytics.recordException(e);
                            }
                        }
                    }, error -> {
                if (binding != null)
                    binding.progressBar4.setVisibility(View.GONE);

                if (error instanceof TimeoutError) {
                    if (getActivity() != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                getString(R.string.slowconn), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date));
                        mySnackbar.show();
                    }

                } else {
                    if (getActivity() != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                getString(R.string.lostsvr), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date));
                        mySnackbar.show();
                    }
                }
                Log.e(DEBUG_TAG, "ERROR");
            }

            );
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                    3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(stringRequest);
            if (binding != null)
                binding.progressBar4.setProgress(40);
        }
    }

    @Override
    public void onDestroyView() {
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
        if (builder != null) {
            if (builder.isShowing())
                builder.dismiss();
        }
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (binding != null) {
            try {
                outState.putString("D1s", binding.D1s.getText().toString());
                outState.putString("Ds", binding.Ds.getText().toString());
                outState.putString("SDs", binding.SDs.getText().toString());
                outState.putString("F10s", binding.F10s.getText().toString());
                outState.putString("F11s", binding.F11s.getText().toString());
                outState.putString("C11s", binding.C11s.getText().toString());
                outState.putString("C12s", binding.C12s.getText().toString());
                outState.putString("N1s", binding.N1s.getText().toString());
                outState.putString("Ns", binding.Ns.getText().toString());
                outState.putString("SNs", binding.SNs.getText().toString());
                outState.putString("F20s", binding.F20s.getText().toString());
                outState.putString("F21s", binding.F21s.getText().toString());
                outState.putString("C21s", binding.C21s.getText().toString());
                outState.putString("C22s", binding.C22s.getText().toString());
            } catch (NullPointerException e) { //
            }
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) buildView(savedInstanceState);
        super.onViewStateRestored(savedInstanceState);
    }

    private void buildView(@Nullable Bundle bundle) {
        if (bundle != null && binding != null) {
            binding.D1s.setText(bundle.getString("D1s", ""));
            binding.Ds.setText(bundle.getString("Ds", ""));
            binding.SDs.setText(bundle.getString("SDs", ""));
            binding.F10s.setText(bundle.getString("F10s", ""));
            binding.F11s.setText(bundle.getString("F11s", ""));
            binding.C11s.setText(bundle.getString("C11s", ""));
            binding.C12s.setText(bundle.getString("C12s", ""));
            binding.N1s.setText(bundle.getString("N1s", ""));
            binding.Ns.setText(bundle.getString("Ns", ""));
            binding.SNs.setText(bundle.getString("SNs", ""));
            binding.F20s.setText(bundle.getString("F20s", ""));
            binding.F21s.setText(bundle.getString("F21s", ""));
            binding.C21s.setText(bundle.getString("C21s", ""));
            binding.C22s.setText(bundle.getString("C22s", ""));
        }
    }

    private void launchError() {
        if (binding != null) {
            binding.progressBar4.setVisibility(View.GONE);
            if (getActivity() != null) {
                mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                        getString(R.string.internalerror), Snackbar.LENGTH_LONG);
                mySnackbar.show();
            }
        }
    }
}
