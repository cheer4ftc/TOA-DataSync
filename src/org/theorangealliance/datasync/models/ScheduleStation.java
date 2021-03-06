package org.theorangealliance.datasync.models;

/**
 * Created by Kyle Flynn on 11/29/2017.
 */
public class ScheduleStation {

    private String stationKey;
    private String matchKey;
    private int station;
    private int stationStatus;
    private int teamKey;

    public ScheduleStation(String matchKey, int station, int teamKey) {
        this.matchKey = matchKey;
        this.station = station;
        this.teamKey = teamKey;
        this.stationKey = this.matchKey + "-" + getStationSuffix();
        this.stationStatus = 1;
    }

    private String getStationSuffix() {
        String suffix = "";
        switch (station) {
            case 11:
                suffix = "R1";
                break;
            case 12:
                suffix = "R2";
                break;
            case 13:
                suffix = "R3";
                break;
            case 21:
                suffix = "B1";
                break;
            case 22:
                suffix = "B2";
                break;
            case 23:
                suffix = "B3";
                break;
        }
        return suffix;
    }

    public String getStationKey() {
        return stationKey;
    }

    public void setStationKey(String stationKey) {
        this.stationKey = stationKey;
    }

    public String getMatchKey() {
        return matchKey;
    }

    public void setMatchKey(String matchKey) {
        this.matchKey = matchKey;
    }

    public int getStation() {
        return station;
    }

    public void setStation(int station) {
        this.station = station;
    }

    public int getStationStatus() {
        return stationStatus;
    }

    public void setStationStatus(int stationStatus) {
        this.stationStatus = stationStatus;
    }

    public int getTeamKey() {
        return teamKey;
    }

    public void setTeamKey(int teamKey) {
        this.teamKey = teamKey;
    }
}
