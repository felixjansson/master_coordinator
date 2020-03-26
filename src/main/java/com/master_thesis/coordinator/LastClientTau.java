package com.master_thesis.coordinator;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

@RestController
@RequestMapping(value = "/lastClient")
public class LastClientTau {

    private static final Logger log = (Logger) LoggerFactory.getLogger(LastClientTau.class);
    private Buffer buffer;
    private Coordinator coordinator;


    @Autowired
    public LastClientTau(Coordinator coordinator) {
        buffer = new Buffer();
        this.coordinator = coordinator;
    }


    @PostMapping(value = "/newNonce")
    void registerNonce(@RequestBody JsonNode body) {
        log.info("Got: {}", body);
        buffer.put(body.get("substationID").asInt(), body.get("fid").asInt(), body.get("clientID").asInt(), new BigInteger(body.get("nonce").toString()));
    }

    @GetMapping(value = "/{substationID}/{fid}/computeLastTau")
    BigInteger computeLastTau(@PathVariable int substationID, @PathVariable int fid) {
        log.debug("Computing lastClientProof: substationID:{} fid: {} clients:{}",
                substationID, fid, buffer.getNumberOfNonce(substationID, fid));
        BigInteger nonceSum = buffer.getNonceSum(substationID, fid);
        return lastClientProof(nonceSum, coordinator.getFieldBase(substationID), coordinator.getGenerator(substationID));
    }


    public BigInteger lastClientProof(BigInteger nonceSum, BigInteger fieldBase, BigInteger generator) {
        BigInteger totient = eulerTotient(fieldBase);
        BigDecimal sum = new BigDecimal(nonceSum);
        BigDecimal tot = new BigDecimal(totient);
        BigInteger ceil = sum.divide(tot, RoundingMode.CEILING).toBigInteger();
        BigInteger result = totient.multiply(ceil).subtract(nonceSum).mod(fieldBase);
        return hash(result, fieldBase, generator);
    }

    private BigInteger eulerTotient(BigInteger prime) {
        if (!prime.isProbablePrime(16)) {
            throw new RuntimeException("No prime, no totient");
        }
        return prime.subtract(BigInteger.ONE);
    }

    public BigInteger hash(BigInteger input, BigInteger fieldBase, BigInteger generator) {
        return generator.modPow(input, fieldBase);
    }

}
