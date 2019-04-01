package com.insakay.conductor;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class locationService extends Service {
    public static final String CHANNEL_1_ID = "channel";
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String conductorID, busID, operatorID, fileName;
    private Boolean loggedIn, updated;
    private HashMap<String, String> coordinate = new HashMap<String, String>();
    private List<String> markingsList = new ArrayList<String>();
    private NotificationManagerCompat notificationManager;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        context = getApplicationContext();
        notificationManager = NotificationManagerCompat.from(getApplicationContext());

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent i = new Intent("location_update");
                i.putExtra("latitude", location.getLatitude());
                i.putExtra("longitude", location.getLongitude());
                sendBroadcast(i);

                operatorID = SaveSharedPreference.getOpUID(getApplicationContext());
                busID = SaveSharedPreference.getBusID(getApplicationContext());
                conductorID = SaveSharedPreference.getConductorID(getApplicationContext());
                loggedIn = SaveSharedPreference.isLoggedIn(getApplicationContext());

                fileName = setFileName();

                if(location != null && conductorID != "" && loggedIn) {
                    //Update Location On Firebase

                    //Prepare Datas
                    coordinate.put("lat", Double.toString(location.getLatitude()));
                    coordinate.put("long", Double.toString(location.getLongitude()));
                    coordinate.put("conductorID", conductorID);
                    coordinate.put("operatorID", operatorID);
                    coordinate.put("busID", busID);

                    //Initialize Firebase and Update
                    FirebaseDatabase.getInstance().getReference("onOperation/" + conductorID).setValue(coordinate);

                    updated = false;
                    markingsList.clear();
                    String path = getFilesDir().getPath();
                    File a = new File(path, "destinationList-"+ fileName);
                    FileInputStream fis2 = null;
                    if(a.exists()) {
                        try {
                            fis2 = openFileInput("destinationList-"+ fileName);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(fis2));
                            String line = "";
                            while ((line = reader.readLine()) != null) {
                                String[] main = line.split("=");
                                String[] datas = main[0].split("_");
                                Double[] curLoc = new Double[]{location.getLatitude(), location.getLongitude()};
                                Double[] marking = new Double[]{Double.parseDouble(datas[2]), Double.parseDouble(datas[3])};
                                if (getDistance(curLoc, marking) <= 50D) {
                                    sendNotification(datas[0], main[1]);
                                    updated = true;
                                } else {
                                    markingsList.add(line);
                                }
                            }
                            reader.close();
                            System.out.println("Len: "+ markingsList.size());
                            if(updated) {
                                FileOutputStream clear = openFileOutput("destinationList-" + fileName, Context.MODE_PRIVATE);
                                clear.close();
                                FileOutputStream fos = openFileOutput("destinationList-" + fileName, Context.MODE_APPEND);
                                for (String m : markingsList) {
                                    fos.write(m.concat("\n").getBytes());
                                }
                                fos.flush();
                                fos.close();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                    sendNotification();
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        System.out.println("Service started");
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG);
        if(locationManager != null) {
            locationManager.removeUpdates(locationListener);
            FirebaseDatabase.getInstance().getReference("onOperation/" + conductorID).removeValue();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Embark Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notify Passenger!");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public void sendNotification(String landmark, String passCount) {
        String mes;
        if(Integer.parseInt(passCount) > 1)
            mes = " passengers need to embark at ";
        else
            mes = " passenger need to embark at ";
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentTitle("Embark Notification")
                .setContentText(passCount + mes + landmark)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .build();

        notificationManager.notify(1, notification);
    }

    public Double getDistance(Double[] origin, Double[] destination) {
        // return distance in meters
        Double lon1 = toRadian(origin[1]);
        Double lat1 = toRadian(origin[0]);
        Double lon2 = toRadian(destination[1]);
        Double lat2 = toRadian(destination[0]);

        Double deltaLat = lat2 - lat1;
        Double deltaLon = lon2 - lon1;

        Double a = Math.pow(Math.sin(deltaLat/2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLon/2), 2);
        Double c = 2 * Math.asin(Math.sqrt(a));
        Double EARTH_RADIUS = 6371D;
        return c * EARTH_RADIUS * 1000;
    }

    public Double toRadian(Double degree) {
        return degree*Math.PI/180;
    }

    private String setFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM_dd_yy");
        String date = dateFormat.format(new Date());
        return SaveSharedPreference.getConductorID(context).concat("_").concat(date).concat(".sky");
    }
}
