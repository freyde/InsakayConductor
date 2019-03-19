package com.insakay.conductor;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChangeBusFragment extends DialogFragment {

    private View view;
    private String opUID, newBusID, changeBusPassword, conductorKey, oldBusID;
    private Boolean busFound;




    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        busFound = false;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        view = inflater.inflate(R.layout.fragment_change_bus, null);

        final Context a = getActivity();
        final Context context =  getActivity().getApplicationContext();
        opUID = SaveSharedPreference.getOpUID(context);
        conductorKey = SaveSharedPreference.getConductorKey(context);
        oldBusID = SaveSharedPreference.getBusID(context);

        // Inflate the layout for Dialog
        builder.setTitle(R.string.change_bus_title)
            .setView(view)
            .setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    final EditText newBusIDView = (EditText) view.findViewById(R.id.newBusID);
                    final EditText changeBusPasswordView = (EditText) view.findViewById(R.id.changeBusPassword);
                    newBusID = newBusIDView.getText().toString();
                    changeBusPassword = changeBusPasswordView.getText().toString();
                    if(!newBusID.equals(oldBusID)) {
                        FirebaseDatabase.getInstance().getReference("users/" + opUID + "/buses")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        if(newBusID.equals(snapshot.child("busNo").getValue())) {
                                            busFound = true;
                                            FirebaseDatabase.getInstance().getReference("users/" + opUID + "/conductors/" + conductorKey)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (changeBusPassword.equals(dataSnapshot.child("password").getValue())) {
                                                            SaveSharedPreference.setBusID(context, newBusID);
                                                            Toast.makeText(a, R.string.change_bus_success, Toast.LENGTH_LONG).show();
                                                        } else {
                                                            Toast.makeText(a, R.string.change_bus_error_incorrect_password, Toast.LENGTH_LONG).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                            break;

                                        }
                                    }

                                    if(!busFound) {
                                        Toast.makeText(a, R.string.change_bus_error_unregistered_bus, Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                    } else {
                        Toast.makeText(a, R.string.change_bus_error_same_bus, Toast.LENGTH_LONG).show();
                    }
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });


        return builder.create();
    }
}
