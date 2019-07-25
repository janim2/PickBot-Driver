package com.pickbot.pickbotdriver.historyRecyclerView;

public class HistoryObject {
    String rideId;
    String time;

    public HistoryObject(String rideId,String time) {

        this.rideId = rideId;
        this.time = time;
    }

    public String getRideId() {
        return rideId;
    }

    public String getTime() {
        return time;
    }

}
