package com.insakay.conductor;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScanFragment extends Fragment {

    private View mView;
    private Button scan, manual;
    private ManualTicket manualTicketFragment;
    private AESencrp decryptor;
    private String contents, UID, routeID, destination, markContent, fileName, coverage, newContents;
    private Activity activity;

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
        decryptor = new AESencrp();
        manualTicketFragment = new ManualTicket();
        activity = getActivity();

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

        String a = result.getContents();
        System.out.println(a);
        if(a != null) {
            try {
                contents = decryptor.decrypt(result.getContents());
                if (result != null && result.getContents() != null) {
                    final SimpleDateFormat dateFormat = new SimpleDateFormat("MM_dd_yy");
                    final String curDate = dateFormat.format(new Date());
                    final String[] infos = contents.split("\\.");
                    System.out.println(contents);
                    if (infos.length > 1) {
                        System.out.println(infos[5] + "_" + curDate);
                        if (infos[5].equals(curDate)) {
                            String p;
                            if (infos[7].equals("Exact"))
                                p = infos[7];
                            else
                                p = infos[7] + "php";
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("Ticket Information")
                                    .setMessage(
                                            "Operator: " + infos[1] +
                                                    "\nRoute: " + infos[2] +
                                                    "\nOrigin: " + infos[3] +
                                                    "\nDestination: " + infos[4] +
                                                    "\nFare: " + infos[6] + " php" +
                                                    "\nPayment: " + p +
                                                    "\nChange: " + infos[8] + " php"
                                    )
                                    .setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String conductorID = SaveSharedPreference.getConductorID(getActivity().getApplicationContext());
                                            fileName = conductorID.concat("_").concat(curDate).concat(".sky");
                                            destination = infos[4];
                                            UID = SaveSharedPreference.getOpUID(activity.getApplicationContext());

                                            FirebaseDatabase.getInstance().getReference("users/" + UID + "/routes")
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            System.out.println("Unang Firebase");
                                                            for (DataSnapshot marks : dataSnapshot.getChildren()) {
                                                                if (infos[2].equals(marks.child("routeName").getValue())) {
                                                                    routeID = marks.child("routeID").getValue().toString();
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });

                                            FirebaseDatabase.getInstance().getReference("users/" + UID + "/landmarks/" + routeID)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @RequiresApi(api = Build.VERSION_CODES.N)
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            System.out.println("Pangalawang Firebase");
                                                            for (DataSnapshot marks : dataSnapshot.getChildren()) {
                                                                System.out.println("Looping.......");
                                                                if (destination.equals(marks.child("landmarkName").getValue())) {
                                                                    String path = activity.getFilesDir().getPath();
                                                                    File a = new File(path, "destinationList-" + fileName);
                                                                    coverage = marks.child("coverage").getValue().toString();
                                                                    markContent = destination + "_" + coverage + "_" + marks.child("coordinate").child("lat").getValue().toString() + "_" + marks.child("coordinate").child("lng").getValue().toString() + "=1";
                                                                    if (!a.exists()) {
                                                                        System.out.println("Wala pa!");
                                                                        try {
                                                                            FileOutputStream fos = activity.openFileOutput("destinationList-" + fileName, Context.MODE_PRIVATE);
                                                                            fos.write(markContent.getBytes());
                                                                            fos.flush();
                                                                            fos.close();
                                                                        } catch (FileNotFoundException e) {
                                                                            e.printStackTrace();
                                                                        } catch (IOException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    } else {
                                                                        System.out.println("Meron na!");
                                                                        Boolean found = false;
                                                                        HashMap<String, String> map = new HashMap<String, String>();
                                                                        try {
                                                                            BufferedReader reader = new BufferedReader(
                                                                                    new InputStreamReader(
                                                                                            activity.openFileInput("destinationList-" + fileName)));
                                                                            String line = "", key = "";
                                                                            int c = 0;
                                                                            while ((line = reader.readLine()) != null) {
                                                                                String[] info = line.split("=");
                                                                                if (info.length > 1) {
                                                                                    System.out.println("Array: " + info[0]);
                                                                                    String[] r = info[0].split("_");
                                                                                    if (r[0].equals(destination)) {
                                                                                        found = true;
                                                                                        key = info[0];
                                                                                        c = Integer.parseInt(info[1]) + 1;

                                                                                    }
                                                                                    map.put(info[0], info[1]);
                                                                                }
                                                                            }
                                                                            System.out.println(found);
                                                                            if (found) {
                                                                                map.replace(key, Integer.toString(c));
                                                                                FileOutputStream clear = activity.openFileOutput("destinationList-" + fileName, Context.MODE_PRIVATE);
                                                                                clear.close();
                                                                                FileOutputStream fos = activity.openFileOutput("destinationList-" + fileName, Context.MODE_APPEND);
                                                                                for (Object dest : map.entrySet()) {
                                                                                    fos.write(dest.toString().concat("\n").getBytes());
                                                                                }
                                                                                fos.flush();
                                                                                fos.close();
                                                                            } else {
                                                                                FileOutputStream clear = activity.openFileOutput("destinationList-" + fileName, Context.MODE_PRIVATE);
                                                                                clear.close();
                                                                                FileOutputStream fos = activity.openFileOutput("destinationList-" + fileName, Context.MODE_APPEND);
                                                                                for (Object dest : map.entrySet()) {
                                                                                    fos.write(dest.toString().concat("\n").getBytes());
                                                                                }
                                                                                fos.write(markContent.getBytes());
                                                                                fos.flush();
                                                                                fos.close();
                                                                            }
                                                                        } catch (FileNotFoundException e) {
                                                                            e.printStackTrace();
                                                                        } catch (IOException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                        //All Scanned File-------------------------------------------------------------------------------------------------
                                                                        try {
                                                                            String root = getActivity().getFilesDir().getPath();
                                                                            if (new File(root, fileName).exists()) {
                                                                                newContents = "\n".concat(infos[1] + ", " + infos[2] + ", " + infos[3] + ", " + infos[4] + ", " + infos[6]);
                                                                            } else {
                                                                                newContents = infos[1] + ", " + infos[2] + ", " + infos[3] + ", " + infos[4] + ", " + infos[6];
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
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
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
                    } else if (infos[0].equals("Insakay")) {
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
        }
        System.out.println(contents);

        super.onActivityResult(requestCode, resultCode, data);
    }

}
