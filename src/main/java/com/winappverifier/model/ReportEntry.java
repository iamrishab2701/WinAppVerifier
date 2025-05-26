package com.winappverifier.model;

public class ReportEntry {
    private String swId;
    private String name;
    private String expectedVersion;
    private String actualVersion;
    private boolean installed;
    private Boolean versionMatch; // null = not checked
    private String launchStatus;
    private String closeStatus;

    public ReportEntry(String swId, String name) {
        this.swId = swId;
        this.name = name;
    }

    // Getters and setters

    public String getSwId() { return swId; }
    public void setSwId(String swId) { this.swId = swId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExpectedVersion() { return expectedVersion; }
    public void setExpectedVersion(String expectedVersion) { this.expectedVersion = expectedVersion; }

    public String getActualVersion() { return actualVersion; }
    public void setActualVersion(String actualVersion) { this.actualVersion = actualVersion; }

    public boolean isInstalled() { return installed; }
    public void setInstalled(boolean installed) { this.installed = installed; }

    public Boolean getVersionMatch() { return versionMatch; }
    public void setVersionMatch(Boolean versionMatch) { this.versionMatch = versionMatch; }

    public String getLaunchStatus() { return launchStatus; }
    public void setLaunchStatus(String launchStatus) { this.launchStatus = launchStatus; }

    public String getCloseStatus() { return closeStatus; }
    public void setCloseStatus(String closeStatus) { this.closeStatus = closeStatus; }
}