package com.master_thesis.coordinator;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Buffer {

    private Map<Integer, Map<Integer, Map<Integer, BigInteger>>> tree;

    public Buffer() {
        this.tree = new HashMap<>();
    }

    public void put(int substationID, int fid, int clientID, BigInteger nonce) {

        tree.putIfAbsent(substationID, new HashMap<>());
        tree.get(substationID).putIfAbsent(fid, new HashMap<>());
        tree.get(substationID).get(fid).put(clientID, nonce);

    }

    public int getNumberOfNonce(int substationID, int fid) {
        return tree.get(substationID).get(fid).size();
    }

    public BigInteger getNonceSum(int substationID, int fid) {
        BigInteger nonceSum = tree.get(substationID).get(fid).values().stream().reduce(BigInteger::add).get();
        // TODO: 3/26/2020 Remove the nonceSum from the tree?
        return nonceSum;
    }
}
