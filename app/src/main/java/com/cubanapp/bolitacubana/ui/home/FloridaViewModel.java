/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FloridaViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    private final MutableLiveData<String> mText;

    public FloridaViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Florida fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
