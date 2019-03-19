package com.insakay.conductor;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseInstance extends Application {

    public void FirebaseInstance() throws IOException {

        FileInputStream serviceAccount =
                new FileInputStream("InsakayConductor/app/insakay-198614-firebase-adminsdk-mrk72-3762d200df.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("insakay-198614")
                .setDatabaseUrl("https://insakay-198614.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(this);

    }
}
