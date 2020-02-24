package com.master_thesis.coordinator;

public class Client {

    private int clientID;
    private int transformatorID;

    public Client(int clientID, int transformatorID) {
        this.clientID = clientID;
        this.transformatorID = transformatorID;
    }

    public int getTransformatorID() {
        return transformatorID;
    }

    public void setTransformatorID(int transformatorID) {
        this.transformatorID = transformatorID;
    }


    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public boolean isConnectedTo(int transformatorID) {
        return this.transformatorID == transformatorID;
    }

}
