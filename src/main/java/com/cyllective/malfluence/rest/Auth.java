package com.cyllective.malfluence.rest;

import com.cyllective.malfluence.helpers.Log;

public class Auth {

    private static String ACCESSKEY = "cyllectivewashere";
    public static boolean IsValidKey(String accesskey) {
        if (accesskey == null) {
            Log.Debug("Accesskey was missing");
            return false;
        }

        if (accesskey.equals(ACCESSKEY)) {
            return true;
        } else {
            Log.Debug("Accesskey was invalid");
            return false;
        }
    }
}
