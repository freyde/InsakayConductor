package com.insakay.conductor;


import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ManualTicket extends DialogFragment {

    private View view;
    private Spinner routeSpinner, originSpinner, destinationSpinner;
    private List<String> routeList, landmarksList;
    private ArrayAdapter<String> routeAdapter, landmarksAdapter;
    private HashMap<String, String> routeHash, landmarksHash, landmarksCov;
//    private String[] routeArr, landmarkArr;
    private String UID, operator, route, origin, destination, curDate, fileName, content, uid, routeID, originCov, destinationCov;
    private Boolean found;
    private TextView fareView;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        view = inflater.inflate(R.layout.fragment_manual_ticket, null);

        routeSpinner = (Spinner) view.findViewById(R.id.spinner_route);
        originSpinner = (Spinner) view.findViewById(R.id.spinner_origin);
        destinationSpinner = (Spinner) view.findViewById(R.id.spinner_destination);
        fareView = (TextView) view.findViewById(R.id.fareView);


        routeList = new ArrayList<>();
        routeHash = new HashMap<String, String>();
        landmarksList = new ArrayList<>();
        landmarksHash = new HashMap<String, String>();
        landmarksCov = new HashMap<String, String>();

        UID = SaveSharedPreference.getOpUID(getActivity().getApplicationContext());

        updateRoutes();

        landmarksList.add("Select Landmark");

        routeAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, routeList);
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routeSpinner.setAdapter(routeAdapter);

        landmarksAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, landmarksList);
        landmarksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        originSpinner.setAdapter(landmarksAdapter);
        destinationSpinner.setAdapter(landmarksAdapter);

        originSpinner.setEnabled(false);
        destinationSpinner.setEnabled(false);

        routeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fareView.setText("");
                landmarksList.clear();
                landmarksList.add("Select Landmark");
                String route = parent.getItemAtPosition(position).toString();
                found = false;
                if(!route.equals("Select Route")) {
                    String routeID = routeHash.get(route);

                    FirebaseDatabase.getInstance().getReference("users/" + UID + "/landmarks/" + routeID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        String name = snapshot.child("landmarkName").getValue().toString();
                                        String id = snapshot.child("landmarkID").getValue().toString();
                                        String cov = snapshot.child("coverage").getValue().toString();
                                        landmarksList.add(name);
                                        landmarksHash.put(name, id);
                                        landmarksCov.put(name, cov);
                                    }
                                    Collections.sort(landmarksList.subList(1, landmarksList.size()));
                                    landmarksAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                    originSpinner.setEnabled(true);
                    destinationSpinner.setEnabled(true);

                } else {
                    originSpinner.setEnabled(false);
                    destinationSpinner.setEnabled(false);
                    landmarksAdapter.notifyDataSetChanged();
                    landmarksList.add("Select Landmark");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        originSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                route = routeSpinner.getSelectedItem().toString();
                destination = destinationSpinner.getSelectedItem().toString();
                origin = parent.getItemAtPosition(position).toString();
                if(!destination.equals("Select Landmark")) {
                    routeID = routeHash.get(route);
                    originCov = landmarksCov.get(origin);
                    destinationCov = landmarksCov.get(destination);
                    FirebaseDatabase.getInstance().getReference("users/"+ UID +"/fares/"+ routeID +"/matrix/"+ originCov)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    System.out.println(dataSnapshot);
                                    System.out.println(destinationCov);
                                    String fare = dataSnapshot.child(destinationCov).getValue().toString();
                                    System.out.println(fare);
                                    fareView.setText("Fare: Php. " + fare);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        destinationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                route = routeSpinner.getSelectedItem().toString();
                origin = originSpinner.getSelectedItem().toString();
                destination = parent.getItemAtPosition(position).toString();
                if(!destination.equals("Select Landmark")) {
                    routeID = routeHash.get(route);
                    originCov = landmarksCov.get(origin);
                    destinationCov = landmarksCov.get(destination);
                    FirebaseDatabase.getInstance().getReference("users/"+ UID +"/fares/"+ routeID +"/matrix/"+ originCov)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                System.out.println(dataSnapshot);
                                System.out.println(destinationCov);
                                String fare = dataSnapshot.child(destinationCov).getValue().toString();
                                System.out.println(fare);
                                fareView.setText("Fare: Php. " + fare);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        builder.setTitle(getString(R.string.qr_scan_manual_ticketing))
                .setView(view)
                .setPositiveButton(getString(R.string.proceed), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        route = routeSpinner.getSelectedItem().toString();
                        origin = originSpinner.getSelectedItem().toString();
                        destination = destinationSpinner.getSelectedItem().toString();
                        final Activity activity = getActivity();
                        if(!route.equals("Select Route") && !origin.equals("Select Landmark") && !destination.equals("Select Landmark")) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
                            curDate = dateFormat.format(new Date());
                            String conductorID = SaveSharedPreference.getConductorID(getActivity().getApplicationContext());
                            fileName = conductorID.concat("_").concat(curDate).concat(".sky");

                            dateFormat = new SimpleDateFormat("MM.dd.yy");
                            curDate = dateFormat.format(new Date());

                            FirebaseDatabase.getInstance().getReference("users/" + UID + "/info")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    operator = dataSnapshot.child("shortName").getValue().toString();
                                    content = operator.concat(", ").concat(route).concat(", ").concat(origin).concat(", ").concat(destination).concat(", ").concat(curDate).concat("\n");

                                    FirebaseDatabase.getInstance().getReference("users/" + UID + "/landmarks/" + routeID)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    System.out.println("Landmarks "+ dataSnapshot);
                                                    for(DataSnapshot marks : dataSnapshot.getChildren()) {
                                                        if(destination.equals(marks.child("landmarkName").getValue())) {
                                                            System.out.println(destination +" "+ marks.child("landmarkName").getValue());
                                                            String path = getActivity().getFilesDir().getPath();
                                                            File a = new File(path, "destinationList"+ fileName);
                                                            if(!a.exists()) {

                                                            } else {


                                                                try {
                                                                    FileOutputStream fos = activity.openFileOutput("destinationList-" + fileName, Context.MODE_APPEND);
                                                                    String content = origin + " " + destination + ", " + marks.child("coordinate").child("lat").getValue().toString() + ", " + marks.child("coordinate").child("lng").getValue().toString();
                                                                    fos.write(content.getBytes());
                                                                    fos.flush();
                                                                    fos.close();
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

                                    try {
                                        FileOutputStream fos = activity.openFileOutput(fileName, Context.MODE_APPEND);
                                        fos.write(content.getBytes());
                                        fos.flush();
                                        fos.close();
                                        Toast.makeText(activity, "Manual Ticketing Successful!", Toast.LENGTH_SHORT).show();
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                                });
                        } else {
                            Toast.makeText(activity, "Manual Ticketing Failed. Please check your input.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })

                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return builder.create();
    }

    private void updateRoutes() {
        routeList.add("Select Route");
        FirebaseDatabase.getInstance().getReference("users/" + UID + "/routes")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String name = snapshot.child("routeName").getValue().toString();
//                        String key = snapshot.getKey();
                        String id = snapshot.child("routeID").getValue().toString();
                        routeList.add(name);
                        routeHash.put(name, id);
//                        routeKeys.put(name, key);
                    }
                    Collections.sort(routeList.subList(1, routeList.size()));
                    routeAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }
}