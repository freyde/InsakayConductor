package com.insakay.conductor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

public class SaveSharedPreference {

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setAccountCredentials(Context context,String conductorName, String conductorID, String operatorUID, String operatorID, String busID, String busPlate, String busDriver, String conductorKey) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("CONDUCTOR_NAME", conductorName);
        editor.putString("CONDUCTOR_ID", conductorID);
        editor.putString("OPERATOR_UID", operatorUID);
        editor.putString("OPERATOR_ID", operatorID);
        editor.putString("BUS_ID", busID);
        editor.putString("BUS_PLATE", busPlate);
        editor.putString("BUS_DRIVER", busDriver);
        editor.putString("CONDUCTOR_KEY", conductorKey);
        editor.putBoolean("IS_LOGGED_IN", true);
        editor.commit();
    }


    public static String getOpUID(Context context) {
        return getSharedPreferences(context).getString("OPERATOR_UID", null);
    }

    public static String getBusID(Context context) {
        return getSharedPreferences(context).getString("BUS_ID", null);
    }

    public static String getBusPlate(Context context) {
        return getSharedPreferences(context).getString("BUS_PLATE", null);
    }

    public static String getBusDriver(Context context) {
        return getSharedPreferences(context).getString("BUS_DRIVER", null);
    }

    public static String getConductorName(Context context) {
        return getSharedPreferences(context).getString("CONDUCTOR_NAME", null);
    }

    public static String getConductorID(Context context) {
        return getSharedPreferences(context).getString("CONDUCTOR_ID", null);
    }

    public static String getConductorKey(Context context) {
        return getSharedPreferences(context).getString("CONDUCTOR_KEY", null);
    }

    public static void setBusID(Context context, String busID) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("BUS_ID", busID);
        editor.commit();
    }

    public static Boolean isLoggedIn (Context context) {
         return getSharedPreferences(context).getBoolean("IS_LOGGED_IN", false);
    }

    public static void logout(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.commit();
    }
}
