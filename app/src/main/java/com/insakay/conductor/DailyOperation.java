package com.insakay.conductor;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class DailyOperation extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView fileList;
    private ArrayList<String> dateName;
    public ArrayAdapter filesAdapter;
    private HashMap<String, String> filenames;
    private TextView noFile;
    private StorageReference storageReference;
    private DeleteConfirm deleteConfirmFragment;
    private Boolean deleteResp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_operation);

        storageReference = FirebaseStorage.getInstance().getReference();

        fileList = (ListView) findViewById(R.id.file_list_view);
        toolbar = (Toolbar) findViewById(R.id.toolbarDailyOp);
        noFile = (TextView) findViewById(R.id.no_file);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Daily Operation");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dateName = new ArrayList<String>();
        filenames = new HashMap<String, String>();
        deleteConfirmFragment = new DeleteConfirm();

        update();

        if(dateName.size() != 0) {
            fileList.setAdapter(filesAdapter);
            noFile.setVisibility(View.INVISIBLE);
            fileList.setVisibility(View.VISIBLE);
        } else {
            noFile.setVisibility(View.VISIBLE);
            fileList.setVisibility(View.INVISIBLE);
        }

        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String textDate = parent.getItemAtPosition(position).toString();
                final String fName = filenames.get(textDate);
                AlertDialog.Builder alert =  new AlertDialog.Builder(DailyOperation.this);

                alert.setTitle(R.string.file_options)
                    .setMessage(parent.getItemAtPosition(position).toString() +"\n"+ fName)
                    .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri file = Uri.fromFile(new File(DailyOperation.this.getFilesDir(), fName));
                            final String conductorName = SaveSharedPreference.getConductorName(getApplicationContext());
                            final String opUID = SaveSharedPreference.getOpUID(getApplicationContext());
                            final HashMap<String, String> name = new HashMap<String, String>();
                            StorageReference operatorDirectory = storageReference.child(opUID +"/daily_reports/"+ fName.replace(".sky", ".csv"));
                            operatorDirectory.putFile(file)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                    String conductorID = SaveSharedPreference.getConductorID(getApplicationContext());
                                    String[] a = fName.split("_");
                                    String[] b = a[3].split("\\.");
                                    String date = a[1]+"-"+a[2]+"-"+b[0];

                                    FirebaseDatabase.getInstance().getReference("users/"+ opUID +"/reports/"+ conductorName.replace(" ", "_").replace(".", "-") +"/"+ date +"/fileName").setValue(fName.replace(".sky", ".csv"));
                                    Toast.makeText(DailyOperation.this, "Upload Successful.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(DailyOperation.this, "Upload Failed",  Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteConfirmFragment.setFile(new File(DailyOperation.this.getFilesDir(), fName));
                            deleteConfirmFragment.setPrevActivity(DailyOperation.this);
                            deleteConfirmFragment.show(getSupportFragmentManager(), "delete confirm");

                            update();
                            filesAdapter.notifyDataSetChanged();
                        }
                    }).create().show();
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void update() {
        dateName.clear();
        filenames.clear();
        String pattern = "MM_dd_yy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String path =this.getFilesDir().getPath();
        File directory = new File(path);
        File[] files = directory.listFiles();
        String condID = SaveSharedPreference.getConductorID(getApplicationContext());
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            if(name.endsWith(".sky") && name.startsWith(condID)) {
                String[] temp = name.split("_");
                String a = temp[1].concat("_").concat(temp[2]).concat("_").concat(temp[3]);
                String raw = "";
                try {
                    raw = simpleDateFormat.parse(a).toString();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String finalName = raw.replace(" 00:00:00 GMT", ",");
                dateName.add(finalName);
                filenames.put(finalName, name);
            }
        }
        filesAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, dateName);
    }
}
