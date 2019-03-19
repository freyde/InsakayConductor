package com.insakay.conductor;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class MarkingsFragment extends Fragment {

    private View mView;
    private String fileName;
    private String[] landmarkNames, landmarkCoverage, finalLandmarkNames, finalLandmarkCoverage;
    private Integer[] passengers, finalPassengers;
    private int arrLength, count;
    private ListView listView;
    private TextView noMarkings;
    private HashMap<String, Integer> passengerChecker, landmarkIndex;

    public MarkingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment

        fileName = setFileName();
        mView = inflater.inflate(R.layout.fragment_markings, container, false);
        noMarkings = (TextView) mView.findViewById(R.id.noMarkings);
        listView = (ListView) mView.findViewById(R.id.markingsList);
        listView.setAdapter(null);
        arrLength = 0; count = 0;

        passengerChecker = new HashMap<String, Integer>();
        landmarkIndex = new HashMap<String, Integer>();
        try {
            String path = getActivity().getFilesDir().getPath();

            File a = new File(path, fileName);
            if(a.exists()) {
                FileInputStream fis = getActivity().openFileInput(fileName);
                BufferedReader lineCounter = new BufferedReader(new InputStreamReader(fis));
                String temp;

                while ((temp = lineCounter.readLine()) != null) {
                    if(temp != "")
                        arrLength++;
                }
                lineCounter.close();
                fis.close();
                String line;

                landmarkNames = new String[arrLength];
                landmarkCoverage = new String[arrLength];
                passengers = new Integer[arrLength];
                FileInputStream fis2 = getActivity().openFileInput(fileName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis2));
                while ((line = reader.readLine()) != null) {
                    line.replace(" ", "");
                    String[] datas = line.split(",");
                    if(passengerChecker.get(datas[3]) == null) {
                        landmarkNames[count] = datas[3];
                        landmarkCoverage[count] = datas[2];
                        passengerChecker.put(datas[3], 1);
                        landmarkIndex.put(datas[3], count);
                        passengers[landmarkIndex.get(datas[3])] = passengerChecker.get(datas[3]);
                        count++;
                    } else {
                        int cur = passengerChecker.get(datas[3]);
                        cur++;
                        passengerChecker.put(datas[3], cur);
                        passengers[landmarkIndex.get(datas[3])] = passengerChecker.get(datas[3]);
                    }
                }
                reader.close();
                fis2.close();

                finalLandmarkNames = resizeArray(landmarkNames, count);
                finalLandmarkCoverage = resizeArray(landmarkCoverage, count);
                finalPassengers = resizeArray(passengers, count);

                System.out.println("final: "+ finalPassengers.length);

                CustomListViewAdapter markings = new CustomListViewAdapter(getActivity(), finalLandmarkNames, finalLandmarkCoverage, finalPassengers);
                markings.notifyDataSetChanged();
                listView.setAdapter(markings);
                noMarkings.setVisibility(View.INVISIBLE);
            }  else {
                listView.setVisibility(View.VISIBLE);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mView;
    }


    private String setFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
        String date = dateFormat.format(new Date());
        return SaveSharedPreference.getConductorID(getActivity().getApplicationContext()).concat("_").concat(date).concat(".sky");
    }

    private String[] resizeArray(String[] arr, int len) {
        String[] b = new String[len];
        int c = 0;
        for(String a : arr) {
            if(a != null) {
                b[c] = a;
                c++;
            }
        }

        return  b;
    }

    private Integer[] resizeArray(Integer[] arr, int len) {
        Integer[] b = new Integer[len];
        int c = 0;
        for(Integer a : arr) {
            if(a != null) {
                b[c] = a;
                c++;
            }
        }

        return  b;
    }

}
