package com.insakay.conductor;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class DeleteConfirm extends DialogFragment {

    private View view;
    private TextView key;
    private String opUID, input;
    private File fileDelete;
    private Activity prevActivity;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        final Activity activity = getActivity();

        view = inflater.inflate(R.layout.fragment_delete_confirm, null, true);

        opUID = SaveSharedPreference.getOpUID(getContext());


        builder.setTitle("Confirm Delete")
                .setView(view)
                .setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        key = (TextView) view.findViewById(R.id.op_key);
                        input = key.getText().toString();
                        if(input != null) {
                            FirebaseDatabase.getInstance().getReference("users/" + opUID + "/info")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {

                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(input.equals(dataSnapshot.child("key").getValue().toString())) {
                                                fileDelete.delete();
                                                activity.finish();
                                                Toast.makeText(activity, "Delete Successful.", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(activity, "Delete Failed. Incorrect Key", Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }


                });

        return builder.create();
    }


    public void setFile(File file) {
        fileDelete = file;
    }

    public void setPrevActivity(Activity activity) {
        prevActivity = activity;
    }
}