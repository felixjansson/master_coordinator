package com.master_thesis.coordinator.data;

import java.math.BigInteger;

public class LinearClientData extends LinearPublicData {

    private BigInteger[] sk;

    public LinearClientData(BigInteger n, BigInteger NRoof, BigInteger g, BigInteger g1, BigInteger[] sk) {
        super(n, NRoof, g, g1);
        this.sk = sk;
    }

    public LinearClientData(LinearClientData linearClientData, BigInteger[] hVector, BigInteger prime) {
        super(linearClientData, hVector, prime);
        this.sk = linearClientData.sk;
    }

    public BigInteger[] getSk() {
        return sk;
    }
}
