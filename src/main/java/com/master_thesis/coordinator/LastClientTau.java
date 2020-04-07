package com.master_thesis.coordinator;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.master_thesis.coordinator.data.Buffer;
import com.master_thesis.coordinator.data.IncomingNonceData;
import lombok.SneakyThrows;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;

@RestController
@RequestMapping(value = "/lastClient")
public class LastClientTau {

    private static final Logger log = (Logger) LoggerFactory.getLogger(LastClientTau.class);
    private Buffer buffer;
    private Coordinator coordinator;
    private ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    public LastClientTau(Coordinator coordinator) {
        buffer = new Buffer();
        this.coordinator = coordinator;
    }


    @SneakyThrows
    @PostMapping(value = "/newNonce")
    void registerNonce(@RequestBody IncomingNonceData body) {
        log.debug("Got: {}", objectMapper.writeValueAsString(body));
        buffer.put(body);
    }

    @SneakyThrows
    @GetMapping(value = "/{substationID}/{fid}/computeLastTau")
    BigInteger computeLastTau(@PathVariable int substationID, @PathVariable int fid) {
        while (!buffer.contains(substationID, fid) || buffer.getNumberOfNonce(substationID, fid) != coordinator.clients.get(substationID).size()) {
            log.error("All nonces have not been sent for Fid {} in substation {}. Can't compute yet.", fid, substationID);
            Thread.sleep(1000);
        }
        log.info("Computing lastClientProof: substationID:{} fid: {} clients:{}",
                substationID, fid, buffer.getNumberOfNonce(substationID, fid));
        BigInteger nonceSum = buffer.getNonceSum(substationID, fid);
        BigInteger fieldBase = coordinator.getFieldBase(substationID);
        return hash(computeNonceInverse(nonceSum, new BigInteger[]{fieldBase}),fieldBase, coordinator.getGenerator(substationID));
    }

    public BigInteger computeNonceInverse(BigInteger nonceSum, BigInteger[] fieldPrimes) {
        BigInteger fieldBase = Arrays.stream(fieldPrimes).reduce(BigInteger.ONE, BigInteger::multiply);
        BigInteger totient = eulerTotient(fieldPrimes);
        BigDecimal sum = new BigDecimal(nonceSum);
        BigDecimal tot = new BigDecimal(totient);
        BigInteger ceil = sum.divide(tot, RoundingMode.CEILING).toBigInteger();
       return totient.multiply(ceil).subtract(nonceSum).mod(fieldBase);
    }


    private BigInteger eulerTotient(BigInteger prime) {
        if (!prime.isProbablePrime(16)) {
            throw new RuntimeException("No prime, no totient");
        }
        return prime.subtract(BigInteger.ONE);
    }

    private BigInteger eulerTotient(BigInteger[] sk){
        return Arrays.stream(sk).map(this::eulerTotient).reduce(BigInteger.ONE, BigInteger::multiply);
    }

    public BigInteger hash(BigInteger input, BigInteger fieldBase, BigInteger generator) {
        return generator.modPow(input, fieldBase);
    }

    public BigInteger getRn(int substationID, int fid, BigInteger[] sk){
        return computeNonceInverse(buffer.getNonceSum(substationID, fid), sk);
    }
}
