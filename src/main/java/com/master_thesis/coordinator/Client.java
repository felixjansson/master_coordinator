package com.master_thesis.coordinator;

public class Client {

    private int clientID;
    private int substationID;
    private int fid;

    public Client(int clientID, int substationID, int fid) {
        this.clientID = clientID;
        this.substationID = substationID;
        this.fid = fid;
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

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }
}
