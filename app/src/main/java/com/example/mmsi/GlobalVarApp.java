package com.example.mmsi;

import android.app.Application;

public class GlobalVarApp extends Application {
    private CharSequence userName;

    public void setUserName(CharSequence userName) {
        this.userName=userName;
    }

    public CharSequence getUserName() {
        return userName;
    }
}
