package com.master_thesis.coordinator.data;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Buffer {

    private Map<Integer, Substation> substations;

    public Buffer() {
        this.substations = new HashMap<>();
    }

    public void put(IncomingNonceData body) {
        substations.putIfAbsent(body.getSubstationID(), new Substation());
        substations.get(body.getSubstationID()).putIfAbsent(body.getFid(), new Fid());
        substations.get(body.getSubstationID()).get(body.getFid()).put(body.getId(), body.getNonce());
    }

    public int getNumberOfNonce(int substationID, int fid) {
        return substations.get(substationID).get(fid).size();
    }

    public BigInteger getNonceSum(int substationID, int fid) {
        return substations.get(substationID).get(fid).values().stream().reduce(BigInteger::add).get();
    }

    public boolean contains(int substationID, int fid) {
        return substations.containsKey(substationID) && substations.get(substationID).containsKey(fid);
    }

    private class Substation extends HashMap<Integer, Fid> {

    }

    private class Fid extends HashMap<Integer, BigInteger> {

    }
}
