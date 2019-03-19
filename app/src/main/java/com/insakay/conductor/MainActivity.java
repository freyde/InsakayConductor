package com.insakay.conductor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mMainNav;
    private FrameLayout mMainFrame;
    private ScanFragment scanFragment;
    private LocationFragment locationFragment;
    private MarkingsFragment markingsFragment;
    private SignOutFragment signOutFragment;
    private ChangeBusFragment changeBusFragment;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private FragmentManager fragmentManager;
    private Toolbar toolbar;
    private Double lat = 0D, lng = 0D;

    public void startService(View v) {
        Intent locationIntent = new Intent(this, locationService.class);
        startService(locationIntent);
    }

    public void stopService(View v) {
        Intent locationIntent = new Intent(this, LocationUpdater.class);
        stopService(locationIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch  (item.getItemId()) {
            case R.id.logout:
                signOutFragment.show(getSupportFragmentManager(), "sign out");
                return true;

            case R.id.changeBus:
                changeBusFragment.show(getSupportFragmentManager(), "change bus");
                return true;

            case R.id.dailyOperation:
                startActivity(new Intent(this, DailyOperation.class));
                return true;

            case  R.id.viewFiles:
                startActivity(new Intent(this, ViewFiles.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Start Location Service Updater
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        Intent locationIntent = new Intent(this, locationService.class);
        startService(locationIntent);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializing Views
        fragmentManager = getSupportFragmentManager();
        mMainFrame = (FrameLayout) findViewById(R.id.main_frame);
        mMainNav = (BottomNavigationView) findViewById(R.id.main_nav);
        scanFragment = new ScanFragment();
        locationFragment = new LocationFragment();
        markingsFragment = new MarkingsFragment();
        signOutFragment = new SignOutFragment();
        changeBusFragment = new ChangeBusFragment();
        setFragment(locationFragment);


        //Navbar selection
        mMainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_scan :
                        setFragment(scanFragment);
                        return true;

                    case R.id.nav_location :
                        setFragment(locationFragment);
                        return true;

                    case R.id.nav_markings :
                        setFragment(markingsFragment);
                        return true;

                    default :
                        return false;
                }
            }
        });
        mMainNav.setSelectedItemId(R.id.nav_location);
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        setFragment(locationFragment);
    }
}
