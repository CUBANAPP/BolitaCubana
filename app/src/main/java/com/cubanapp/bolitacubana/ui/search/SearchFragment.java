/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.search;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private AlertDialog builder;
    private String apiKey;

    private JsonObjectRequest stringRequest;

    private RequestQueue requestQueue;

    private Snackbar mySnackbar;
    private static final String DEBUG_TAG = "SearchDate";
    
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        /*DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
*/
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        apiKey = BuildConfig.API_KEY;
        binding.button4.setOnClickListener(this::openDate);

        //final TextView textView = binding;
        //dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    /*private void pickDate(View v){
        CalendarView calendarView = .findViewById(R.id.calendarid);
        calendarView.getDate();
        Log.d(DEBUG_TAG, "DATE Selected: " + calendarView.getDate());

    }*/
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*if (binding != null && getActivity() != null) {

            try {
                font = Typeface.createFromAsset(requireContext().getAssets(), "burbank_normal.otf");
            }
            catch (IllegalStateException e){
                font = Typeface.create(Typeface.DEFAULT,Typeface.NORMAL);
            }

            binding.D1s.setTypeface(font);
            binding.Ds.setTypeface(font);
            binding.SDs.setTypeface(font);
            binding.F10s.setTypeface(font);
            binding.F11s.setTypeface(font);
            binding.C11s.setTypeface(font);
            binding.C12s.setTypeface(font);
            binding.N1s.setTypeface(font);
            binding.Ns.setTypeface(font);
            binding.SNs.setTypeface(font);
            binding.F20s.setTypeface(font);
            binding.F21s.setTypeface(font);
            binding.C21s.setTypeface(font);
            binding.C22s.setTypeface(font);
        }*/
    }

    private void openDate(View v){
        builder = new AlertDialog.Builder(v.getContext())
                .create();
        //builder.setTitle(getString(R.string.pickDate));
        View datepickerView = getLayoutInflater().inflate(R.layout.calendar_view,binding.getRoot(),false);
        DatePicker datePickers = datepickerView.findViewById(R.id.datepicker);
        Calendar fecha = Calendar.getInstance();
        fecha.add(Calendar.DATE, -1);
        Date currentTimes = fecha.getTime();
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            datePickers.setFirstDayOfWeek(2);
        }*/
        datePickers.setMaxDate(currentTimes.getTime());
        CalendarView calendarViews = datePickers.getCalendarView();
        calendarViews.setMaxDate(currentTimes.getTime());
        builder.setView(datepickerView);

        builder.setButton(Dialog.BUTTON_POSITIVE,getString(R.string.open), (dialog, which) -> {
            DatePicker datePicker = builder.findViewById(R.id.datepicker);
            if(datePicker !=null) {
                Date currentTime = Calendar.getInstance().getTime();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    datePicker.setFirstDayOfWeek(2);
                }
                CalendarView calendarView = datePicker.getCalendarView();
                //calendarView.setDate(currentTime.getTime());
                //Log.e(DEBUG_TAG, "DATE Selected: " + calendarView.getDate());
                //Log.d(DEBUG_TAG, "currentTime: " + currentTime.getTime());
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date format = new Date(calendarView.getDate());
                String dateString = dateFormat.format(format);
                //Log.e(DEBUG_TAG, "DATE Parsed: " + dateString);

                searchDate(calendarView.getDate());
            }
        });
        builder.setButton(Dialog.BUTTON_NEGATIVE,getString(R.string.dismiss), (dialog, which) -> builder.dismiss());
        builder.show();
    }

    private void searchDate(Long date) {
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
            }else{
                url = "http://cubanapp.info/api/searchcache/index.php";
            }
            //String url = "https://cubanapp.info/api/searchcache/index.php";
            JSONObject json = new JSONObject();

            try {
                json.put("apiKey", apiKey);
                json.put("f", dateString);
                Log.d(DEBUG_TAG, "Date to Search: " + dateString);
            } catch (JSONException e) {
                //try {
                if (getActivity() != null) {
                    mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                            getString(R.string.errorData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date));
                    mySnackbar.show();
                }
                //} catch (Exception ei) {
                // Log.e(DEBUG_TAG, "SnackbarError1 : " + ei.getMessage());
                //}
                Log.e(DEBUG_TAG, "JSONException : " + e.getMessage());
                //throw new RuntimeException(e);
                //startLaunch(false);
                if (binding != null)
                    binding.progressBar4.setVisibility(View.GONE);
            }
            // Request a string response from the provided URL.
            stringRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                    response -> {
                        // Display the first 500 characters of the response string.
                        try {
                            //JSONObject error = response.getJSONObject("");
                            //response.get("error");
                            boolean error = (Boolean) response.get("error");
                            if (!error) {
                                if (getActivity() != null && binding != null) {
                                    binding.progressBar4.setProgress(100);
                                    //SimpleDateFormat semanaOut = new SimpleDateFormat("EEEE", Locale.getDefault());
                                    //SimpleDateFormat yearOut = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                                    //Date yearText = semanaOut.parse((String) response.get("date"));
                                    //Date semanaText = yearOut.parse((String) response.get("semana"));


                                    String fijos = (String) response.get("fijos");
                                    String corridos = (String) response.get("corridos");

                                    binding.D1s.setText((String) response.get("date"));
                                    binding.Ds.setText(getString(R.string.dia));
                                    binding.SDs.setText((String) response.get("semana"));
                                    try {
                                        binding.F10s.setText(fijos.substring(0, 1));
                                        binding.F11s.setText(fijos.substring(1, 3));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        throw new RuntimeException(e);
                                    }


                                    try {
                                        binding.C11s.setText(corridos.substring(0, 2));
                                        binding.C12s.setText(corridos.substring(2, 4));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        throw new RuntimeException(e);
                                    }

                                    binding.N1s.setText((String) response.get("date"));
                                    binding.Ns.setText(getString(R.string.noche));
                                    binding.SNs.setText((String) response.get("semana"));

                                    try {
                                        binding.F20s.setText(fijos.substring(3, 4));
                                        binding.F21s.setText(fijos.substring(4, 6));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        throw new RuntimeException(e);
                                    }

                                    try {
                                        binding.C21s.setText(corridos.substring(4, 6));
                                        binding.C22s.setText(corridos.substring(6, 8));
                                    } catch (StringIndexOutOfBoundsException e) {
                                        throw new RuntimeException(e);
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
                            //errorStart.set(true);
                            //try {
                            if (getActivity() != null) {
                                mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                        getString(R.string.generateData), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date));
                                mySnackbar.show();
                            }
                            // } catch (Exception ei) {
                            //    Log.e(DEBUG_TAG, "SnackbarError3 : " + ei.getMessage());
                            //}
                            Log.e(DEBUG_TAG, "JSONException2 : " + e.getMessage());
                            //throw new RuntimeException(e);
                        }
                    }, error -> {
                if (binding != null)
                    binding.progressBar4.setVisibility(View.GONE);

                if (error instanceof TimeoutError) {
                    //try {
                    if (getActivity() != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                getString(R.string.slowconn), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date));
                        mySnackbar.show();
                    }
                    //} catch (Exception e) {
                    //  Log.e(DEBUG_TAG, "SnackbarError4 : " + e.getMessage());
                    //}

                } else {
                    //try {
                    if (getActivity() != null) {
                        mySnackbar = Snackbar.make(getActivity().findViewById(R.id.container),
                                getString(R.string.lostsvr), Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> searchDate(date));
                        mySnackbar.show();
                    }
                    //} catch (Exception e) {
                    //   Log.e(DEBUG_TAG, "SnackbarError5 : " + e.getMessage());
                    //}
                }
                Log.e(DEBUG_TAG, "ERROR");
            }

            );
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                    3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Add the request to the RequestQueue.
            requestQueue.add(stringRequest);
            if (binding != null)
                binding.progressBar4.setProgress(40);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mySnackbar != null) {
            if(mySnackbar.isShown())
                mySnackbar.dismiss();
        }
        if (requestQueue != null) {
            requestQueue.stop();
            if (stringRequest != null) {
                stringRequest.cancel();
            }
        }
        binding = null;
        if(builder != null){
            if(builder.isShowing())
                builder.dismiss();
        }
    }
    
}
