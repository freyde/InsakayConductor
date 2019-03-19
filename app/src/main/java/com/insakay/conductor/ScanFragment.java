package com.insakay.conductor;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScanFragment extends Fragment {

    private View mView;
    private Button scan, manual;
    private ManualTicket manualTicketFragment;

    public ScanFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment

        mView =  inflater.inflate(R.layout.fragment_scan, container, false);

        scan = (Button) mView.findViewById(R.id.scan_camera);
        manual = (Button) mView .findViewById(R.id.scan_manual);

        manualTicketFragment = new ManualTicket();

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentIntentIntegrator IntentIntegrator = new FragmentIntentIntegrator(ScanFragment.this  );
                IntentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                IntentIntegrator.setCameraId(0);
                IntentIntegrator.setOrientationLocked(false);
                IntentIntegrator.setPrompt("scanning");
                IntentIntegrator.setBeepEnabled(true);
                IntentIntegrator.setBarcodeImageEnabled(true);
                IntentIntegrator.initiateScan();
            }
        });


        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualTicketFragment.show(getActivity().getSupportFragmentManager(), "manual ticket");
            }
        });

        return mView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        final String contents = result.getContents();
        if (result!=null && result.getContents()!=null) {
            new AlertDialog.Builder(getActivity())
                .setTitle("Ticket Information")
                .setMessage(contents)
                .setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
                        String curDate = dateFormat.format(new Date());
                        String conductorID = SaveSharedPreference.getConductorID(getActivity().getApplicationContext());
                        String fileName = conductorID.concat("_").concat(curDate).concat(".sky");
                        String newContents ="";
                        try {
                            String root = getActivity().getFilesDir().getPath();
                            if(new File(root, fileName).exists()) {
                                newContents = "\n".concat(contents.replace(".", ", "));
                            } else {
                                newContents = contents.replace(".", ", ");
                            }
                            FileOutputStream fos = getActivity().openFileOutput(fileName, Context.MODE_APPEND);
                            fos.write(newContents.getBytes());
                            fos.flush();
                            fos.close();
                            Toast.makeText(getContext(), "QR Verified", Toast.LENGTH_LONG).show();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
