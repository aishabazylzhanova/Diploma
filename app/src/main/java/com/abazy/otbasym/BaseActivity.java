package com.abazy.otbasym;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Renews activity title when in-app language is changed
        try {
            int label = getPackageManager().getActivityInfo(getComponentName(), 0).labelRes;
            if (label != 0) {
                setTitle(label);
            }
        } catch (Exception e) {
        }
    }
}
