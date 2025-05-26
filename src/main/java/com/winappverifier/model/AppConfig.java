package com.winappverifier.model;

public class AppConfig {
    private String sw_id;
    private String name;
    private String path;
    private String process;

    public String getSw_id() {
        return sw_id;
    }

    public void setSw_id(String sw_id) {
        this.sw_id = sw_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                "sw_id='" + sw_id + '\'' +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", process='" + process + '\'' +
                '}';
    }
}