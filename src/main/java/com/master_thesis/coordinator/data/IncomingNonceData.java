package com.master_thesis.coordinator.data;

import java.math.BigInteger;

public class IncomingNonceData {

    private int substationID, fid, id;
    private BigInteger nonce;

    public int getSubstationID() {
        return substationID;
    }

    public void setSubstationID(int substationID) {
        this.substationID = substationID;
    }

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        return "IncomingNonceData{" +
                "substationID=" + substationID +
                ", fid=" + fid +
                ", clientID=" + id +
                ", nonce=" + nonce +
                '}';
    }
}
