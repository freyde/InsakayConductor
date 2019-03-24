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
    private AESencrp decryptor;
    private String contents;
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
//        contents = "";
        try {
            contents = decryptor.decrypt(result.getContents());
            if (result!=null && result.getContents()!=null) {
                final SimpleDateFormat dateFormat = new SimpleDateFormat("MM_dd_yy");
                final String curDate = dateFormat.format(new Date());
                final String[] infos = contents.split("\\.");
                System.out.println(contents);
                if(infos.length > 1) {
                    System.out.println(infos[5] +"_"+ curDate);
                    if(infos[5].equals(curDate)) {
                        String p;
                        if(infos[7].equals("Exact"))
                            p = infos[7];
                        else
                            p = infos[7] + "php";
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Ticket Information")
                                .setMessage(
                                        "Operator: "+ infos[1] +
                                        "\nRoute: "+ infos[2] +
                                        "\nOrigin: "+ infos[3] +
                                        "\nDestination: "+ infos[4] +
                                        "\nFare: "+ infos[6] +" php"+
                                        "\nPayment: "+ p +
                                        "\nChange: "+ infos[8] +" php"
                                )
                                .setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String conductorID = SaveSharedPreference.getConductorID(getActivity().getApplicationContext());
                                        String fileName = conductorID.concat("_").concat(curDate).concat(".sky");
                                        String newContents ="";
                                        try {
                                            String root = getActivity().getFilesDir().getPath();
                                            if(new File(root, fileName).exists()) {
                                                newContents = "\n".concat(infos[1] +", "+ infos[2] +", "+ infos[3] +", "+ infos[4] +", "+ infos[6]);
                                            } else {
                                                newContents = infos[1] +", "+ infos[2] +", "+ infos[3] +", "+ infos[4] +", "+ infos[6];
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
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Error")
                                .setMessage("Outdated QR Code")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();
                    }
                } else if(infos[0].equals("Insakay")) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Insakay")
                            .setMessage("Thank you for using insakay!!")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new AlertDialog.Builder(getActivity())
                    .setTitle("Error")
                    .setMessage("Invalid QR Code")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        }

        System.out.println(contents);

        super.onActivityResult(requestCode, resultCode, data);
    }

}
