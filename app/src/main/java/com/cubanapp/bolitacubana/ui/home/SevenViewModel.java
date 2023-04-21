/*
 * Copyright (c) CUBANAPP LLC 2019-2023 .
 */

package com.cubanapp.bolitacubana.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SevenViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SevenViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Seven fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
