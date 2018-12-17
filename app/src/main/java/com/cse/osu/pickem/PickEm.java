package com.cse.osu.pickem;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class PickEm extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
