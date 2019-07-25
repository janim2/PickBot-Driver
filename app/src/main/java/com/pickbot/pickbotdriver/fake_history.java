package com.pickbot.pickbotdriver;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pickbot.pickbotdriver.historyRecyclerView.HistoryAdapter;
import com.pickbot.pickbotdriver.historyRecyclerView.HistoryObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class fake_history extends Activity {
    RecyclerView mHistoryRecycyclerView;
    RecyclerView.Adapter mHistoryAdapter;
    RecyclerView.LayoutManager mHistoryLayoutManager;
    String customerOrDriver, userid;
    TextView mbalance;
    Double balance = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fake_history);

        mHistoryRecycyclerView = (RecyclerView) findViewById(R.id.myRecyclerView);
        mHistoryRecycyclerView.setHasFixedSize(true);

        mHistoryLayoutManager = new LinearLayoutManager(this);
        mHistoryRecycyclerView.setLayoutManager(mHistoryLayoutManager);

        mbalance = (TextView)findViewById(R.id.balance);

        customerOrDriver = getIntent().getExtras().getString("customerOrDriver");
        userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();

        mHistoryAdapter = new HistoryAdapter(getDataSetHistory(), this);
        mHistoryRecycyclerView.setAdapter(mHistoryAdapter);

        if(customerOrDriver.equals("Drivers")){
            mbalance.setVisibility(View.VISIBLE);
        }
    }


    private void getUserHistoryIds() {
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(customerOrDriver).child(userid).child("history");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot history : dataSnapshot.getChildren()) {
                        FetchRideInformation(history.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //
    private void FetchRideInformation(String Ridekey) {
        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(Ridekey);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String rideId = dataSnapshot.getKey();
                    Long timestamp = 0L;
                    String distance = "";
                    Double ridePrize = 0.0;
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.getKey().equals("timestamp")){
                            timestamp = Long.valueOf(child.getValue().toString());
                        }
                    }

                    if(dataSnapshot.child("customerPaid").getValue() != null && dataSnapshot.child("driverPaidOut").getValue()  == null){
                        if(dataSnapshot.child("distance").getValue() != null){
                            distance = dataSnapshot.child("distance").getValue().toString();
                            ridePrize = Double.valueOf(distance) * 0.4;
                            balance += ridePrize;
                            mbalance.setText("Balance: " + "$ "+ String.valueOf(balance));

                        }
                    }
                    HistoryObject obj = new HistoryObject(rideId,getDate(timestamp));
                    resultHistory.add(obj);
                    mHistoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @TargetApi(Build.VERSION_CODES.N)
    private String getDate(Long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm",cal).toString();
        return date;
    }

    ArrayList resultHistory = new ArrayList<HistoryObject>();

    public ArrayList<HistoryObject> getDataSetHistory() {
        return resultHistory;
    }
}
