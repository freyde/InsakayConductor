package com.insakay.conductor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



/**
 * A simple {@link Fragment} subclass.
 */
public class LocationFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private View mView;
    private Double lat, lng;
    private BroadcastReceiver broadcastReceiver;
    private Boolean firstRun = true;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver !=  null) {
            getActivity().unregisterReceiver(broadcastReceiver);
        }
    }

    public LocationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mView =  inflater.inflate(R.layout.fragment_location, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView) mView.findViewById(R.id.map);

        if(broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    lat =(Double) intent.getExtras().get("latitude");
                    lng = (Double) intent.getExtras().get("longitude");
                    if(lat != null && lng != null) {
                        mMapView.getMapAsync(LocationFragment.this);
                    }
                }
            };
        }

        if(mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
        }

        getActivity().registerReceiver(broadcastReceiver, new IntentFilter("location_update"));

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.clear();
        mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
        mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if(lat != null && lng != null) {
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Your Current Location"));
            CameraPosition test = CameraPosition.builder().target(new LatLng(lat, lng)).zoom(15).bearing(0).tilt(45).build();
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(test));
            if(getContext() != null)
                MapsInitializer.initialize(getContext());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

//        LatLng latLng = new LatLng(14.195862, 120.878176);
}
