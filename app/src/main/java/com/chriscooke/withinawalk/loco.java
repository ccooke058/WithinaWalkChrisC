package com.chriscooke.withinawalk;

import java.io.Serializable;

/**
 * Created by Chris_Home on 01/12/15.
 */
public class loco implements Serializable{

    int itemID;

    double lat;

    double lng;

    String Name;

    String address;

    String phoneNum;

    public loco(){}

    public loco(int itemID, double lat, double lng, String Name, String address, String phoneNum){
        this.itemID = itemID;
        this.lat = lat;
        this.lng = lng;
        this.Name = Name;
        this.address = address;
        this.phoneNum = phoneNum;

    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }



}
