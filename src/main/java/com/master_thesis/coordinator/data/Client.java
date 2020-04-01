package com.master_thesis.coordinator.data;

public class Client {

    private int clientID;
    private int substationID;
    private int startFid;

    public Client(int clientID, int substationID, int startFid) {
        this.clientID = clientID;
        this.substationID = substationID;
        this.startFid = startFid;
    }

    public int getSubstationID() {
        return substationID;
    }

    public void setSubstationID(int substationID) {
        this.substationID = substationID;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public boolean isConnectedTo(int substationID) {
        return this.substationID == substationID;
    }

    public int getStartFid() {
        return startFid;
    }

    public void setStartFid(int startFid) {
        this.startFid = startFid;
    }
}
