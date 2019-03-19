package com.insakay.conductor;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignOutFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final locationService a = new locationService();
        Activity activity = new Activity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.sign_out_title)
                .setMessage(R.string.sign_out_message)
                .setPositiveButton(R.string.sign_out, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Context context = getActivity().getApplicationContext();
                        String key = SaveSharedPreference.getConductorKey(context);
                        String opUID = SaveSharedPreference.getOpUID(context);
                        getActivity().stopService(new Intent(getActivity(), locationService.class));
                        getActivity().finish();
                        HashMap<String, Integer> status = new HashMap<String, Integer>();
                        status.put("status", 0);
                        FirebaseDatabase.getInstance().getReference("users/" + opUID + "/conductors/" + key + "/status").setValue(0);
                        SaveSharedPreference.logout(context);
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                    }
                })

                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return  builder.create();
    }
}
