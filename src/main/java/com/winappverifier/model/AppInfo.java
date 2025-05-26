package com.winappverifier.model;

public class AppInfo {
    private String sw_id;

    public String getSw_id() {
        return sw_id;
    }

    public void setSw_id(String sw_id) {
        this.sw_id = sw_id;
    }

    @Override
    public String toString() {
        return "AppInfo{sw_id='" + sw_id + "'}";
    }
}
