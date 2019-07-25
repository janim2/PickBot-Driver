package com.pickbot.pickbotdriver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Driver_map extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener{

    public GoogleMap mMap;
    LinearLayout locatemelayout;
    LocationManager locationManager;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    SupportMapFragment mapFragment;
    String customerId = "", destination;
    LatLng destinationLatLng;
    Boolean isLoggingout = false;
    LinearLayout mcustomerinfo;
    ImageView customerprofileimage;
    TextView customerdestination,customername,customernumber;
    Button msettings,rideStatus;
    int status = 0;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    Switch workingSwitch;
    float ridedistance;
    Button history;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        //trying to change the status bar background color
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(Driver_map.this, R.color.grey));

        //checking if app is killed or not//
        startService(new Intent(Driver_map.this,OnAppKilled.class));
//        checking stops here

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Driver_map.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }else {
            mapFragment.getMapAsync(this);
        }

        mcustomerinfo = (LinearLayout)findViewById(R.id.customerInfo);
        customerdestination = (TextView) findViewById(R.id.customerdestination);
        customerprofileimage = (ImageView) findViewById(R.id.customerprofileimage);
        customername = (TextView) findViewById(R.id.customername);
        customernumber = (TextView) findViewById(R.id.customerphone);
        msettings = (Button) findViewById(R.id.settings);
        rideStatus = (Button) findViewById(R.id.rideStatus);
        workingSwitch = (Switch) findViewById(R.id.workingSwitch);
        history = (Button) findViewById(R.id.history);

        workingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    connectDriver();
                }else{
                    disconnectDriver();
                }
            }
        });
        polylines = new ArrayList<>();

        rideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (status){
                    case 1:
                        status = 2;
                        erasePolylines();
                        if(destinationLatLng.latitude != 0.0 && destinationLatLng.longitude != 0.0 && destinationLatLng != null){
                            getRouteToMarker(destinationLatLng);
                        }
                        rideStatus.setText("Drive Completed");
                        break;
                    case 2:
                        recordRide();
                        endRide();
//                        status = 0;
                        break;
                }
            }
        });
        msettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Driver_map.this,Fake_settings.class));
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent history = new Intent(Driver_map.this,fake_history.class);
                history.putExtra("customerOrDriver", "Drivers");
                startActivity(history);
            }
        });


//        logout.setOnClickListener(new On..){
//            isLoggingout = true;
//            disconnectDriver();
//            FirebaseAuth.getInstance().signOut();
//            startActivity(new Intent(Driver_map.this,Fake_login.class));
//        }
        getAssignedCustomer();
    }

    private void endRide() {
            rideStatus.setText("Pick Customer");
            erasePolylines();

            String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference driverref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userid).child("customerRequest");
            driverref.removeValue();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
            GeoFire geoFire = new GeoFire(ref);
            geoFire.removeLocation(customerId, new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String s, DatabaseError databaseError) {

                }
            });
            customerId = "";
            ridedistance = 0;

            if(pickupMarker !=  null){
                pickupMarker.remove();
            }
            if(assignedcustomerpickuplocationRefListener != null){
                assignedcustomerpickuplocationRef.removeEventListener(assignedcustomerpickuplocationRefListener);
            }
            mcustomerinfo.setVisibility(View.GONE);
            customername.setText("");
            customernumber.setText("");
            customerdestination.setText("Destination: --");
            customerprofileimage.setImageResource(R.drawable.profile);
    }

    private void recordRide() {
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userid).child("history");
        DatabaseReference customerref = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId).child("history");
        DatabaseReference historyref = FirebaseDatabase.getInstance().getReference().child("history");

        String requestID = historyref.push().getKey();
        driverref.child(requestID).setValue(true);
        customerref.child(requestID).setValue(true);

        HashMap map = new HashMap();
        map.put("driver",userid);
        map.put("customer",customerId);
        map.put("rating",0);
        map.put("timestamp",getCurrentTimestamp());
        map.put("destination",destination);
        map.put("location/from/lat",pickuplatlng.latitude);
        map.put("location/from/lng",pickuplatlng.longitude);
        map.put("location/to/lat",destinationLatLng.latitude);
        map.put("location/to/lng",destinationLatLng.longitude);
        map.put("distance",ridedistance);
        historyref.child(requestID).updateChildren(map);
    }

    private Object getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }


    private void getAssignedCustomer() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedcustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("customerRideId");
        assignedcustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    status = 1;
                    customerId = dataSnapshot.getValue().toString();
                    getassignedCustomerpickupLocation();
                    getassignedCustomerDestination();
                    getassignedCustomerInfo();
                }else{
                    endRide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getassignedCustomerDestination() {
            String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference assignedcustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest");
            assignedcustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        Map<String,Object> map = (Map<String,Object>) dataSnapshot.getValue();
                        if(map.get("destination") != null){
                            destination = map.get("destination").toString();
                            customerdestination.setText("Destination: "+ destination);
                        }else{
                            customerdestination.setText("Destination: --");
                        }

                        Double destinationLat = 0.0;
                        Double destinationLng = 0.0;
                        if(map.get("destinationLat") != null){
                            destinationLat = Double.valueOf(map.get("destinationLat").toString());
                        }
                        if(map.get("destinationLng") != null){
                            destinationLng = Double.valueOf(map.get("destinationLng").toString());
                            destinationLatLng = new LatLng(destinationLat,destinationLng);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

    private void getassignedCustomerInfo() {
        mcustomerinfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
        mCustomerdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
//                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        if(map.get("first_name") != null && map.get("last_name") != null ){
                            customername.setText(map.get("first_name").toString() + " " + map.get("last_name").toString() );
                        }
                        if(map.get("phone") != null){
                            customernumber.setText(map.get("phone").toString());
                        }
                        if(map.get("profileImageUrl") != null){
                            Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(customerprofileimage);
                        }
                    }else{

                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

    Marker pickupMarker;
    DatabaseReference assignedcustomerpickuplocationRef;
    ValueEventListener assignedcustomerpickuplocationRefListener;
    double locationlat = 0;
    double locationlong = 0;
    LatLng pickuplatlng = new LatLng(locationlat,locationlong);
    private void getassignedCustomerpickupLocation() {
        assignedcustomerpickuplocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
        assignedcustomerpickuplocationRefListener = assignedcustomerpickuplocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !customerId.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();

                    if(map.get(0) != null){
                        locationlat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationlong = Double.parseDouble(map.get(1).toString());
                    }

                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickuplatlng).title("Pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.pin_)));
                    getRouteToMarker(pickuplatlng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getRouteToMarker(LatLng pickuplatlng) {
        Routing routing = new Routing.Builder()
                .key("AIzaSyAnUcHSUb1jZHI6I9JDrAEDH8Q71Tg0hwE")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),pickuplatlng).build();
        routing.execute();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API
        ).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext() != null){

            if(!customerId.equals("")){
                ridedistance += mLastLocation.distanceTo(location)/1000;
            }
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

//        update location of driver
        String  userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
        DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking");
        GeoFire geoFireAvailable = new GeoFire(refAvailable);
        GeoFire geoFireworking = new GeoFire(refWorking);

            switch (customerId){
                case "":
//                    geoFireworking.removeLocation(userid);
                    geoFireAvailable.setLocation(userid, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String s, DatabaseError databaseError) {
                        }
                    });
                    break;

                default:
//                    geoFireAvailable.removeLocation(userid);
                    geoFireworking.setLocation(userid, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String s, DatabaseError databaseError) {
                        }
                    });
                    break;
            }
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        public void connectDriver(){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Driver_map.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


        }

    public void disconnectDriver(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userid, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String s, DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onConnectionSuspended(int i) {


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    final int LOCATION_REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mapFragment.getMapAsync(this);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Turn Location On",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null){
            Toast.makeText(Driver_map.this,"Error" + e.getMessage(),Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(Driver_map.this,"Something Went Wrong, Try Again",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();

            }
        }

    @Override
    public void onRoutingCancelled() {

    }

    private void erasePolylines(){
        for(Polyline line: polylines){
            line.remove();
        }
        polylines.clear();
    }
}
